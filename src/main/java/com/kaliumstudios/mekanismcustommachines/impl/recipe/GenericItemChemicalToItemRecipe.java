package com.kaliumstudios.mekanismcustommachines.impl.recipe;

import com.mojang.datafixers.util.Function4;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.basic.BasicItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.ItemChemical;
import mekanism.common.registration.impl.RecipeTypeRegistryObject;
import mekanism.api.recipes.vanilla_input.SingleItemChemicalRecipeInput;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Recipe class shared by all Item+Chemical→Item custom machines.
 * <p>
 * One class covers every registered machine of this shape; the
 * {@link mekanism.common.recipe.MekanismRecipeType} instance (one per machine)
 * is the discriminator that routes recipes to the correct machine.
 */
public class GenericItemChemicalToItemRecipe extends BasicItemStackChemicalToItemStackRecipe {

    private final Holder<Item> machineItemHolder;
    private final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericItemChemicalToItemRecipe>> recipeSerializer;
    private final String machineName;

    public GenericItemChemicalToItemRecipe(
            ItemStackIngredient itemInput,
            ChemicalStackIngredient chemicalInput,
            ItemStack output,
            boolean perTickUsage,
            RecipeTypeRegistryObject<SingleItemChemicalRecipeInput, ItemStackChemicalToItemStackRecipe, ItemChemical<ItemStackChemicalToItemStackRecipe>> recipeType,
            ResourceLocation machineId,
            DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericItemChemicalToItemRecipe>> recipeSerializer) {
        super(itemInput, chemicalInput, output, perTickUsage, recipeType.value());
        this.machineItemHolder = DeferredHolder.create(Registries.ITEM, machineId);
        this.recipeSerializer = recipeSerializer;
        this.machineName = machineId.getPath();
    }

    /**
     * Returns a factory {@link Function4} suitable for passing to
     * {@link mekanism.common.recipe.serializer.MekanismRecipeSerializer#itemChemicalToItem}.
     */
    public static Function4<ItemStackIngredient, ChemicalStackIngredient, ItemStack, Boolean, GenericItemChemicalToItemRecipe> getFactory(
            RecipeTypeRegistryObject<SingleItemChemicalRecipeInput, ItemStackChemicalToItemStackRecipe, ItemChemical<ItemStackChemicalToItemStackRecipe>> recipeType,
            ResourceLocation machineId,
            DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericItemChemicalToItemRecipe>> recipeSerializer) {
        return (itemInput, chemicalInput, output, perTickUsage) ->
                new GenericItemChemicalToItemRecipe(itemInput, chemicalInput, output, perTickUsage, recipeType, machineId, recipeSerializer);
    }

    @Override
    public RecipeSerializer<GenericItemChemicalToItemRecipe> getSerializer() {
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
