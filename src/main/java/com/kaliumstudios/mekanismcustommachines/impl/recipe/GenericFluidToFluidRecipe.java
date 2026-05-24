package com.kaliumstudios.mekanismcustommachines.impl.recipe;

import java.util.function.BiFunction;

import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.basic.BasicFluidToFluidRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.vanilla_input.SingleFluidRecipeInput;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleFluid;
import mekanism.common.registration.impl.RecipeTypeRegistryObject;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Recipe class shared by all Fluid→Fluid custom machines.
 * <p>
 * Extends {@link BasicFluidToFluidRecipe}.
 * {@link FluidToFluidRecipe#getType()} is normally {@code final} and hardcoded;
 * {@code CustomRecipeTypedMixin} overrides it so we can bind a per-machine
 * recipe type via {@link CustomRecipeTyped} in the constructor.
 */
public class GenericFluidToFluidRecipe extends BasicFluidToFluidRecipe {

    private final Holder<Item> machineItemHolder;
    private final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericFluidToFluidRecipe>> recipeSerializer;
    private final String machineName;

    public GenericFluidToFluidRecipe(
            FluidStackIngredient input,
            FluidStack output,
            RecipeTypeRegistryObject<SingleFluidRecipeInput, FluidToFluidRecipe, SingleFluid<FluidToFluidRecipe>> recipeType,
            ResourceLocation machineId,
            DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericFluidToFluidRecipe>> recipeSerializer) {
        super(input, output);
        this.machineItemHolder = DeferredHolder.create(Registries.ITEM, machineId);
        this.recipeSerializer = recipeSerializer;
        this.machineName = machineId.getPath();
        ((CustomRecipeTyped) (Object) this).mekcm$setRecipeType(recipeType.value());
    }

    /**
     * Returns a factory suitable for passing to
     * {@link mekanism.common.recipe.serializer.MekanismRecipeSerializer#fluidToFluid}.
     */
    public static BiFunction<FluidStackIngredient, FluidStack, GenericFluidToFluidRecipe> getFactory(
            RecipeTypeRegistryObject<SingleFluidRecipeInput, FluidToFluidRecipe, SingleFluid<FluidToFluidRecipe>> recipeType,
            ResourceLocation machineId,
            DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericFluidToFluidRecipe>> recipeSerializer) {
        return (input, output) -> new GenericFluidToFluidRecipe(input, output, recipeType, machineId, recipeSerializer);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public RecipeSerializer<BasicFluidToFluidRecipe> getSerializer() {
        return (RecipeSerializer) recipeSerializer.value();
    }

    @Override
    public String getGroup() {
        return machineName;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(machineItemHolder);
    }
}
