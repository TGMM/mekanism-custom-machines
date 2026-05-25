package com.kaliumstudios.mekanismcustommachines.integration.kubejs.startup.builder;

import com.kaliumstudios.mekanismcustommachines.api.definition.ChemicalToItemDefinition;
import com.kaliumstudios.mekanismcustommachines.api.energy.EnergyProfile;
import com.kaliumstudios.mekanismcustommachines.api.layout.RecipeViewerLayout;

import net.minecraft.resources.ResourceLocation;

/** KubeJS-friendly builder for {@link ChemicalToItemDefinition}. */
public final class ChemicalToItemBuilderJS {

    private final ChemicalToItemDefinition.Builder delegate;

    public ChemicalToItemBuilderJS(ResourceLocation id) {
        this.delegate = ChemicalToItemDefinition.builder(id);
    }

    public ChemicalToItemBuilderJS processName(String name) {
        delegate.processName(name);
        return this;
    }

    public ChemicalToItemBuilderJS energy(long usageFE, long storageFE) {
        delegate.energy(usageFE, storageFE);
        return this;
    }

    public ChemicalToItemBuilderJS energyUsage(long usageFE) {
        delegate.energy(EnergyProfile.of(usageFE, 10_000L));
        return this;
    }

    public ChemicalToItemBuilderJS ticks(int ticks) {
        delegate.ticks(ticks);
        return this;
    }

    public ChemicalToItemBuilderJS maxChemical(long capacity) {
        delegate.maxChemical(capacity);
        return this;
    }

    public ChemicalToItemBuilderJS viewerLayout(RecipeViewerLayout layout) {
        delegate.viewerLayout(layout);
        return this;
    }

    public ChemicalToItemDefinition build() {
        return delegate.build();
    }
}
