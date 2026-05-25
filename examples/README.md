# Examples — Arcane Workshop

A complete KubeJS pack that exercises every recipe shape Mekanism: Custom
Machines supports. Use it as an end-to-end smoke test and a
copy-paste-friendly reference for your own custom machines.

## Layout

```
examples/
├── startup_scripts/
│   └── arcane_workshop.js     ← Registers eight custom machine blocks
└── server_scripts/
    └── arcane_recipes.js      ← One distinctive recipe per machine
```

## Installing

1. Copy `examples/startup_scripts/arcane_workshop.js` into your world's
   `run/kubejs/startup_scripts/` directory.
2. Copy `examples/server_scripts/arcane_recipes.js` into your world's
   `run/kubejs/server_scripts/` directory.
3. (Re)launch the dev client — startup scripts only reload reliably on
   restart. Recipes can be hot-reloaded later with `/reload`.

## What it adds

Every machine block is registered under the `arcane:` namespace so it cannot
collide with vanilla, Mekanism, or any other pack. The names are
intentionally flavourful — easy to spot in JEI / EMI, and obviously not
shipped by any other mod.

| Machine | Shape | Demo recipe |
|---|---|---|
| `arcane:bone_dust_mill` | Item → Item | `bone` → `bone_meal × 6` |
| `arcane:essence_imbuer` | Item + Chemical → Item | `redstone + 100 mB sulfuric_acid` → `blaze_powder` |
| `arcane:runic_press` | Item + Item → Item (combiner) | `gold_ingot + emerald` → `ender_pearl` |
| `arcane:prismatic_cutter` | Item → Item + chance secondary | `diamond` → `flint × 2`, 25 % chance `emerald` |
| `arcane:vapor_alembic` | Item → Chemical | `rotten_flesh` → `100 mB sulfur_dioxide` |
| `arcane:aether_condenser` | Chemical → Item | `200 mB oxygen` → `quartz` |
| `arcane:philosophers_coil` | Chemical → Chemical | `1 mB hydrogen` → `1 mB ethene` |
| `arcane:briny_distiller` | Fluid → Fluid | `1 B water` → `1 B brine` |

Each demo recipe is **distinguishable from any vanilla or Mekanism recipe**:
either the input/output combination doesn't exist elsewhere, or the rate
differs (e.g. bone → 6× bone meal versus the vanilla 3×). When you place an
input in the machine and the expected output appears, you know the custom
machine actually fired.

## End-to-end test checklist

For a comprehensive smoke test, after copying the scripts and restarting
the client:

1. **Registration succeeded** — open JEI / EMI and confirm all eight
   `arcane:*` blocks appear in the search and have a recipe category each.
2. **Energy / upgrades** — place any one machine, give it power, slot in a
   speed upgrade. The progress bar should speed up.
3. **Item → Item** — feed a bone to `bone_dust_mill`; you should get
   exactly 6 bone meal per craft.
4. **Item + Chemical → Item** — pipe sulfuric acid into the chemical tank
   of `essence_imbuer`, drop in redstone, get blaze powder.
5. **Combiner (Item + Item → Item)** — `runic_press` consumes one
   gold ingot **and** one emerald per ender pearl; it should refuse to
   start with only one of the two.
6. **Sawmill (chance secondary)** — `prismatic_cutter` should always output
   2 flint and occasionally an emerald (~1 in 4 cycles).
7. **Item → Chemical** — `vapor_alembic` builds up sulfur dioxide in its
   internal tank; pipe it out to a chemical tank to confirm.
8. **Chemical → Item** — fill `aether_condenser`'s chemical tank with
   oxygen (electrolytic separator → pipe → input slot), get quartz.
9. **Chemical → Chemical** — feed hydrogen into `philosophers_coil`,
   harvest ethene from the output tank.
10. **Fluid → Fluid** — bucket water in or pipe it into `briny_distiller`'s
    input fluid tank, harvest brine from the output tank.

If any step misbehaves: check `logs/kubejs/startup.txt` and
`logs/kubejs/server.txt` for errors, and confirm Mekanism's chemical /
fluid registry IDs match the ones used in the recipes (these are stable
as of Mekanism `10.7.19+`).

## Editing the demo

Both files are plain JavaScript — change names, energy values, recipes,
or whole shapes freely. The Java API and the KubeJS bridge expose the
exact same set of definition fields, so anything you can configure on the
Java builders is reachable from a script.

See the project's main `README.md` for the full KubeJS API reference and
the JSON field layouts each shape expects.
