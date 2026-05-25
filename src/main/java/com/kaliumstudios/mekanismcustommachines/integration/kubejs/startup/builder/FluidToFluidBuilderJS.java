package com.kaliumstudios.mekanismcustommachines.integration.kubejs.startup.builder;

import com.kaliumstudios.mekanismcustommachines.api.definition.FluidToFluidDefinition;
import com.kaliumstudios.mekanismcustommachines.api.energy.EnergyProfile;
import com.kaliumstudios.mekanismcustommachines.api.layout.RecipeViewerLayout;

import net.minecraft.resources.ResourceLocation;

/** KubeJS-friendly builder for {@link FluidToFluidDefinition}. */
public final class FluidToFluidBuilderJS {

    private final FluidToFluidDefinition.Builder delegate;

    public FluidToFluidBuilderJS(ResourceLocation id) {
        this.delegate = FluidToFluidDefinition.builder(id);
    }

    public FluidToFluidBuilderJS processName(String name) {
        delegate.processName(name);
        return this;
    }

    public FluidToFluidBuilderJS energy(long usageFE, long storageFE) {
        delegate.energy(usageFE, storageFE);
        return this;
    }

    public FluidToFluidBuilderJS energyUsage(long usageFE) {
        delegate.energy(EnergyProfile.of(usageFE, 10_000L));
        return this;
    }

    public FluidToFluidBuilderJS ticks(int ticks) {
        delegate.ticks(ticks);
        return this;
    }

    public FluidToFluidBuilderJS maxFluid(int capacity) {
        delegate.maxFluid(capacity);
        return this;
    }

    public FluidToFluidBuilderJS viewerLayout(RecipeViewerLayout layout) {
        delegate.viewerLayout(layout);
        return this;
    }

    public FluidToFluidDefinition build() {
        return delegate.build();
    }
}
