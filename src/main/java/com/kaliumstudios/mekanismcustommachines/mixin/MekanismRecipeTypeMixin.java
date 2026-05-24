package com.kaliumstudios.mekanismcustommachines.mixin;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import mekanism.common.registration.impl.RecipeTypeRegistryObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeInput;

@Mixin(MekanismRecipeType.class)
public interface MekanismRecipeTypeMixin {
    @Invoker("<init>")
    public static <VANILLA_INPUT extends RecipeInput, RECIPE extends MekanismRecipe<VANILLA_INPUT>, INPUT_CACHE extends IInputRecipeCache> MekanismRecipeType<VANILLA_INPUT, RECIPE, INPUT_CACHE> invokeConstructor(
            ResourceLocation name,
            Function<MekanismRecipeType<VANILLA_INPUT, RECIPE, INPUT_CACHE>, INPUT_CACHE> inputCacheCreator) {
        throw new Error("This is a mixin method and should not be called directly");
    }

    @Invoker("register")
    public static <VANILLA_INPUT extends RecipeInput, RECIPE extends MekanismRecipe<VANILLA_INPUT>, INPUT_CACHE extends IInputRecipeCache> RecipeTypeRegistryObject<VANILLA_INPUT, RECIPE, INPUT_CACHE> register(
            ResourceLocation name,
            Function<MekanismRecipeType<VANILLA_INPUT, RECIPE, INPUT_CACHE>, INPUT_CACHE> inputCacheCreator) {
        throw new Error("This is a mixin method and should not be called directly");
    }
}
