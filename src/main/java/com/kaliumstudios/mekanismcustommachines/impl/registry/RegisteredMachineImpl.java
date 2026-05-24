package com.kaliumstudios.mekanismcustommachines.impl.registry;

import com.kaliumstudios.mekanismcustommachines.api.definition.MachineDefinition;
import com.kaliumstudios.mekanismcustommachines.api.handle.MachineHolders;
import com.kaliumstudios.mekanismcustommachines.api.handle.RegisteredMachine;

import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.RecipeTypeRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Internal implementation of {@link RegisteredMachine}.
 * Holds all resolved deferred holders for one custom machine.
 */
public final class RegisteredMachineImpl implements RegisteredMachine {

    private final MachineDefinition definition;
    private final Holders holders;

    public RegisteredMachineImpl(
            MachineDefinition definition,
            BlockRegistryObject<?, ?> block,
            TileEntityTypeRegistryObject<? extends TileEntityMekanism> tileEntityType,
            RecipeTypeRegistryObject<?, ?, ?> recipeType,
            ContainerTypeRegistryObject<?> containerType) {
        this.definition = definition;
        this.holders = new Holders(block, tileEntityType, recipeType, containerType);
    }

    @Override
    public ResourceLocation id() {
        return definition.id();
    }

    @Override
    public MachineDefinition definition() {
        return definition;
    }

    @Override
    public MachineHolders holders() {
        return holders;
    }

    private record Holders(
            BlockRegistryObject<?, ?> block,
            TileEntityTypeRegistryObject<? extends TileEntityMekanism> tileEntityType,
            RecipeTypeRegistryObject<?, ?, ?> rawRecipeType,
            ContainerTypeRegistryObject<?> containerType
    ) implements MachineHolders {

        @Override
        public BlockRegistryObject<?, ?> block() {
            return block;
        }

        @Override
        public TileEntityTypeRegistryObject<? extends TileEntityMekanism> tileEntityType() {
            return tileEntityType;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <INPUT extends RecipeInput,
                RECIPE extends MekanismRecipe<INPUT>,
                CACHE extends IInputRecipeCache>
        RecipeTypeRegistryObject<INPUT, RECIPE, CACHE> recipeType() {
            return (RecipeTypeRegistryObject<INPUT, RECIPE, CACHE>) rawRecipeType;
        }

        @Override
        public ContainerTypeRegistryObject<?> containerType() {
            return containerType;
        }
    }
}
