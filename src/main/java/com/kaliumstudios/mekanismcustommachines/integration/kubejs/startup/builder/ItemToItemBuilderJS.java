package com.kaliumstudios.mekanismcustommachines.integration.kubejs.startup.builder;

import com.kaliumstudios.mekanismcustommachines.api.definition.ItemToItemDefinition;
import com.kaliumstudios.mekanismcustommachines.api.energy.EnergyProfile;
import com.kaliumstudios.mekanismcustommachines.api.layout.RecipeViewerLayout;

import net.minecraft.resources.ResourceLocation;

/**
 * KubeJS-friendly builder for {@link ItemToItemDefinition}.
 * <p>
 * Wraps the Java {@code ItemToItemDefinition.Builder} so scripts can configure
 * machines using JS-native values:
 * <pre>{@code
 * event.itemToItem('mymod:my_machine', m => {
 *     m.processName('Processing')
 *     m.energy(200, 10000)
 *     m.ticks(200)
 * })
 * }</pre>
 */
public final class ItemToItemBuilderJS {

    private final ItemToItemDefinition.Builder delegate;

    public ItemToItemBuilderJS(ResourceLocation id) {
        this.delegate = ItemToItemDefinition.builder(id);
    }

    public ItemToItemBuilderJS processName(String name) {
        delegate.processName(name);
        return this;
    }

    public ItemToItemBuilderJS energy(long usageFE, long storageFE) {
        delegate.energy(usageFE, storageFE);
        return this;
    }

    public ItemToItemBuilderJS energyUsage(long usageFE) {
        delegate.energy(EnergyProfile.of(usageFE, 10_000L));
        return this;
    }

    public ItemToItemBuilderJS ticks(int ticks) {
        delegate.ticks(ticks);
        return this;
    }

    public ItemToItemBuilderJS viewerLayout(RecipeViewerLayout layout) {
        delegate.viewerLayout(layout);
        return this;
    }

    /** Builds the immutable definition. Called by the dispatcher after the JS configurator returns. */
    public ItemToItemDefinition build() {
        return delegate.build();
    }
}
