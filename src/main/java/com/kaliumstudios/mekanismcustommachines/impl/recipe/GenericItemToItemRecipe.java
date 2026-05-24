package com.kaliumstudios.mekanismcustommachines.impl.recipe;

import java.util.function.BiFunction;

import com.kaliumstudios.mekanismcustommachines.api.MekanismCustomMachinesAPI;

import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.basic.BasicItemStackToItemStackRecipe;
import mekanism.api.recipes.basic.IBasicItemStackOutput;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleItem;
import mekanism.common.registration.impl.RecipeTypeRegistryObject;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Recipe class shared by all Item→Item custom machines.
 * <p>
 * One class covers every registered Item→Item machine; the
 * {@link mekanism.common.recipe.MekanismRecipeType} instance (one per machine)
 * is the discriminator that routes recipes to the correct machine.
 */
public class GenericItemToItemRecipe extends BasicItemStackToItemStackRecipe implements IBasicItemStackOutput {

    private final Holder<Item> machineItemHolder;
    private final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericItemToItemRecipe>> recipeSerializer;
    private final String machineName;

    public GenericItemToItemRecipe(
            ItemStackIngredient input,
            ItemStack output,
            RecipeTypeRegistryObject<SingleRecipeInput, ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> recipeType,
            ResourceLocation machineId,
            DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericItemToItemRecipe>> recipeSerializer) {
        super(input, output, recipeType.value());
        this.machineItemHolder = DeferredHolder.create(Registries.ITEM, machineId);
        this.recipeSerializer = recipeSerializer;
        this.machineName = machineId.getPath();
    }

    /**
     * Returns a factory {@link BiFunction} suitable for passing to
     * {@link mekanism.common.recipe.serializer.MekanismRecipeSerializer#itemToItem}.
     */
    public static BiFunction<ItemStackIngredient, ItemStack, GenericItemToItemRecipe> getFactory(
            RecipeTypeRegistryObject<SingleRecipeInput, ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> recipeType,
            ResourceLocation machineId,
            DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericItemToItemRecipe>> recipeSerializer) {
        return (input, output) -> new GenericItemToItemRecipe(input, output, recipeType, machineId, recipeSerializer);
    }

    @Override
    public RecipeSerializer<GenericItemToItemRecipe> getSerializer() {
        return recipeSerializer.value();
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
