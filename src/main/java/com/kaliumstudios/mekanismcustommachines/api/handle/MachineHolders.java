package com.kaliumstudios.mekanismcustommachines.api.handle;

import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.RecipeTypeRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Read-only view of the NeoForge deferred-registry holders produced when a
 * custom machine is registered.
 * <p>
 * All holders are resolved lazily — do not call {@code get()} on them until
 * after NeoForge's {@code RegisterEvent} has fired.
 */
public interface MachineHolders {

    /** The registered block (and its item-block). */
    BlockRegistryObject<?, ?> block();

    /** The tile entity type. */
    TileEntityTypeRegistryObject<? extends TileEntityMekanism> tileEntityType();

    /** The recipe type, parameterised over this machine's recipe class. */
    <INPUT extends RecipeInput,
     RECIPE extends MekanismRecipe<INPUT>,
     CACHE extends IInputRecipeCache>
    RecipeTypeRegistryObject<INPUT, RECIPE, CACHE> recipeType();

    /** The container (menu) type used to open the machine GUI. */
    ContainerTypeRegistryObject<?> containerType();
}
