package com.kaliumstudios.mekanismcustommachines.integration.kubejs.startup.builder;

import com.kaliumstudios.mekanismcustommachines.api.definition.CombinerDefinition;
import com.kaliumstudios.mekanismcustommachines.api.energy.EnergyProfile;
import com.kaliumstudios.mekanismcustommachines.api.layout.RecipeViewerLayout;

import net.minecraft.resources.ResourceLocation;

/** KubeJS-friendly builder for {@link CombinerDefinition}. */
public final class CombinerBuilderJS {

    private final CombinerDefinition.Builder delegate;

    public CombinerBuilderJS(ResourceLocation id) {
        this.delegate = CombinerDefinition.builder(id);
    }

    public CombinerBuilderJS processName(String name) {
        delegate.processName(name);
        return this;
    }

    public CombinerBuilderJS energy(long usageFE, long storageFE) {
        delegate.energy(usageFE, storageFE);
        return this;
    }

    public CombinerBuilderJS energyUsage(long usageFE) {
        delegate.energy(EnergyProfile.of(usageFE, 10_000L));
        return this;
    }

    public CombinerBuilderJS ticks(int ticks) {
        delegate.ticks(ticks);
        return this;
    }

    public CombinerBuilderJS viewerLayout(RecipeViewerLayout layout) {
        delegate.viewerLayout(layout);
        return this;
    }

    public CombinerDefinition build() {
        return delegate.build();
    }
}
