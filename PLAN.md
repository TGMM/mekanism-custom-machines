# Implementation Plan: MekanismCustomMachines

## Overview

This mod serves two purposes:
1. A **library** that any Java mod can depend on to register custom Mekanism machines without duplicating boilerplate.
2. A **platform integration layer** exposing that library to scripting environments (KubeJS first, CraftTweaker and JSON later).

The current PoC is a flat package with a 145-line monolith hardcoded to the Enrichment Chamber. This plan describes how to restructure and extend it.

**Key constraints:**
- Machine registration (blocks, tile entities, recipe types) must happen during mod construction, before NeoForge's `RegisterEvent` fires.
- Recipe *instances* (what goes in/comes out) can be added at server reload time.
- `FactoryType` is a closed Mekanism enum. All v1 custom machines are registered as **non-factory machines** via `MachineBuilder.createMachine()`. Factory tier support is deferred to v2 (see Section 8).
- `MekanismRecipeType`'s constructor is package-private and its `register()` method rejects any namespace other than `mekanism`. The `@Invoker` mixin bypasses both constraints and must be kept.
- Speed, energy, and muffling **upgrade support is fully preserved** in v1. `AttributeUpgradeSupport.DEFAULT_MACHINE_UPGRADES` is added by the base `Machine` constructor, which runs regardless of whether `createMachine()` or `createFactoryMachine()` is used. Only the tier installer upgrade path (`AttributeUpgradeable`) is withheld in v1.

---

## 1. Target Package Structure

