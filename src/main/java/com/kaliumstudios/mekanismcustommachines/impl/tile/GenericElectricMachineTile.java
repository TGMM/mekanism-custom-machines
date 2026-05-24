package com.kaliumstudios.mekanismcustommachines.impl.tile;

import org.jetbrains.annotations.NotNull;

import com.kaliumstudios.mekanismcustommachines.api.definition.ItemToItemDefinition;

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

/**
 * Generic tile entity for Item→Item custom machines.
 * <p>
 * Extends {@link TileEntityElectricMachine} to inherit all standard Mekanism
 * machine behaviour: energy containers, upgrades (speed, energy, muffling),
 * side configuration, ejector, and computer integration.
 * <p>
 * One instance of this class is used per registered Item→Item machine. The
 * {@link RecipeTypeRegistryObject} it holds is the discriminator that routes
 * recipe lookups to the correct machine.
 */
public class GenericElectricMachineTile extends TileEntityElectricMachine {

    private final RecipeTypeRegistryObject<SingleRecipeInput, ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> recipeType;
    private final IBlockProvider machine;
    private final ItemToItemDefinition definition;

    // Lazily initialised to avoid allocating before the recipe type is resolved.
    private RVRecipeTypeWrapper<?, ItemStackToItemStackRecipe, ?> recipeViewerType;

    public GenericElectricMachineTile(
            BlockPos pos,
            BlockState state,
            IBlockProvider machine,
            RecipeTypeRegistryObject<SingleRecipeInput, ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> recipeType,
            ItemToItemDefinition definition) {
        super(machine, pos, state, definition.baseTicksRequired());
        this.recipeType = recipeType;
        this.machine = machine;
        this.definition = definition;
    }

    /**
     * Returns a {@link BlockEntitySupplier} that closes over the given machine
     * block provider, recipe type, and definition.
     */
    public static BlockEntitySupplier<GenericElectricMachineTile> factory(
            IBlockProvider machine,
            RecipeTypeRegistryObject<SingleRecipeInput, ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> recipeType,
            ItemToItemDefinition definition) {
        return (pos, state) -> new GenericElectricMachineTile(pos, state, machine, recipeType, definition);
    }

    @NotNull
    @Override
    public IMekanismRecipeTypeProvider<SingleRecipeInput, ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> getRecipeType() {
        return recipeType;
    }

    @Override
    public IRecipeViewerRecipeType<ItemStackToItemStackRecipe> recipeViewerType() {
        if (recipeViewerType == null) {
            var layout = definition.viewerLayout();
            recipeViewerType = new RVRecipeTypeWrapper<>(
                    recipeType,
                    ItemStackToItemStackRecipe.class,
                    layout.xOffset(),
                    layout.yOffset(),
                    layout.width(),
                    layout.height(),
                    machine);
        }
        return recipeViewerType;
    }
}
