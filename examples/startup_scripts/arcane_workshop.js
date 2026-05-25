// ─────────────────────────────────────────────────────────────────────────
//  Arcane Workshop — example custom machines (startup phase)
// ─────────────────────────────────────────────────────────────────────────
//
//  Demonstrates every recipe shape Mekanism: Custom Machines supports.
//  Each machine is registered under the `arcane` namespace so it cannot
//  collide with vanilla, Mekanism, or any other pack content.
//
//  Once this script is loaded, eight machine blocks become available in
//  the creative inventory and in JEI / EMI.
//
//  Paired with `server_scripts/arcane_recipes.js` — see that file for the
//  per-machine recipes that close the end-to-end loop.

MekanismCustomMachines.registerMachines(event => {

    // ── Item → Item ───────────────────────────────────────────────────────
    //  A spinning mill of carved bone wheels. Grinds bones to dust six times
    //  more efficiently than crafting a single bone meal by hand.
    event.itemToItem('arcane:bone_dust_mill', m => {
        m.processName('Grinding')
        m.energy(200, 10000)
        m.ticks(100)
    })

    // ── Item + Chemical → Item ────────────────────────────────────────────
    //  Inscribed obsidian basin. Bathing a redstone mote in sulfuric acid
    //  scares it so badly it bursts into flame.
    event.itemChemicalToItem('arcane:essence_imbuer', m => {
        m.processName('Imbuing')
        m.energy(400, 20000)
        m.ticks(150)
        m.maxChemical(10000)
    })

    // ── Item + Item → Item (combiner) ─────────────────────────────────────
    //  A press wreathed in arcane sigils. Compresses two materials together
    //  until they fuse into something stranger than either alone.
    event.combiner('arcane:runic_press', m => {
        m.processName('Pressing')
        m.energy(300, 15000)
        m.ticks(200)
    })

    // ── Item → Item + chance secondary (sawmill-style) ────────────────────
    //  A faceted lens that splits gemstones along their cleavage planes.
    //  Always yields the dull main fragment; occasionally a richer shard
    //  spills out as well.
    event.sawmill('arcane:prismatic_cutter', m => {
        m.processName('Cleaving')
        m.energy(250, 12000)
        m.ticks(120)
    })

    // ── Item → Chemical ───────────────────────────────────────────────────
    //  An alembic that boils mundane matter into a noxious gas.
    event.itemToChemical('arcane:vapor_alembic', m => {
        m.processName('Distilling')
        m.energy(200, 10000)
        m.ticks(100)
        m.maxChemical(10000)
    })

    // ── Chemical → Item ───────────────────────────────────────────────────
    //  Pulls scattered aether out of a sealed chamber and condenses it into
    //  solid crystal.
    event.chemicalToItem('arcane:aether_condenser', m => {
        m.processName('Crystallising')
        m.energy(400, 20000)
        m.ticks(180)
        m.maxChemical(10000)
    })

    // ── Chemical → Chemical ───────────────────────────────────────────────
    //  A coiled glass spiral named after a philosopher who didn't believe in
    //  atoms. Transmutes one gas into another.
    event.chemicalToChemical('arcane:philosophers_coil', m => {
        m.processName('Transmuting')
        m.energy(300, 15000)
        m.ticks(140)
        m.maxChemical(10000)
    })

    // ── Fluid → Fluid ─────────────────────────────────────────────────────
    //  A stone basin that draws latent salt out of plain water.
    event.fluidToFluid('arcane:briny_distiller', m => {
        m.processName('Boiling')
        m.energy(500, 25000)
        m.ticks(200)
        m.maxFluid(16000)
    })
})
