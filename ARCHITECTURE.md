# Architecture Review & Suggestions

> Synthesized from three independent reviews (GPT-4.5, Gemini 2.5 Pro, Claude Opus 4.5).

---

## What to Keep

### Mixin-based constructor access
`MekanismRecipeTypeMixin` using `@Invoker` to call the package-private `MekanismRecipeType` constructor is the correct and only viable approach without forking Mekanism. Keep it as-is.

### Deferred registration wiring
All six `DeferredRegister`s being created and subscribed to `modEventBus` within a single `RegisterMachineAtRuntime` call is structurally correct. The `AtomicReference` forward-reference trick works for now (though it can be improved — see below).

### Extending Mekanism's tile entity base classes
`GenericMachineTileEntity extends TileEntityElectricMachine` is the right approach. It inherits all Mekanism machine behaviour for free (upgrades, energy containers, config, ejector, computer integration). This pattern must be extended to other base classes, not replaced.

---

## What to Change

### 1. Replace `MachineParameters` with a typed `MachineDefinition` hierarchy

**Problem:** `MachineParameters` holds only two strings. Every machine detail — energy config, sound, `FactoryType`, recipe shape, slot layout — is hardcoded inside `RegisterMachineAtRuntime` to Enrichment Chamber values.

**Solution:** Introduce a sealed `MachineTypeDefinition` interface with one concrete record per recipe shape family:

```java
sealed interface MachineTypeDefinition permits ItemToItemMachine, ItemChemicalToItemMachine, ... {
    String machineName();
    String processName();
    Supplier<FloatingLong> energyUsage();
    Supplier<FloatingLong> energyStorage();
    SoundEvent sound();
    FactoryType factoryType();
}

record ItemToItemMachine(...) implements MachineTypeDefinition {}
record ItemChemicalToItemMachine(...) implements MachineTypeDefinition {}
```

This is the single object a JSON deserializer or KubeJS script constructs. All variance lives here.

### 2. Create a `GenericMachineTileEntity` per recipe category

**Problem:** The current `GenericMachineTileEntity` hard-codes `SingleRecipeInput`, `ItemStackToItemStackRecipe`, and `SingleItem` at the class level. It covers exactly one of Mekanism's ~25 recipe shapes.

**Solution:** Create one generic tile entity per recipe category, mirroring Mekanism's own split:

```java
// Item → Item (Enricher, Crusher, Smelter, Sawmill)
class GenericElectricMachineTile extends TileEntityElectricMachine { ... }

// Item + Chemical → Item (Compressor, Purifier, Injector, Infuser, Painter)
class GenericAdvancedElectricMachineTile extends TileEntityAdvancedElectricMachine { ... }
```

Each subclass owns its slot layout, cache type, and `createNewCachedRecipe` logic. A `MachineTypeDefinition` subtype selects which tile class to instantiate.

> Note: `FactoryType` is a closed Mekanism enum. Custom machines will need to borrow an existing `FactoryType` (e.g. `ENRICHING` for item→item machines). This is an inherent Mekanism limitation — document it clearly rather than trying to fight it.

### 3. Decompose `RegisterMachineAtRuntime` into a typed dispatcher

**Problem:** The method is a 145-line monolith that hardcodes Enrichment Chamber config for every machine. Adding a second shape requires duplicating it.

**Solution:** A dispatcher that selects the correct registrar per definition type:

```java
MachineRegistrar.forDefinition(myMachineDefinition)
    .withModId("mymod")
    .build(modEventBus); // returns registered holders
```

Internally, switch on the `MachineTypeDefinition` subtype to select the correct tile class, factory type, and slot config. Adding a new machine category = adding one `MachineTypeDefinition` subtype + one registrar branch. Nothing else changes.

### 4. Replace the static `screenContainers` list with a `MachineRegistry` map

**Problem:** `MekanismCustomMachines.screenContainers` is a public mutable static list typed specifically to `GenericMachineTileEntity`. It breaks as soon as a second tile class is introduced.

**Solution:**

```java
public class MachineRegistry {
    private static final Map<ResourceLocation, RegisteredMachine> MACHINES = new LinkedHashMap<>();

    public static void register(ResourceLocation id, RegisteredMachine machine) { ... }
    public static Collection<RegisteredMachine> all() { ... }
}
```

`RegisteredMachine` holds the `ContainerTypeRegistryObject`, `TileEntityTypeRegistryObject`, `BlockRegistryObject`, and the originating `MachineTypeDefinition`. Screen registration iterates this map with type-erased containers.

### 5. Implement the KubeJS bridge around `StartupEvents`

**Problem:** `MekanismCustomMachinesKubeJSPlugin` is an empty stub.

**Critical constraint:** Machine registration (blocks, items, tile entities, recipe types) must happen during mod construction — before NeoForge's `RegisterEvent` fires. This means KubeJS machine definitions must be collected via `StartupEvents`, not server reload events.

```java
public class MekanismCustomMachinesKubeJSPlugin implements KubeJSPlugin {
    @Override
    public void registerEvents(KubeJSEventFactory factory) {
        factory.register(MachineRegistrationEventJS.ID, MachineRegistrationEventJS.class);
    }
}
```

`MachineRegistrationEventJS` exposes a fluent builder API to scripts, collects `MachineTypeDefinition` instances, and triggers `MachineRegistrar.build()` for each before the registration window closes.

Recipe *instances* (what goes in/comes out) can be added at server reload time and should use a separate KubeJS `RecipeEventHandler` per registered machine type.

### 6. Add per-machine recipe viewer layout

**Problem:** `recipeViewerType()` hardcodes pixel offsets `(-28, -16, 144, 54)` — valid only for item→item machines with one input and one output slot.

**Solution:** Include a layout value in each `MachineTypeDefinition`:

```java
record MachineRecipeViewerLayout(int xOffset, int yOffset, int width, int height) {
    static final MachineRecipeViewerLayout ELECTRIC = new MachineRecipeViewerLayout(-28, -16, 144, 54);
}
```

### 7. (Optional) Support caller-supplied namespaces

If this mod is intended to be a library (other mods/packs define their own machines), all registrations should support a caller-supplied `modId` namespace rather than always using `mekanismcustommachines`. This is required for correct datapack recipe JSON paths (`data/<namespace>/recipes/...`).

---

## Summary

| Concern | Current State | Target State |
|---|---|---|
| Recipe type coverage | `Item→Item` only | All shapes via typed definitions |
| Machine config | 2 strings, rest hardcoded | Full `MachineTypeDefinition` record |
| Registration logic | Monolith, Enricher-hardcoded | Dispatcher per definition type |
| Machine state tracking | Static mutable list | `MachineRegistry` map |
| KubeJS integration | Empty stub | `StartupEvents` builder API |
| Recipe viewer layout | Hardcoded offsets | Per-definition layout config |
| Namespace | Always `mekanismcustommachines` | Caller-supplied |

The mixin trick and deferred registration wiring are solid foundations. The entire layer above them — parameterization, config, dispatching, and the KubeJS bridge — needs to be rebuilt around the typed `MachineTypeDefinition` hierarchy before any meaningful expansion is possible.
