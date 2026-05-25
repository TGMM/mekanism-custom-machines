package com.kaliumstudios.mekanismcustommachines.integration.kubejs.startup.builder;

import com.kaliumstudios.mekanismcustommachines.api.definition.SawmillDefinition;
import com.kaliumstudios.mekanismcustommachines.api.energy.EnergyProfile;
import com.kaliumstudios.mekanismcustommachines.api.layout.RecipeViewerLayout;

import net.minecraft.resources.ResourceLocation;

/** KubeJS-friendly builder for {@link SawmillDefinition}. */
public final class SawmillBuilderJS {

    private final SawmillDefinition.Builder delegate;

    public SawmillBuilderJS(ResourceLocation id) {
        this.delegate = SawmillDefinition.builder(id);
    }

    public SawmillBuilderJS processName(String name) {
        delegate.processName(name);
        return this;
    }

    public SawmillBuilderJS energy(long usageFE, long storageFE) {
        delegate.energy(usageFE, storageFE);
        return this;
    }

    public SawmillBuilderJS energyUsage(long usageFE) {
        delegate.energy(EnergyProfile.of(usageFE, 10_000L));
        return this;
    }

    public SawmillBuilderJS ticks(int ticks) {
        delegate.ticks(ticks);
        return this;
    }

    public SawmillBuilderJS viewerLayout(RecipeViewerLayout layout) {
        delegate.viewerLayout(layout);
        return this;
    }

    public SawmillDefinition build() {
        return delegate.build();
    }
}
