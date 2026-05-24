package com.kaliumstudios.mekanismcustommachines.impl.recipe;

import java.util.function.BiFunction;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.api.recipes.basic.BasicItemStackToChemicalRecipe;
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
 * Recipe class shared by all Item→Chemical custom machines (e.g. oxidizer,
 * pigment extractor analogs).
 * <p>
 * Extends {@link BasicItemStackToChemicalRecipe} which already accepts a
 * per-instance {@code RecipeType} via constructor — no mixin needed.
 */
public class GenericItemToChemicalRecipe extends BasicItemStackToChemicalRecipe {

    private final Holder<Item> machineItemHolder;
    private final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericItemToChemicalRecipe>> recipeSerializer;
    private final String machineName;

    public GenericItemToChemicalRecipe(
            ItemStackIngredient input,
            ChemicalStack output,
            RecipeTypeRegistryObject<SingleRecipeInput, ItemStackToChemicalRecipe, SingleItem<ItemStackToChemicalRecipe>> recipeType,
            ResourceLocation machineId,
            DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericItemToChemicalRecipe>> recipeSerializer) {
        super(input, output, recipeType.value());
        this.machineItemHolder = DeferredHolder.create(Registries.ITEM, machineId);
        this.recipeSerializer = recipeSerializer;
        this.machineName = machineId.getPath();
    }

    /**
     * Returns a factory suitable for passing to
     * {@link mekanism.common.recipe.serializer.MekanismRecipeSerializer#itemToChemical}.
     */
    public static BiFunction<ItemStackIngredient, ChemicalStack, GenericItemToChemicalRecipe> getFactory(
            RecipeTypeRegistryObject<SingleRecipeInput, ItemStackToChemicalRecipe, SingleItem<ItemStackToChemicalRecipe>> recipeType,
            ResourceLocation machineId,
            DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericItemToChemicalRecipe>> recipeSerializer) {
        return (input, output) -> new GenericItemToChemicalRecipe(input, output, recipeType, machineId, recipeSerializer);
    }

    @Override
    public RecipeSerializer<GenericItemToChemicalRecipe> getSerializer() {
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
