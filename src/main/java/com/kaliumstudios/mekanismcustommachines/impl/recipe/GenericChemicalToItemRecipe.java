package com.kaliumstudios.mekanismcustommachines.impl.recipe;

import java.util.function.BiFunction;

import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.basic.BasicChemicalCrystallizerRecipe;
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
 * Recipe class shared by all Chemical→Item custom machines (crystallizer
 * analogs).
 * <p>
 * Extends {@link BasicChemicalCrystallizerRecipe}.
 * {@link ChemicalCrystallizerRecipe#getType()} is normally {@code final};
 * {@code CustomRecipeTypedMixin} overrides it so we can bind a per-machine
 * recipe type via {@link CustomRecipeTyped} in the constructor.
 */
public class GenericChemicalToItemRecipe extends BasicChemicalCrystallizerRecipe {

    private final Holder<Item> machineItemHolder;
    private final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericChemicalToItemRecipe>> recipeSerializer;
    private final String machineName;

    public GenericChemicalToItemRecipe(
            ChemicalStackIngredient input,
            ItemStack output,
            RecipeTypeRegistryObject<SingleChemicalRecipeInput, ChemicalCrystallizerRecipe, SingleChemical<ChemicalCrystallizerRecipe>> recipeType,
            ResourceLocation machineId,
            DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericChemicalToItemRecipe>> recipeSerializer) {
        super(input, output);
        this.machineItemHolder = DeferredHolder.create(Registries.ITEM, machineId);
        this.recipeSerializer = recipeSerializer;
        this.machineName = machineId.getPath();
        ((CustomRecipeTyped) (Object) this).mekcm$setRecipeType(recipeType.value());
    }

    /**
     * Returns a factory suitable for passing to
     * {@link mekanism.common.recipe.serializer.MekanismRecipeSerializer#crystallizing}.
     */
    public static BiFunction<ChemicalStackIngredient, ItemStack, BasicChemicalCrystallizerRecipe> getFactory(
            RecipeTypeRegistryObject<SingleChemicalRecipeInput, ChemicalCrystallizerRecipe, SingleChemical<ChemicalCrystallizerRecipe>> recipeType,
            ResourceLocation machineId,
            DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericChemicalToItemRecipe>> recipeSerializer) {
        return (input, output) -> new GenericChemicalToItemRecipe(input, output, recipeType, machineId, recipeSerializer);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public RecipeSerializer<BasicChemicalCrystallizerRecipe> getSerializer() {
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
