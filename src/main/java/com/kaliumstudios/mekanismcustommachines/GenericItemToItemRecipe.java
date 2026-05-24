package com.kaliumstudios.mekanismcustommachines;

import java.util.function.BiFunction;

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

public class GenericItemToItemRecipe extends BasicItemStackToItemStackRecipe implements IBasicItemStackOutput {
    private final Holder<Item> machineItemHolder;
    private final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericItemToItemRecipe>> recipeSerializer;
    private final String machineName;

    public GenericItemToItemRecipe(ItemStackIngredient input, ItemStack output,
            RecipeTypeRegistryObject<SingleRecipeInput, ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> recipeType,
            String machineName,
            DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericItemToItemRecipe>> recipeSerializer) {
        super(input, output, recipeType.value());

        machineItemHolder = DeferredHolder.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(MekanismCustomMachines.MODID,
                        machineName));
        this.recipeSerializer = recipeSerializer;
        this.machineName = machineName;
    }

    public static BiFunction<ItemStackIngredient, ItemStack, GenericItemToItemRecipe> getFactory(
            RecipeTypeRegistryObject<SingleRecipeInput, ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> recipeType,
            String machineName,
            DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericItemToItemRecipe>> recipeSerializer) {
        return (input, output) -> new GenericItemToItemRecipe(input, output, recipeType, machineName,
                recipeSerializer);
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