```
src/main/java/com/kaliumstudios/mekanismcustommachines/
│
├── api/                                              ← PUBLIC, STABLE (no impl/ imports)
│   ├── MekanismCustomMachinesAPI.java               Façade: MODID constant, logger, version
│   ├── MachineRegistry.java                         Entry point: register(def) → RegisteredMachine
│   │
│   ├── definition/
│   │   ├── MachineDefinition.java                   Sealed interface (common fields)
│   │   ├── ItemToItemDefinition.java                record + nested Builder
│   │   ├── ItemChemicalToItemDefinition.java        record + nested Builder
│   │   ├── ItemToChemicalDefinition.java            record + nested Builder
│   │   ├── ChemicalToItemDefinition.java            record + nested Builder
│   │   ├── ChemicalToChemicalDefinition.java        record + nested Builder
│   │   ├── FluidToFluidDefinition.java              record + nested Builder
│   │   ├── CombinerDefinition.java                  record + nested Builder (2 items → 1 item)
│   │   └── SawmillDefinition.java                   record + nested Builder (1 item → 1 + chance item)
│   │
│   ├── energy/
│   │   └── EnergyProfile.java                       usage + storage suppliers
│   │
│   ├── layout/
│   │   ├── RecipeViewerLayout.java                  xOffset, yOffset, width, height
│   │   └── SlotLayout.java                          Future-proofing for custom GUIs; v1 uses presets
│   │
│   ├── event/
│   │   └── RegisterCustomMachinesEvent.java         Fired on mod event bus during @Mod construction
│   │
│   ├── handle/
│   │   ├── RegisteredMachine.java                   Opaque interface returned from MachineRegistry.register()
│   │   └── MachineHolders.java                      Read-only view: block/item/te/container/recipe-type holders
│   │
│   └── recipe/
│       └── CustomRecipeBuilder.java                 Fluent helper to construct recipe instances at server reload
│
├── impl/                                             ← INTERNAL (never imported by api/ or integration/)
│   ├── MekanismCustomMachines.java                  @Mod main class
│   ├── MekanismCustomMachinesClient.java            Client @Mod class
│   ├── Config.java
│   │
│   ├── registry/
│   │   ├── MachineRegistryImpl.java                 LinkedHashMap<ResourceLocation, RegisteredMachineImpl>
│   │   └── RegisteredMachineImpl.java               Holds all 6 DeferredHolder references + definition
│   │
│   ├── pipeline/
│   │   ├── MachineRegistrar.java                    Sealed interface: register(DeferredBundle, MachineDefinition)
│   │   ├── AbstractMachineRegistrar.java            Common steps: block, item, container type, tile entity
│   │   ├── ItemToItemRegistrar.java
│   │   ├── ItemChemicalToItemRegistrar.java
│   │   ├── ItemToChemicalRegistrar.java
│   │   ├── ChemicalToItemRegistrar.java
│   │   ├── ChemicalToChemicalRegistrar.java
│   │   ├── FluidToFluidRegistrar.java
│   │   ├── CombinerRegistrar.java
│   │   ├── SawmillRegistrar.java
│   │   ├── RegistrarDispatcher.java                 switch(def) → correct MachineRegistrar
│   │   └── DeferredBundle.java                      Groups 6 DeferredRegisters; replaces AtomicReference
│   │
│   ├── tile/
│   │   ├── GenericElectricMachineTile.java          extends TileEntityElectricMachine (Item→Item)
│   │   ├── GenericAdvancedElectricMachineTile.java  extends TileEntityAdvancedElectricMachine (Item+Chemical→Item)
│   │   ├── GenericItemToChemicalTile.java
│   │   ├── GenericChemicalToItemTile.java
│   │   ├── GenericChemicalToChemicalTile.java
│   │   ├── GenericFluidToFluidTile.java
│   │   ├── GenericCombinerTile.java
│   │   └── GenericSawmillTile.java
│   │
│   ├── recipe/
│   │   ├── GenericItemToItemRecipe.java
│   │   ├── GenericItemChemicalToItemRecipe.java
│   │   ├── GenericItemToChemicalRecipe.java
│   │   ├── GenericChemicalToItemRecipe.java
│   │   ├── GenericChemicalToChemicalRecipe.java
│   │   ├── GenericFluidToFluidRecipe.java
│   │   ├── GenericCombinerRecipe.java
│   │   └── GenericSawmillRecipe.java
│   │
│   ├── client/
│   │   └── ScreenBinder.java                        Iterates MachineRegistry.all(), registers screens
│   │
│   ├── asset/
│   │   └── FactoryTypeAssetResolver.java            Maps definition.factoryType() → base machine GUI/sound
│   │
│   └── mixin/
│       └── MekanismRecipeTypeMixin.java             (moved from current flat location)
│
└── integration/
    ├── kubejs/
    │   ├── MekanismCustomMachinesKubeJSPlugin.java
    │   ├── startup/
    │   │   ├── MachineRegistrationEventJS.java      KubeJS StartupEvents binding
    │   │   └── JSDefinitionBuilders.java            One JS-friendly builder per shape
    │   └── recipe/
    │       └── KubeJSRecipeHandlers.java            Registers per-machine recipe handlers for ServerEvents.recipes
    │
    ├── crafttweaker/                                (empty — Phase 7)
    └── json/                                        (empty — Phase 8)
```

### Resource file updates required alongside the restructure

| File | Change |
|---|---|
| `mekanismcustommachines.mixins.json` | Mixin package → `com.kaliumstudios.mekanismcustommachines.impl.mixin` |
| `neoforge.mods.toml` | Main mod class FQN → `...impl.MekanismCustomMachines` |
| `kubejs.plugins.txt` | Plugin FQN → `...integration.kubejs.MekanismCustomMachinesKubeJSPlugin` |

---

## 2. Core Abstractions

### 2.1 `MachineDefinition` (sealed interface)

```java
public sealed interface MachineDefinition
    permits ItemToItemDefinition, ItemChemicalToItemDefinition,
            ItemToChemicalDefinition, ChemicalToItemDefinition,
            ChemicalToChemicalDefinition, FluidToFluidDefinition,
            CombinerDefinition, SawmillDefinition {

    ResourceLocation id();           // full namespace:path, e.g. "mymod:my_machine"
    String processName();            // shown in GUI progress bar
    EnergyProfile energy();          // usage + storage
    int baseTicksRequired();
    SoundEvent sound();
    RecipeViewerLayout viewerLayout();
}
```

