package com.kaliumstudios.mekanismcustommachines.impl.recipe;

import java.util.function.BiFunction;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.api.recipes.basic.BasicChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.vanilla_input.SingleChemicalRecipeInput;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleChemical;
import mekanism.common.registration.impl.RecipeTypeRegistryObject;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Recipe class shared by all Chemical→Chemical custom machines.
 * <p>
 * Extends {@link BasicChemicalToChemicalRecipe} which already accepts a
 * per-instance {@code RecipeType} via constructor — no mixin needed.
 */
public class GenericChemicalToChemicalRecipe extends BasicChemicalToChemicalRecipe {

    private final Holder<Item> machineItemHolder;
    private final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericChemicalToChemicalRecipe>> recipeSerializer;
    private final String machineName;

    public GenericChemicalToChemicalRecipe(
            ChemicalStackIngredient input,
            ChemicalStack output,
            RecipeTypeRegistryObject<SingleChemicalRecipeInput, ChemicalToChemicalRecipe, SingleChemical<ChemicalToChemicalRecipe>> recipeType,
            ResourceLocation machineId,
            DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericChemicalToChemicalRecipe>> recipeSerializer) {
        super(input, output, recipeType.value());
        this.machineItemHolder = DeferredHolder.create(Registries.ITEM, machineId);
        this.recipeSerializer = recipeSerializer;
        this.machineName = machineId.getPath();
    }

    /**
     * Returns a factory suitable for passing to
     * {@link mekanism.common.recipe.serializer.MekanismRecipeSerializer#chemicalToChemical}.
     */
    public static BiFunction<ChemicalStackIngredient, ChemicalStack, GenericChemicalToChemicalRecipe> getFactory(
            RecipeTypeRegistryObject<SingleChemicalRecipeInput, ChemicalToChemicalRecipe, SingleChemical<ChemicalToChemicalRecipe>> recipeType,
            ResourceLocation machineId,
            DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericChemicalToChemicalRecipe>> recipeSerializer) {
        return (input, output) -> new GenericChemicalToChemicalRecipe(input, output, recipeType, machineId, recipeSerializer);
    }

    @Override
    public RecipeSerializer<GenericChemicalToChemicalRecipe> getSerializer() {
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
