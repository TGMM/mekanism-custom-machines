package com.kaliumstudios.mekanismcustommachines.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kaliumstudios.mekanismcustommachines.impl.recipe.CustomRecipeTyped;

import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.SawmillRecipe;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Adds a per-instance recipe-type override to selected Mekanism recipe classes.
 * <p>
 * The four target classes ({@link CombinerRecipe}, {@link SawmillRecipe},
 * {@link ChemicalCrystallizerRecipe}, {@link FluidToFluidRecipe}) each declare
 * a {@code final} {@code getType()} method that hard-codes the Mekanism
 * recipe-type singleton. This mixin:
 * <ol>
 *   <li>Implements the {@link CustomRecipeTyped} interface on each target so
 *       our generic recipe subclasses can stash a per-instance type.</li>
 *   <li>Injects at the head of {@code getType()} so when the per-instance
 *       override is set, it is returned instead of the Mekanism default.</li>
 * </ol>
 * <p>
 * Without this mixin, all our generic combiner/sawmill/crystallizer/evaporator
 * recipes would share the Mekanism recipe-type and therefore the Mekanism
 * machines' recipe pools — preventing per-machine recipe scoping.
 */
@Mixin({CombinerRecipe.class, SawmillRecipe.class, ChemicalCrystallizerRecipe.class, FluidToFluidRecipe.class})
public abstract class CustomRecipeTypedMixin implements CustomRecipeTyped {

    @Unique
    private RecipeType<?> mekcm$customType;

    @Override
    public void mekcm$setRecipeType(RecipeType<?> type) {
        this.mekcm$customType = type;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "getType", at = @At("HEAD"), cancellable = true)
    private void mekcm$overrideGetType(CallbackInfoReturnable<RecipeType> cir) {
        if (mekcm$customType != null) {
            cir.setReturnValue(mekcm$customType);
        }
    }
}
