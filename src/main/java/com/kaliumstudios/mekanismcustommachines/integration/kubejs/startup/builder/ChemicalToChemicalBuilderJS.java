package com.kaliumstudios.mekanismcustommachines.integration.kubejs.startup.builder;

import com.kaliumstudios.mekanismcustommachines.api.definition.ChemicalToChemicalDefinition;
import com.kaliumstudios.mekanismcustommachines.api.energy.EnergyProfile;
import com.kaliumstudios.mekanismcustommachines.api.layout.RecipeViewerLayout;

import net.minecraft.resources.ResourceLocation;

/** KubeJS-friendly builder for {@link ChemicalToChemicalDefinition}. */
public final class ChemicalToChemicalBuilderJS {

    private final ChemicalToChemicalDefinition.Builder delegate;

    public ChemicalToChemicalBuilderJS(ResourceLocation id) {
        this.delegate = ChemicalToChemicalDefinition.builder(id);
    }

    public ChemicalToChemicalBuilderJS processName(String name) {
        delegate.processName(name);
        return this;
    }

    public ChemicalToChemicalBuilderJS energy(long usageFE, long storageFE) {
        delegate.energy(usageFE, storageFE);
        return this;
    }

    public ChemicalToChemicalBuilderJS energyUsage(long usageFE) {
        delegate.energy(EnergyProfile.of(usageFE, 10_000L));
        return this;
    }

    public ChemicalToChemicalBuilderJS ticks(int ticks) {
        delegate.ticks(ticks);
        return this;
    }

    public ChemicalToChemicalBuilderJS maxChemical(long capacity) {
        delegate.maxChemical(capacity);
        return this;
    }

    public ChemicalToChemicalBuilderJS viewerLayout(RecipeViewerLayout layout) {
        delegate.viewerLayout(layout);
        return this;
    }

    public ChemicalToChemicalDefinition build() {
        return delegate.build();
    }
}
