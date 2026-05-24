package com.kaliumstudios.mekanismcustommachines;

import java.util.function.BiFunction;
import org.jetbrains.annotations.NotNull;

import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RVRecipeTypeWrapper;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleItem;
import mekanism.common.registration.impl.RecipeTypeRegistryObject;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockState;

public class GenericMachineTileEntity extends TileEntityElectricMachine {
    RecipeTypeRegistryObject<SingleRecipeInput, ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> recipeType;
    IBlockProvider machine;
    RVRecipeTypeWrapper<?, ItemStackToItemStackRecipe, ?> recipeViewerType = null;

    public GenericMachineTileEntity(BlockPos pos, BlockState state, IBlockProvider machine,
            RecipeTypeRegistryObject<SingleRecipeInput, ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> recipeType) {
        super(machine, pos, state, BASE_TICKS_REQUIRED);

        this.recipeType = recipeType;
        this.machine = machine;
    }

    public static BlockEntitySupplier<GenericMachineTileEntity> getFactory(
            IBlockProvider machine,
            RecipeTypeRegistryObject<SingleRecipeInput, ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> recipeType) {

        return (pos, state) -> new GenericMachineTileEntity(pos, state, machine, recipeType);
    }

    @NotNull
    @Override
    public IMekanismRecipeTypeProvider<SingleRecipeInput, ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> getRecipeType() {
        return recipeType;
    }

    @Override
    public IRecipeViewerRecipeType<ItemStackToItemStackRecipe> recipeViewerType() {
        if (recipeViewerType == null) {
            recipeViewerType = new RVRecipeTypeWrapper<>(
                    recipeType,
                    ItemStackToItemStackRecipe.class, -28,
                    -16, 144,
                    54,
                    machine);
        }

        return recipeViewerType;
    }
}
