package com.kaliumstudios.mekanismcustommachines.impl.recipe;

import com.mojang.datafixers.util.Function4;

import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.basic.BasicSawmillRecipe;
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
 * Recipe class shared by all sawmill-style (Item → Item + chance secondary)
 * custom machines.
 * <p>
 * Extends {@link BasicSawmillRecipe} so it can be produced by
 * {@link mekanism.common.recipe.serializer.SawmillRecipeSerializer} (which is
 * statically typed to {@code Function4<..., BasicSawmillRecipe>}).
 * <p>
 * {@link SawmillRecipe#getType()} is normally {@code final} and hard-coded to
 * Mekanism's SAWING type. {@code CustomRecipeTypedMixin} injects into it so a
 * per-instance override (set via {@link CustomRecipeTyped}) takes precedence.
 */
public class GenericSawmillRecipe extends BasicSawmillRecipe {

    private final Holder<Item> machineItemHolder;
    private final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericSawmillRecipe>> recipeSerializer;
    private final String machineName;

    public GenericSawmillRecipe(
            ItemStackIngredient input,
            ItemStack mainOutput,
            ItemStack secondaryOutput,
            double secondaryChance,
            RecipeTypeRegistryObject<SingleRecipeInput, SawmillRecipe, SingleItem<SawmillRecipe>> recipeType,
            ResourceLocation machineId,
            DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericSawmillRecipe>> recipeSerializer) {
        super(input, mainOutput, secondaryOutput, secondaryChance);
        this.machineItemHolder = DeferredHolder.create(Registries.ITEM, machineId);
        this.recipeSerializer = recipeSerializer;
        this.machineName = machineId.getPath();
        ((CustomRecipeTyped) (Object) this).mekcm$setRecipeType(recipeType.value());
    }

    /**
     * Returns a factory suitable for passing to
     * {@link mekanism.common.recipe.serializer.SawmillRecipeSerializer}.
     */
    public static Function4<ItemStackIngredient, ItemStack, ItemStack, Double, BasicSawmillRecipe> getFactory(
            RecipeTypeRegistryObject<SingleRecipeInput, SawmillRecipe, SingleItem<SawmillRecipe>> recipeType,
            ResourceLocation machineId,
            DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericSawmillRecipe>> recipeSerializer) {
        return (input, mainOutput, secondaryOutput, secondaryChance) ->
                new GenericSawmillRecipe(input, mainOutput, secondaryOutput, secondaryChance, recipeType, machineId, recipeSerializer);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public RecipeSerializer<BasicSawmillRecipe> getSerializer() {
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
