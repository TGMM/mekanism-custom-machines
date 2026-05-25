package com.kaliumstudios.mekanismcustommachines.integration.kubejs.startup.builder;

import com.kaliumstudios.mekanismcustommachines.api.definition.ItemToChemicalDefinition;
import com.kaliumstudios.mekanismcustommachines.api.energy.EnergyProfile;
import com.kaliumstudios.mekanismcustommachines.api.layout.RecipeViewerLayout;

import net.minecraft.resources.ResourceLocation;

/** KubeJS-friendly builder for {@link ItemToChemicalDefinition}. */
public final class ItemToChemicalBuilderJS {

    private final ItemToChemicalDefinition.Builder delegate;

    public ItemToChemicalBuilderJS(ResourceLocation id) {
        this.delegate = ItemToChemicalDefinition.builder(id);
    }

    public ItemToChemicalBuilderJS processName(String name) {
        delegate.processName(name);
        return this;
    }

    public ItemToChemicalBuilderJS energy(long usageFE, long storageFE) {
        delegate.energy(usageFE, storageFE);
        return this;
    }

    public ItemToChemicalBuilderJS energyUsage(long usageFE) {
        delegate.energy(EnergyProfile.of(usageFE, 10_000L));
        return this;
    }

    public ItemToChemicalBuilderJS ticks(int ticks) {
        delegate.ticks(ticks);
        return this;
    }

    public ItemToChemicalBuilderJS maxChemical(long capacity) {
        delegate.maxChemical(capacity);
        return this;
    }

    public ItemToChemicalBuilderJS viewerLayout(RecipeViewerLayout layout) {
        delegate.viewerLayout(layout);
        return this;
    }

    public ItemToChemicalDefinition build() {
        return delegate.build();
    }
}
