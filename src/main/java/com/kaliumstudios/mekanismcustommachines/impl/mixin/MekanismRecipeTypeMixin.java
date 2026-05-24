package com.kaliumstudios.mekanismcustommachines.impl.mixin;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import mekanism.common.registration.impl.RecipeTypeRegistryObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Mixin that exposes {@link MekanismRecipeType}'s package-private constructor
 * via an {@code @Invoker}. This is required because {@code MekanismRecipeType}
 * cannot be instantiated from outside the {@code mekanism} package, and its
 * static {@code register()} method rejects any namespace that is not
 * {@code "mekanism"}.
 * <p>
 * Only {@code invokeConstructor} is used in production code. The {@code register}
 * invoker is present for completeness but must <em>not</em> be called — Mekanism
 * will throw {@link IllegalStateException} if the namespace check fails.
 */
@Mixin(MekanismRecipeType.class)
public interface MekanismRecipeTypeMixin {

    @Invoker("<init>")
    static <VANILLA_INPUT extends RecipeInput,
            RECIPE extends MekanismRecipe<VANILLA_INPUT>,
            INPUT_CACHE extends IInputRecipeCache>
    MekanismRecipeType<VANILLA_INPUT, RECIPE, INPUT_CACHE> invokeConstructor(
            ResourceLocation name,
            Function<MekanismRecipeType<VANILLA_INPUT, RECIPE, INPUT_CACHE>, INPUT_CACHE> inputCacheCreator) {
        throw new AssertionError("Mixin invoker not applied");
    }

    @Invoker("register")
    static <VANILLA_INPUT extends RecipeInput,
            RECIPE extends MekanismRecipe<VANILLA_INPUT>,
            INPUT_CACHE extends IInputRecipeCache>
    RecipeTypeRegistryObject<VANILLA_INPUT, RECIPE, INPUT_CACHE> register(
            ResourceLocation name,
            Function<MekanismRecipeType<VANILLA_INPUT, RECIPE, INPUT_CACHE>, INPUT_CACHE> inputCacheCreator) {
        throw new AssertionError("Mixin invoker not applied");
    }
}
