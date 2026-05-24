package com.kaliumstudios.mekanismcustommachines.impl.pipeline;

import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.RecipeTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Groups the six {@link DeferredRegister}s required to register a custom
 * machine, all bound to the same mod id and event bus.
 * <p>
 * A single {@code DeferredBundle} is created per namespace (mod id) on first
 * use and reused for all machines in that namespace. This ensures all registers
 * are subscribed to the event bus exactly once.
 */
public final class DeferredBundle {

    public final BlockDeferredRegister blocks;
    public final ItemDeferredRegister items;
    public final TileEntityTypeDeferredRegister tileEntities;
    public final RecipeTypeDeferredRegister recipeTypes;
    public final DeferredRegister<RecipeSerializer<?>> recipeSerializers;
    public final ContainerTypeDeferredRegister containerTypes;

    public DeferredBundle(String modId, IEventBus eventBus) {
        this.blocks = new BlockDeferredRegister(modId);
        this.items = new ItemDeferredRegister(modId);
        this.tileEntities = new TileEntityTypeDeferredRegister(modId);
        this.recipeTypes = new RecipeTypeDeferredRegister(modId);
        this.recipeSerializers = DeferredRegister.create(Registries.RECIPE_SERIALIZER, modId);
        this.containerTypes = new ContainerTypeDeferredRegister(modId);

        this.blocks.register(eventBus);
        this.items.register(eventBus);
        this.tileEntities.register(eventBus);
        this.recipeTypes.register(eventBus);
        this.recipeSerializers.register(eventBus);
        this.containerTypes.register(eventBus);
    }
}