Each subtype is a `record` with a nested `Builder` for fluent construction.

All v1 machines are built with `MachineBuilder.createMachine()`. This adds `AttributeUpgradeSupport.DEFAULT_MACHINE_UPGRADES` (speed, energy, muffling upgrades) but intentionally omits `AttributeUpgradeable` (tier installer → factory upgrade). The `FactoryType` field is introduced in v2 once factory tier support is implemented (see Section 8).

### 2.2 `EnergyProfile`

```java
public record EnergyProfile(
    Supplier<FloatingLong> usage,
    Supplier<FloatingLong> storage
) {
    public static EnergyProfile of(FloatingLong usage, FloatingLong storage);
    public static EnergyProfile borrowFrom(Supplier<FloatingLong> usage, Supplier<FloatingLong> storage);
}
```

`borrowFrom` lets Java mods pass `() -> MekanismConfig.usage.enrichmentChamber.get()` (or any other Mekanism config).

### 2.3 `MachineRegistry` (API entry point)

```java
public final class MachineRegistry {
    public static RegisteredMachine register(MachineDefinition definition);
    public static Collection<RegisteredMachine> all();
    public static Optional<RegisteredMachine> get(ResourceLocation id);
}
```

Backed by `MachineRegistryImpl` in `impl/`. `register()` may only be called before `RegisterEvent` fires (enforced with a logged error if violated).

### 2.4 `RegisterCustomMachinesEvent`

```java
public class RegisterCustomMachinesEvent extends Event {
    public static final String ID = "mekanismcustommachines:register_machines";

    private final MachineRegistry registry;

    public RegisteredMachine register(MachineDefinition definition) {
        return registry.register(definition);
    }
}
```

Fired in the `MekanismCustomMachines` `@Mod` constructor on `NeoForge.EVENT_BUS` so any other mod can subscribe and register before the window closes.

### 2.5 `DeferredBundle` (replaces AtomicReference hacks)

```java
public class DeferredBundle {
    public final DeferredRegister<Block> blocks;
    public final DeferredRegister<Item> items;
    public final DeferredRegister<BlockEntityType<?>> tileEntities;
    public final DeferredRegister<RecipeType<?>> recipeTypes;
    public final DeferredRegister<RecipeSerializer<?>> recipeSerializers;
    public final DeferredRegister<MenuType<?>> containerTypes;

    public DeferredBundle(String modId, IEventBus eventBus);
}
```

