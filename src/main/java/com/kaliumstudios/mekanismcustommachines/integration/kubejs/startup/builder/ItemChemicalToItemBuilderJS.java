package com.kaliumstudios.mekanismcustommachines.integration.kubejs.startup.builder;

import com.kaliumstudios.mekanismcustommachines.api.definition.ItemChemicalToItemDefinition;
import com.kaliumstudios.mekanismcustommachines.api.energy.EnergyProfile;
import com.kaliumstudios.mekanismcustommachines.api.layout.RecipeViewerLayout;

import net.minecraft.resources.ResourceLocation;

/** KubeJS-friendly builder for {@link ItemChemicalToItemDefinition}. */
public final class ItemChemicalToItemBuilderJS {

    private final ItemChemicalToItemDefinition.Builder delegate;

    public ItemChemicalToItemBuilderJS(ResourceLocation id) {
        this.delegate = ItemChemicalToItemDefinition.builder(id);
    }

    public ItemChemicalToItemBuilderJS processName(String name) {
        delegate.processName(name);
        return this;
    }

    public ItemChemicalToItemBuilderJS energy(long usageFE, long storageFE) {
        delegate.energy(usageFE, storageFE);
        return this;
    }

    public ItemChemicalToItemBuilderJS energyUsage(long usageFE) {
        delegate.energy(EnergyProfile.of(usageFE, 10_000L));
        return this;
    }

    public ItemChemicalToItemBuilderJS ticks(int ticks) {
        delegate.ticks(ticks);
        return this;
    }

    public ItemChemicalToItemBuilderJS maxChemical(long capacity) {
        delegate.maxChemical(capacity);
        return this;
    }

    public ItemChemicalToItemBuilderJS viewerLayout(RecipeViewerLayout layout) {
        delegate.viewerLayout(layout);
        return this;
    }

    public ItemChemicalToItemDefinition build() {
        return delegate.build();
    }
}
