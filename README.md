# Mekanism: Custom Machines

A NeoForge **library mod** for Minecraft 1.21.1 that lets other mods and KubeJS
packs register their own custom Mekanism machines (blocks, tile entities,
recipe types, recipe serializers, screens, and JEI/EMI recipe categories)
without duplicating the considerable amount of boilerplate involved.

The PoC behind this mod registers a generic Enrichment Chamber clone at
runtime by mixing into Mekanism's package-private `MekanismRecipeType`
constructor; this project generalises that trick into a typed pipeline that
supports eight distinct recipe shapes.

## Status

| Phase | Scope | Status |
|---|---|---|
| 1 | Package reorganisation | Done |
| 2 | Core API + `Item → Item` shape | Done |
| 3 | Lifecycle + event bus integration | Done |
| 4a–4g | Seven additional recipe shapes | Done |
| **5** | **KubeJS integration (this section)** | **Done** |
| 6 | Library hardening / examples / changelog | Pending |
| 7+ | CraftTweaker bridge, JSON datapack loader | Future |

All machines are non-factory (no tier installer support); upgrade-tab support
(speed, energy, muffling) is preserved. Factory tiers are deferred to v2 —
see `PLAN.md` Section 8 for the proposed implementation strategy.

## Supported recipe shapes (v1)

| Shape | Mekanism analog | Definition class |
|---|---|---|
| Item → Item | Enrichment Chamber, Crusher, Energized Smelter | `ItemToItemDefinition` |
| Item + Chemical → Item | Osmium Compressor, Purification Chamber, Chemical Injection Chamber, Painter | `ItemChemicalToItemDefinition` |
| Item + Item → Item | Combiner | `CombinerDefinition` |
| Item → Item + chance secondary | Precision Sawmill | `SawmillDefinition` |
| Item → Chemical | Chemical Oxidizer, Pigment Extractor | `ItemToChemicalDefinition` |
| Chemical → Item | Chemical Crystallizer | `ChemicalToItemDefinition` |
| Chemical → Chemical | Isotopic Centrifuge, Solar Neutron Activator | `ChemicalToChemicalDefinition` |
| Fluid → Fluid | (no single-block analog — adapted from Thermal Evaporation plant) | `FluidToFluidDefinition` |

## Java API

Subscribe to `RegisterCustomMachinesEvent` on the NeoForge game bus from your
own `@Mod` constructor:

```java
@Mod("mymod")
public class MyMod {
    public MyMod() {
        NeoForge.EVENT_BUS.addListener(this::registerMekanismMachines);
    }

    private void registerMekanismMachines(RegisterCustomMachinesEvent event) {
        event.register(
                ItemToItemDefinition.builder("mymod:my_enricher")
                        .processName("Enriching")
                        .energy(200, 10_000)
                        .ticks(200)
                        .build()
        );
    }
}
```

Your mod's `neoforge.mods.toml` must declare `mekanismcustommachines` as a
dependency loaded **AFTER** it; the event fires once during our `@Mod`
constructor.

## KubeJS API

The KubeJS bridge is loaded automatically when KubeJS is present. It exposes
two surfaces.

### 1. Registering machine types (startup script)

Use the `MekanismCustomMachines.registerMachines` event. Listeners run during
mod construction, so machine types are registered before NeoForge's
`RegisterEvent` fires.

```js
// startup_scripts/example_machines.js
MekanismCustomMachines.registerMachines(event => {
    event.itemToItem('mymod:my_enricher', m => {
        m.processName('Enriching')
        m.energy(200, 10_000)
        m.ticks(200)
    })

    event.itemChemicalToItem('mymod:my_injector', m => {
        m.energy(400, 20_000)
        m.ticks(200)
        m.maxChemical(10_000)
    })

    event.sawmill('mymod:my_sawmill', m => {
        m.energy(400, 20_000)
    })

    event.fluidToFluid('mymod:my_evaporator', m => {
        m.energy(800, 40_000)
        m.maxFluid(16_000)
    })
})
```

One method exists per supported shape:

