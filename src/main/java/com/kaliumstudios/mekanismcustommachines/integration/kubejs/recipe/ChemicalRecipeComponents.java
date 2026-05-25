package com.kaliumstudios.mekanismcustommachines.integration.kubejs.recipe;

import com.kaliumstudios.mekanismcustommachines.api.MekanismCustomMachinesAPI;

import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.SimpleRecipeComponent;
import dev.latvian.mods.rhino.type.TypeInfo;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.resources.ResourceLocation;

/**
 * Custom KubeJS {@link RecipeComponent}s for Mekanism chemical stacks and
 * ingredients.
 * <p>
 * KubeJS ships components for vanilla {@code Ingredient}/{@code ItemStack} and
 * NeoForge {@code FluidStack}/{@code FluidIngredient} but has no built-in
 * support for Mekanism chemicals. These components wrap Mekanism's own
 * {@code ChemicalStack.MAP_CODEC}/{@code ChemicalStackIngredient.CODEC} so
 * scripts can specify chemicals as JSON objects:
 * <pre>{@code
 * {
 *     chemicalInput: { chemical: 'mekanism:hydrogen', amount: 100 },
 *     output:        { id:       'mekanism:oxygen',  amount: 100 }
 * }
 * }</pre>
 */
public final class ChemicalRecipeComponents {

    private ChemicalRecipeComponents() {}

    /** A single chemical stack — used for chemical outputs. */
    public static final RecipeComponentType<ChemicalStack> CHEMICAL_STACK = RecipeComponentType.unit(
            ResourceLocation.fromNamespaceAndPath(MekanismCustomMachinesAPI.MODID, "chemical_stack"),
            type -> new SimpleRecipeComponent<>(type, ChemicalStack.CODEC, TypeInfo.of(ChemicalStack.class)));

    /** A chemical stack ingredient — used for chemical inputs. */
    public static final RecipeComponentType<ChemicalStackIngredient> CHEMICAL_STACK_INGREDIENT = RecipeComponentType.unit(
            ResourceLocation.fromNamespaceAndPath(MekanismCustomMachinesAPI.MODID, "chemical_stack_ingredient"),
            type -> new SimpleRecipeComponent<>(
                    type,
                    IngredientCreatorAccess.chemicalStack().codec(),
                    TypeInfo.of(ChemicalStackIngredient.class)));
}