Each registrar creates its `DeferredHolder`s from the bundle. Back-references (e.g. tile needing the block's holder) use `DeferredHolder::get()` called lazily from within the tile entity factory lambda, which is only invoked after all holders are resolved.

### 2.6 Registrar pattern

```java
sealed interface MachineRegistrar<D extends MachineDefinition>
    permits ItemToItemRegistrar, ItemChemicalToItemRegistrar, ... {

    RegisteredMachineImpl register(D definition, DeferredBundle bundle);
}
```

`RegistrarDispatcher.dispatch(MachineDefinition, DeferredBundle)` switches on the sealed subtype and delegates to the correct registrar. Adding a new recipe shape = adding one `MachineDefinition` subtype + one `MachineRegistrar` implementation + one `switch` branch.

---

## 3. Recipe Shape Coverage

All v1 machines are non-factory (`createMachine()`). They support speed, energy, and muffling upgrades via the upgrade tab but cannot be upgraded to factory tiers with a tier installer. The factory column shows which Mekanism `FactoryType` would be associated in v2.

| Sub-phase | Shape | Mekanism machines covered | v2 FactoryType |
|---|---|---|---|
| Phase 2 (current) | Item → Item | Enrichment Chamber, Crusher, Energized Smelter | `ENRICHING`, `CRUSHING`, `SMELTING` |
| Phase 4a | Item + Chemical → Item | Compressor, Purifier, Injector, Infuser, Painter | `COMPRESSING`, `PURIFYING`, `INJECTING`, `INFUSING` |
| Phase 4b | Item + Item → Item | Combiner | `COMBINING` |
| Phase 4c | Item → Item (+ chance secondary) | Precision Sawmill | `SAWING` |
| Phase 4d | Item → Chemical | Oxidizer, Pigment Extractor, Chemical Conversion | None |
| Phase 4e | Chemical → Item | Chemical Crystallizer | None |
| Phase 4f | Chemical → Chemical | Activator, Centrifuge | None |
| Phase 4g | Fluid → Fluid | Thermal Evaporation | None |
| v2+ | Item + Fluid + Chemical → Item | Reaction Chamber | None |
| v2+ | Fluid + Chemical → Chemical | Chemical Washer | None |
| v2+ | Bidirectional | Rotary Condensentrator | None |

Each sub-phase follows the same self-contained pattern:
1. Add `XxxDefinition` record + `Builder` in `api/definition/`
2. Add `GenericXxxTile` in `impl/tile/`
3. Add `GenericXxxRecipe` in `impl/recipe/`
4. Add `XxxRegistrar` in `impl/pipeline/`
5. Add the `case XxxDefinition` branch in `RegistrarDispatcher`
6. Validate with a test machine in `MekanismCustomMachinesRuntimeTest`

---

## 4. Phased Roadmap

### Phase 1 — Package reorganization (no behaviour change)

**Tasks:**
1. Create the full package tree (empty packages + placeholder files where needed).
2. Move existing classes to new locations:
   - `MekanismCustomMachines` → `impl/`
   - `MekanismCustomMachinesClient` → `impl/`
   - `Config` → `impl/`
   - `GenericMachineTileEntity` → `impl/tile/GenericElectricMachineTile`
   - `GenericItemToItemRecipe` → `impl/recipe/`
   - `MekanismCustomMachinesRuntimeTest` → `impl/pipeline/` (temporary — deleted in Phase 2)
   - `MekanismRecipeTypeMixin` → `impl/mixin/`
   - `MekanismCustomMachinesKubeJSPlugin` → `integration/kubejs/`
3. Update `mekanismcustommachines.mixins.json`, `neoforge.mods.toml`, `kubejs.plugins.txt` FQNs.
4. Create empty stub files for all `api/` classes so the project compiles.

**Definition of done:** project compiles; the test machine (`test_chamber`) still registers and works in-game; no flat-package files remain.

---

### Phase 2 — Core API + Item→Item refactor

**Tasks:**
1. Implement `api/definition/MachineDefinition` (sealed interface).
2. Implement `api/definition/ItemToItemDefinition` (record + Builder).
3. Implement `api/energy/EnergyProfile`.
4. Implement `api/layout/RecipeViewerLayout` with a preset `ELECTRIC` constant.
5. Implement `api/handle/RegisteredMachine` and `MachineHolders`.
6. Implement `api/MachineRegistry` interface.
7. Implement `impl/pipeline/DeferredBundle`.
8. Implement `impl/pipeline/MachineRegistrar` sealed interface.
9. Implement `impl/pipeline/AbstractMachineRegistrar` (shared block/item/container/tile wiring).
10. Implement `impl/pipeline/ItemToItemRegistrar` (recipe type via mixin, serializer, tile factory).
11. Implement `impl/pipeline/RegistrarDispatcher`.
12. Implement `impl/registry/MachineRegistryImpl` and `RegisteredMachineImpl`.
13. Update `impl/MekanismCustomMachines` to call `MachineRegistry.register(ItemToItemDefinition.builder()…build())` in the constructor.
14. Delete `MekanismCustomMachinesRuntimeTest` and `MachineParameters`.

**Definition of done:** the test machine registers through the new API; `AtomicReference` is gone; no hardcoded Enrichment Chamber config.

---

### Phase 3 — Lifecycle & event bus integration

**Tasks:**
1. Implement `api/event/RegisterCustomMachinesEvent`.
2. Fire the event in `impl/MekanismCustomMachines` constructor on `NeoForge.EVENT_BUS` before `DeferredRegister`s are committed.
3. Replace `MekanismCustomMachines.screenContainers` (public static list) with `impl/client/ScreenBinder` that iterates `MachineRegistry.all()` at `RegisterMenuScreensEvent`.
4. Guard `MachineRegistry.register()` against calls after `RegisterEvent` has fired (log a descriptive error).
5. Create `api/MekanismCustomMachinesAPI.java` with the `MODID` constant, logger, and a `getVersion()` utility.

**Definition of done:** the static `screenContainers` field is gone; an external Java mod can subscribe to `RegisterCustomMachinesEvent` and register a machine without touching this mod's internals.

---

### Phase 4 — Recipe shape expansion

Each sub-phase is a self-contained unit following the pattern documented in Section 3.

**4a — Item + Chemical → Item**
- `ItemChemicalToItemDefinition` (adds `long maxChemical` and `ChemicalStack defaultChemical` to the builder)
- `GenericAdvancedElectricMachineTile` extends `TileEntityAdvancedElectricMachine`
- `GenericItemChemicalToItemRecipe`
- `ItemChemicalToItemRegistrar`
- Test machine: `test_compressor` (FactoryType.COMPRESSING)

**4b — Combiner (Item + Item → Item)**
- `CombinerDefinition` (adds secondary item input)
- `GenericCombinerTile`
- `GenericCombinerRecipe`
- `CombinerRegistrar` (FactoryType.COMBINING)

**4c — Sawmill (Item → Item + chance secondary)**
- `SawmillDefinition` (adds secondary output chance float)
- `GenericSawmillTile`
- `GenericSawmillRecipe`
- `SawmillRegistrar` (FactoryType.SAWING)

**4d — Item → Chemical**
- `ItemToChemicalDefinition`
- `GenericItemToChemicalTile`
- `GenericItemToChemicalRecipe`
- `ItemToChemicalRegistrar` (`factoryType()` returns null — uses `MachineBuilder.createMachine`)

**4e — Chemical → Item**
- `ChemicalToItemDefinition`
- `GenericChemicalToItemTile`
- `GenericChemicalToItemRecipe`
- `ChemicalToItemRegistrar` (non-factory)

**4f — Chemical → Chemical**
- `ChemicalToChemicalDefinition`
- `GenericChemicalToChemicalTile`
- `GenericChemicalToChemicalRecipe`
- `ChemicalToChemicalRegistrar` (non-factory)

**4g — Fluid → Fluid**
- `FluidToFluidDefinition`
- `GenericFluidToFluidTile`
- `GenericFluidToFluidRecipe`
- `FluidToFluidRegistrar` (non-factory)

**Definition of done (per sub-phase):** test machine for that shape registers; previous shapes unaffected; machine visible in JEI/EMI recipe viewer.

---

### Phase 5 — KubeJS integration

**Tasks:**
1. Implement `MachineRegistrationEventJS` as a KubeJS `StartupEvents` binding. Exposes shape-specific builder methods:
   ```js
   // Example KubeJS startup script
   MekanismCustomMachines.startup.registerMachine(event => {
       event.itemToItem('mymod:my_machine', m => {
           m.processName('Processing')
           m.energyUsage(200)
           m.energyStorage(10000)
           m.ticks(200)
           m.factoryType('ENRICHING')
       })
   })
   ```
2. Implement `JSDefinitionBuilders` — one JS-friendly builder per shape, wrapping the Java `Builder` classes.
3. Implement `KubeJSRecipeHandlers` — registers one recipe handler per machine registered in `MachineRegistry.all()` at server reload. Exposes:
   ```js
   // Example KubeJS server script
   ServerEvents.recipes(event => {
       event.recipes.mymod.my_machine({ input: 'minecraft:cobblestone', output: 'minecraft:stone' })
   })
   ```
4. Wire both in `MekanismCustomMachinesKubeJSPlugin.registerEvents()` and `registerRecipeHandlers()`.
5. Document KubeJS API in `README.md`.

**Definition of done:** a pack with only KubeJS scripts (no Java code) can define a custom machine and add recipes for it.

---

### Phase 6 — Library hardening

**Tasks:**
1. Add Javadoc to all `api/` classes and interfaces.
2. Add a `@ApiStatus.Internal` annotation or equivalent on all `impl/` and `integration/` root packages to signal to downstream IDE users that these are not for external consumption.
3. Add an `examples/` folder at the repo root containing:
   - `ExampleJavaMod.java` — a minimal Java mod that subscribes to `RegisterCustomMachinesEvent`
   - `example_startup.js` — a KubeJS startup script registering a machine
   - `example_server.js` — a KubeJS server script adding a recipe to it
4. Add a `CHANGELOG.md` with the `api/` surface marked as semver-stable from v1.0.

**Definition of done:** a Java mod can depend on this mod and use only `api/` classes; README clearly documents both usage paths.

---

### Phase 7+ — Future bridges (out of scope for v1)

**CraftTweaker (`integration/crafttweaker/`)**
Mirror the KubeJS pattern. Same `MachineDefinition` objects; different scripting front-end.
- `CustomMachineManager` — `@ZenCodeType.Name("mods.mekanismcustommachines.MachineManager")`
- One `@ZenCodeType.Method` per definition shape

**JSON datapack loader (`integration/json/`)**
JSON files under `data/<namespace>/custom_machines/<machine_id>.json`. Must be read at pack discovery time (not server reload) because blocks/TEs need `RegisterEvent`. Implementation uses a custom `PackResources` scanner registered at `AddPackFindersEvent`. JSON schema follows the `MachineDefinition` record structure. Uses Gson/Codec deserialization.

**Procedural texture differentiation**
Currently custom machines reuse base machine assets by borrowing from a specified Mekanism base machine sound/GUI. A future `DynamicTextureProvider` could tint a base texture per machine using a colour specified in the definition. Defer until pack authors request it.

---

## 8. V2: Factory Tier Support via `FactoryType` Extension

This section documents the approach for v2, where custom machines gain tier installer support (Basic → Advanced → Elite → Ultimate factory). It is **not part of v1** because `FactoryType` is a closed enum requiring coordinated mutation across multiple Mekanism classes.

### The Problem

`FactoryType` is a sealed `enum` with 9 values (SMELTING, ENRICHING, CRUSHING, COMPRESSING, COMBINING, PURIFYING, INJECTING, INFUSING, SAWING). Two Mekanism attributes gatekeep factory behaviour:

- **`AttributeFactoryType`** — tags a block as belonging to a specific factory category. Added only by `FactoryMachine` constructor (via `createFactoryMachine()`).
- **`AttributeUpgradeable`** — holds a supplier for the target block when a tier installer is used. Its mere presence is the **only gate** checked by `ItemTierInstaller.useOn()`. If absent, the installer silently does nothing (`InteractionResult.PASS`).

Using `createMachine()` in v1 adds neither attribute, which is correct behaviour: speed/energy/muffling upgrades still work (provided by `AttributeUpgradeSupport.DEFAULT_MACHINE_UPGRADES` which `Machine`'s base constructor always adds), but tier installers are safely ignored.

### Why `FactoryType` Extension Is Hard

Adding a new enum entry requires coordinated mutations across **five classes** in Mekanism:

| Class | Problem |
|---|---|
| `FactoryType` | `$VALUES` array is `private static final`; must mutate before any consumer loads |
| `EnumUtils` | `public static final FactoryType[] FACTORY_TYPES = FactoryType.values()` — snapshot cached at class load; must be replaced via `Unsafe` |
| `MekanismBlocks` | Two exhaustive `switch` expressions in `registerFactory()` — Java 21 throws `MatchException` on unknown ordinals |
| `MekanismBlockTypes` | `Table<FactoryTier, FactoryType, ...>` populated by iterating `FACTORY_TYPES` in static initializer |
| `MekanismTileEntityTypes` | Same `Table` pattern; must include new entry or `getFactory(tier, myType)` returns null |

### Proposed V2 Implementation Strategy

**Step 1 — Enum entry injection (before `EnumUtils` loads)**

Use `sun.misc.Unsafe` to mutate `FactoryType.$VALUES` inside a `@Mixin @Inject(at = @At("TAIL"))` targeting `FactoryType.<clinit>`. This runs immediately after the enum's static initializer, before any other class can cache `values()`.

```java
@Mixin(FactoryType.class)
public class FactoryTypeExtensionMixin {
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void injectCustomTypes(CallbackInfo ci) {
        EnumExtender.addEntry(FactoryType.class,
            "MY_TYPE", "mytype", myLangEntry, myMachineSupplier, myBlockSupplier);
    }
}
```

`EnumExtender` is a utility class in `impl/mixin/` that uses `Unsafe.putObject` to replace the `$VALUES` field and clears `Class.enumConstants` / `Class.enumConstantDirectory` caches.

**Step 2 — Patch `EnumUtils.FACTORY_TYPES`**

`EnumUtils.FACTORY_TYPES` is assigned `FactoryType.values()` in a field initializer. Since the mixin injection in Step 1 runs before `EnumUtils` loads (guaranteed by the load ordering: `FactoryType` is referenced first), the snapshot will already contain the new entry — **provided** the mixin runs before `EnumUtils` is classloaded. This ordering must be validated and documented.

As a safety fallback, add a `@Mixin @Inject(at = @At("TAIL"))` on `EnumUtils.<clinit>` that overwrites `FACTORY_TYPES` via `Unsafe` with the current `FactoryType.values()`.

**Step 3 — Patch exhaustive switches in `MekanismBlocks.registerFactory()`**

Two `switch` expressions in `registerFactory()` are exhaustive over all `FactoryType` values. Java 21 compiles these as `tableswitch` bytecode that throws `MatchException` for unrecognized ordinals. Use **MixinExtras `@WrapOperation`** to intercept the entire `registerFactory` call for custom entries and route to a separate registration path that bypasses both switches entirely.

**Step 4 — Populate the factory block/tile tables**

`MekanismBlockTypes.FACTORIES` and `MekanismBlocks.FACTORIES` are `HashBasedTable` instances populated in static initializers. Add a `@Inject(at = @At("TAIL"))` into each `<clinit>` to call `FACTORIES.put(tier, myType, ...)` for each custom factory tier × type combination. The tile entity type and block registry objects are resolved from this mod's own `DeferredHolder`s.

**Step 5 — `MachineDefinition` in v2**

Add `@Nullable FactoryType factoryType()` back to `MachineDefinition`. When non-null, the registrar uses `createFactoryMachine()` and adds `AttributeUpgradeable`, enabling tier installer support. When null (default), `createMachine()` is used as in v1.

### Risk Summary

| Risk | Severity | Note |
|---|---|---|
| Class load order: `EnumUtils` must load after `FactoryType.<clinit>` injection | Critical | Must be tested per Mekanism version; any class referencing `EnumUtils` before mixin runs will cache the old array |
| `MatchException` in exhaustive switches | Critical | `@WrapOperation` workaround is the only viable injection point |
| `Unsafe` API stability on Java 21 | High | No official support; functional in practice with `--add-opens` flags NeoForge supplies |
| `EnumMap`/`EnumSet` snapshots | Medium | Any `EnumMap<FactoryType, ...>` constructed before injection will not contain custom types |
| Mekanism version coupling | High | Table population code in `MekanismBlocks`/`MekanismBlockTypes` may shift between Mekanism patch versions |

---

## 5. Key Technical Decisions

| Decision | Choice | Reason |
|---|---|---|
| API surface separation | Sub-packages (`api/` vs `impl/`) in one jar | Simplest to maintain; no extra gradle modules |
| Java consumer pattern | `RegisterCustomMachinesEvent` on NeoForge event bus | Most idiomatic; prevents accidental late registration |
| Generic tile entities | One class per recipe-shape family | Mirrors Mekanism's own class hierarchy; avoids combinatorial explosion |
| Recipe class per machine vs per shape | Per shape; `MekanismRecipeType` instance discriminates per machine | Reduces class count; the recipe type is the machine identity |
| Forward-reference handling | `DeferredHolder::get()` called lazily in factory lambdas | Cleaner than `AtomicReference`; same effect |
| v1 machine builder | All machines use `MachineBuilder.createMachine()` | Keeps speed/energy upgrades; blocks tier installers until v2 factory support lands |
| Asset strategy | `FactoryTypeAssetResolver` reuses base machine assets by `FactoryType` | No assets duplicated in addon jar; reuses Mekanism's existing resources |
| Energy config storage | Embedded in `EnergyProfile` (suppliers) | Simple for v1; KubeJS passes literals, Java mods pass Mekanism config suppliers |
| Namespace | `MachineDefinition#id()` is a full `ResourceLocation` | Downstream mods register under their own namespace; datapack recipe paths are correct |
| KubeJS binding phase | `StartupEvents` for machine type registration; `ServerEvents.recipes` for recipe instances | Machine types need mod-construction timing; recipe instances can reload |

---

## 6. Risks & Mitigations

| Risk | Mitigation |
|---|---|
| `MekanismRecipeType` constructor becomes truly private | Keep mixin target; add a reflection fallback in `AbstractMachineRegistrar` with a clear error message if mixin is unavailable |
| Multiple Mekanism addons competing for the registration window | Document that `RegisterCustomMachinesEvent` fires once, synchronously; handlers must not defer to async callbacks |
| JEI/EMI recipe viewer integration | `recipeViewerType()` on each tile must return a valid `IRecipeViewerRecipeType`. Build the viewer type in each registrar using the registered recipe type. Validate during each Phase 4 sub-phase. |
| KubeJS `ServerEvents.recipes` handler resolution | `MachineRegistry.all()` must be fully populated before server reload fires. Guaranteed because registry is populated at `@Mod` construction, which precedes world load. Validate during Phase 5. |
| Tier installer silently ignored on v1 machines | `createMachine()` omits `AttributeUpgradeable` — `ItemTierInstaller.useOn()` returns `PASS` immediately. Document this as intended v1 behaviour; users retain speed/energy/muffling upgrades. |

---

## 7. Definition of Done (Full)

| Phase | Criteria |
|---|---|
| 1 | Compiles; test machine registers; all source in new package tree; no flat-package files |
| 2 | Test machine registers via `MachineRegistry.register()`; `AtomicReference` gone; `RuntimeTest` deleted; speed/energy upgrades work in-game; tier installer does nothing on custom machine |
| 3 | Static `screenContainers` gone; external Java mod can register via event; late-registration guard in place |
| 4a–4g | Each shape has a working test machine; visible in JEI/EMI; previous shapes unaffected |
| 5 | KubeJS script alone (no Java changes) can register a machine and add recipes |
| 6 | All `api/` classes have Javadoc; `examples/` folder present; CHANGELOG started |
