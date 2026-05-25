package com.kaliumstudios.mekanismcustommachines.integration.kubejs.startup;

import java.util.function.Consumer;

import com.kaliumstudios.mekanismcustommachines.api.MachineRegistry;
import com.kaliumstudios.mekanismcustommachines.api.event.RegisterCustomMachinesEvent;
import com.kaliumstudios.mekanismcustommachines.integration.kubejs.startup.builder.ChemicalToChemicalBuilderJS;
import com.kaliumstudios.mekanismcustommachines.integration.kubejs.startup.builder.ChemicalToItemBuilderJS;
import com.kaliumstudios.mekanismcustommachines.integration.kubejs.startup.builder.CombinerBuilderJS;
import com.kaliumstudios.mekanismcustommachines.integration.kubejs.startup.builder.FluidToFluidBuilderJS;
import com.kaliumstudios.mekanismcustommachines.integration.kubejs.startup.builder.ItemChemicalToItemBuilderJS;
import com.kaliumstudios.mekanismcustommachines.integration.kubejs.startup.builder.ItemToChemicalBuilderJS;
import com.kaliumstudios.mekanismcustommachines.integration.kubejs.startup.builder.ItemToItemBuilderJS;
import com.kaliumstudios.mekanismcustommachines.integration.kubejs.startup.builder.SawmillBuilderJS;

import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.resources.ResourceLocation;

/**
 * KubeJS event class scripts receive when handling
 * {@code MekanismCustomMachines.registerMachines}. Each method registers a
 * single machine of a given recipe shape.
 * <p>
 * The configurator lambda receives a JS-friendly builder; once it returns,
 * the assembled definition is forwarded to
 * {@link MachineRegistry#register(com.kaliumstudios.mekanismcustommachines.api.definition.MachineDefinition)}.
 * <p>
 * Example usage from a startup script:
 * <pre>{@code
 * MekanismCustomMachines.registerMachines(event => {
 *     event.itemToItem('mymod:my_enricher', m => {
 *         m.processName('Enriching')
 *         m.energy(200, 10000)
 *         m.ticks(200)
 *     })
 *
 *     event.itemChemicalToItem('mymod:my_injector', m => {
 *         m.energy(400, 20000)
 *         m.maxChemical(10000)
 *     })
 * })
 * }</pre>
 */
public class RegisterMachinesKubeEvent implements KubeEvent {

    private final RegisterCustomMachinesEvent registerEvent;

    public RegisterMachinesKubeEvent(RegisterCustomMachinesEvent registerEvent) {
        this.registerEvent = registerEvent;
    }

    // ── Shape dispatch methods ────────────────────────────────────────────

    public void itemToItem(String id, Consumer<ItemToItemBuilderJS> configurator) {
        ItemToItemBuilderJS builder = new ItemToItemBuilderJS(ResourceLocation.parse(id));
        configurator.accept(builder);
        registerEvent.register(builder.build());
    }

    public void itemChemicalToItem(String id, Consumer<ItemChemicalToItemBuilderJS> configurator) {
        ItemChemicalToItemBuilderJS builder = new ItemChemicalToItemBuilderJS(ResourceLocation.parse(id));
        configurator.accept(builder);
        registerEvent.register(builder.build());
    }

    public void combiner(String id, Consumer<CombinerBuilderJS> configurator) {
        CombinerBuilderJS builder = new CombinerBuilderJS(ResourceLocation.parse(id));
        configurator.accept(builder);
        registerEvent.register(builder.build());
    }

    public void sawmill(String id, Consumer<SawmillBuilderJS> configurator) {
        SawmillBuilderJS builder = new SawmillBuilderJS(ResourceLocation.parse(id));
        configurator.accept(builder);
        registerEvent.register(builder.build());
    }

    public void itemToChemical(String id, Consumer<ItemToChemicalBuilderJS> configurator) {
        ItemToChemicalBuilderJS builder = new ItemToChemicalBuilderJS(ResourceLocation.parse(id));
        configurator.accept(builder);
        registerEvent.register(builder.build());
    }

    public void chemicalToItem(String id, Consumer<ChemicalToItemBuilderJS> configurator) {
        ChemicalToItemBuilderJS builder = new ChemicalToItemBuilderJS(ResourceLocation.parse(id));
        configurator.accept(builder);
        registerEvent.register(builder.build());
    }

    public void chemicalToChemical(String id, Consumer<ChemicalToChemicalBuilderJS> configurator) {
        ChemicalToChemicalBuilderJS builder = new ChemicalToChemicalBuilderJS(ResourceLocation.parse(id));
        configurator.accept(builder);
        registerEvent.register(builder.build());
    }

    public void fluidToFluid(String id, Consumer<FluidToFluidBuilderJS> configurator) {
        FluidToFluidBuilderJS builder = new FluidToFluidBuilderJS(ResourceLocation.parse(id));
        configurator.accept(builder);
        registerEvent.register(builder.build());
    }
}
