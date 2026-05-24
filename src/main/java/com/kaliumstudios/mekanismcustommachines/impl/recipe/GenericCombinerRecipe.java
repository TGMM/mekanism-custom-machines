package com.kaliumstudios.mekanismcustommachines.impl.recipe;

import com.mojang.datafixers.util.Function3;

import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.basic.BasicCombinerRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.DoubleItem;
import mekanism.common.registration.impl.RecipeTypeRegistryObject;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Recipe class shared by all combiner-style (Item + Item → Item) custom machines.
 * <p>
 * Extends {@link BasicCombinerRecipe} so it can be produced by
 * {@link mekanism.common.recipe.serializer.MekanismRecipeSerializer#combining}
 * (which is statically typed to {@code Function3<..., BasicCombinerRecipe>}).
 * <p>
 * {@link CombinerRecipe#getType()} is normally {@code final} and hard-coded to
 * Mekanism's COMBINING type. {@code CustomRecipeTypedMixin} injects into it so
 * a per-instance override (set via {@link CustomRecipeTyped}) takes precedence.
 * We stash our per-machine recipe type in the constructor.
 * <p>
 * {@code getSerializer()} is forced through a raw cast because
 * {@link BasicCombinerRecipe#getSerializer()} narrows the return type to
 * {@code RecipeSerializer<BasicCombinerRecipe>}, and {@code RecipeSerializer}
 * is invariant on its type parameter. At runtime the returned instance is
 * still our concrete per-machine serializer.
 */
public class GenericCombinerRecipe extends BasicCombinerRecipe {

    private final Holder<Item> machineItemHolder;
    private final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericCombinerRecipe>> recipeSerializer;
    private final String machineName;

    public GenericCombinerRecipe(
            ItemStackIngredient mainInput,
            ItemStackIngredient extraInput,
            ItemStack output,
            RecipeTypeRegistryObject<RecipeInput, CombinerRecipe, DoubleItem<CombinerRecipe>> recipeType,
            ResourceLocation machineId,
            DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericCombinerRecipe>> recipeSerializer) {
        super(mainInput, extraInput, output);
        this.machineItemHolder = DeferredHolder.create(Registries.ITEM, machineId);
        this.recipeSerializer = recipeSerializer;
        this.machineName = machineId.getPath();
        // Stash the per-machine recipe type through the mixin interface. After this
        // call, getType() returns recipeType.value() instead of the Mekanism default.
        ((CustomRecipeTyped) (Object) this).mekcm$setRecipeType(recipeType.value());
    }

    /**
     * Returns a factory suitable for passing to
     * {@link mekanism.common.recipe.serializer.MekanismRecipeSerializer#combining}.
     */
    public static Function3<ItemStackIngredient, ItemStackIngredient, ItemStack, BasicCombinerRecipe> getFactory(
            RecipeTypeRegistryObject<RecipeInput, CombinerRecipe, DoubleItem<CombinerRecipe>> recipeType,
            ResourceLocation machineId,
            DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericCombinerRecipe>> recipeSerializer) {
        return (main, extra, output) -> new GenericCombinerRecipe(main, extra, output, recipeType, machineId, recipeSerializer);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public RecipeSerializer<BasicCombinerRecipe> getSerializer() {
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