| Method | Builder fields |
|---|---|
| `event.itemToItem(id, m => …)` | `processName`, `energy(usage, storage)`, `energyUsage`, `ticks`, `viewerLayout` |
| `event.itemChemicalToItem(id, m => …)` | …same, plus `maxChemical(long)` |
| `event.combiner(id, m => …)` | base fields |
| `event.sawmill(id, m => …)` | base fields |
| `event.itemToChemical(id, m => …)` | base fields plus `maxChemical(long)` |
| `event.chemicalToItem(id, m => …)` | base fields plus `maxChemical(long)` |
| `event.chemicalToChemical(id, m => …)` | base fields plus `maxChemical(long)` |
| `event.fluidToFluid(id, m => …)` | base fields plus `maxFluid(int)` |

The `id` is a full `namespace:path` `ResourceLocation`. Blocks, items, and
recipe types are registered under that namespace, so use your pack/mod's
namespace (not `mekanismcustommachines:`) for anything you ship.

### 2. Adding recipes (server script)

Each registered machine becomes a KubeJS recipe type at server reload. The
recipe schema mirrors the JSON layout Mekanism's recipe serializer expects.

```js
// server_scripts/example_recipes.js
ServerEvents.recipes(event => {
    // Item → Item
    event.recipes.mymod.my_enricher({
        input:  { item: 'minecraft:cobblestone' },
        output: { id:   'minecraft:stone' }
    })

    // Item + Chemical → Item
    event.recipes.mymod.my_injector({
        item_input:     { item: 'minecraft:diamond' },
        chemical_input: { chemical: 'mekanism:hydrogen_chloride', amount: 1 },
        output:         { id: 'minecraft:netherite_scrap' },
        per_tick_usage: true
    })

    // Precision Sawmill clone — main + chance secondary
    event.recipes.mymod.my_sawmill({
        input:            { tag: 'minecraft:logs' },
        main_output:      { id: 'minecraft:oak_planks', count: 6 },
        secondary_output: { id: 'minecraft:oak_sapling' },
        secondary_chance: 0.5
    })

    // Fluid → Fluid (heat-less evaporation analog)
    event.recipes.mymod.my_evaporator({
        input:  { fluid: 'minecraft:water', amount: 1000 },
        output: { id:    'mekanism:brine', amount: 1 }
    })
})
```

#### JSON field reference

The key names in the JS object match Mekanism's serialization constants
exactly (`mekanism.api.SerializationConstants`):

| Shape | Required keys | Optional keys |
|---|---|---|
| `itemToItem` | `input`, `output` | — |
| `itemChemicalToItem` | `item_input`, `chemical_input`, `output` | `per_tick_usage` (default `false`) |
| `combiner` | `main_input`, `extra_input`, `output` | — |
| `sawmill` | `input` | `main_output`, `secondary_output`, `secondary_chance` (default `0.0`) — at least one output must be present |
| `itemToChemical` | `input`, `output` | — |
| `chemicalToItem` | `input`, `output` | — |
| `chemicalToChemical` | `input`, `output` | — |
| `fluidToFluid` | `input`, `output` | — |

Item ingredients use the flat NeoForge `SizedIngredient` codec
(`{ "item": …, "count": … }` or `{ "tag": …, "count": … }`).

Fluid ingredients use the flat NeoForge `SizedFluidIngredient` codec
(`{ "fluid": …, "amount": … }`).

Chemical ingredients/stacks use Mekanism's own codec
(`{ "chemical": "mekanism:hydrogen", "amount": 100 }` for ingredients;
`{ "id": "mekanism:hydrogen", "amount": 100 }` for outputs).

Item outputs use the vanilla strict `ItemStack` codec
(`{ "id": "minecraft:stone", "count": 1 }`).

Fluid outputs use the standard NeoForge `FluidStack` codec
(`{ "id": "minecraft:water", "amount": 1000 }`).

## Architecture

See `ARCHITECTURE.md` and `PLAN.md` for the design rationale and roadmap.

## Building

```
./gradlew build
```

Runtime dependencies (Mekanism + KubeJS + EMI) are pulled from their public
Maven repositories. The mod targets Java 21 / Minecraft 1.21.1 / NeoForge
21.1.230.

## Licence

All Rights Reserved (placeholder — review `gradle.properties` before
publishing).
