// ─────────────────────────────────────────────────────────────────────────
//  Arcane Workshop — example recipes (server reload phase)
// ─────────────────────────────────────────────────────────────────────────
//
//  One distinctive recipe per machine registered in
//  `startup_scripts/arcane_workshop.js`. Each output is intentionally either
//  impossible by vanilla means or produced at a different rate, so a
//  successful craft proves the custom machine actually ran the recipe.
//
//  All field names match Mekanism's own SerializationConstants exactly —
//  see the JSON field reference in README.md.

ServerEvents.recipes(event => {

    // ── arcane:bone_dust_mill ─────────────────────────────────────────────
    //  Bone → 6× bone meal (vanilla crafting yields only 3).
    event.recipes.arcane.bone_dust_mill({
        input:  { item: 'minecraft:bone' },
        output: { id: 'minecraft:bone_meal', count: 6 }
    })

    // ── arcane:essence_imbuer ─────────────────────────────────────────────
    //  Redstone bathed in sulfuric acid combusts into blaze powder.
    //  per_tick_usage = false → the full 100 mB is consumed in one go.
    event.recipes.arcane.essence_imbuer({
        item_input:     { item: 'minecraft:redstone' },
        chemical_input: { chemical: 'mekanism:sulfuric_acid', amount: 100 },
        output:         { id: 'minecraft:blaze_powder' },
        per_tick_usage: false
    })

    // ── arcane:runic_press ────────────────────────────────────────────────
    //  Gold + emerald, pressed under runic seals, fuse into an ender pearl.
    event.recipes.arcane.runic_press({
        main_input:  { item: 'minecraft:gold_ingot' },
        extra_input: { item: 'minecraft:emerald' },
        output:      { id: 'minecraft:ender_pearl' }
    })

    // ── arcane:prismatic_cutter ───────────────────────────────────────────
    //  Diamond → 2× flint, plus a 25 % chance of recovering an emerald
    //  from the residual shavings.
    event.recipes.arcane.prismatic_cutter({
        input:            { item: 'minecraft:diamond' },
        main_output:      { id: 'minecraft:flint', count: 2 },
        secondary_output: { id: 'minecraft:emerald' },
        secondary_chance: 0.25
    })

    // ── arcane:vapor_alembic ──────────────────────────────────────────────
    //  Rotten flesh distilled into 100 mB of sulfur dioxide. Smells bad.
    event.recipes.arcane.vapor_alembic({
        input:  { item: 'minecraft:rotten_flesh' },
        output: { id: 'mekanism:sulfur_dioxide', amount: 100 }
    })

    // ── arcane:aether_condenser ───────────────────────────────────────────
    //  200 mB of oxygen condensed into a chunk of quartz crystal.
    event.recipes.arcane.aether_condenser({
        input:  { chemical: 'mekanism:oxygen', amount: 200 },
        output: { id: 'minecraft:quartz' }
    })

    // ── arcane:philosophers_coil ──────────────────────────────────────────
    //  Pure hydrogen, transmuted through arcane spirals, becomes ethene.
    //  (Not chemically valid. That's the point — it's the philosophers' coil.)
    event.recipes.arcane.philosophers_coil({
        input:  { chemical: 'mekanism:hydrogen', amount: 1 },
        output: { id: 'mekanism:ethene', amount: 1 }
    })

    // ── arcane:briny_distiller ────────────────────────────────────────────
    //  A bucket of water → a bucket of brine. The latent salt is coaxed out
    //  of solution by the basin's slow boil.
    event.recipes.arcane.briny_distiller({
        input:  { fluid: 'minecraft:water', amount: 1000 },
        output: { id: 'mekanism:brine', amount: 1000 }
    })
})
