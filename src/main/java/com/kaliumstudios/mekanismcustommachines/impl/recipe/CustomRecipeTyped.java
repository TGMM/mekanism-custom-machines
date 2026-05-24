package com.kaliumstudios.mekanismcustommachines.impl.recipe;

import net.minecraft.world.item.crafting.RecipeType;

/**
 * Internal "duck type" interface added by mixin to selected Mekanism recipe
 * classes whose {@code getType()} method is otherwise hard-coded and {@code final}.
 * <p>
 * After construction, our generic recipe subclasses cast {@code this} to this
 * interface and call {@link #mekcm$setRecipeType} with the per-machine
 * {@link RecipeType}. The mixin then makes the inherited {@code getType()}
 * return our override instead of the Mekanism default.
 * <p>
 * <strong>Not part of the public API.</strong> Downstream consumers must not
 * reference this type.
 */
public interface CustomRecipeTyped {

    /**
     * Sets the per-instance recipe-type override. Once non-null, the recipe's
     * {@code getType()} returns this value instead of the Mekanism default.
     */
    void mekcm$setRecipeType(RecipeType<?> type);
}
