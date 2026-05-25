package com.kaliumstudios.mekanismcustommachines.integration.kubejs.recipe;

import com.kaliumstudios.mekanismcustommachines.api.MachineRegistry;
import com.kaliumstudios.mekanismcustommachines.api.definition.ChemicalToChemicalDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.ChemicalToItemDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.CombinerDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.FluidToFluidDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.ItemChemicalToItemDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.ItemToChemicalDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.ItemToItemDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.MachineDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.SawmillDefinition;
import com.kaliumstudios.mekanismcustommachines.api.handle.RegisteredMachine;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.FluidStackComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.SizedFluidIngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.SizedIngredientComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

/**
 * Registers a KubeJS {@link RecipeSchema} for every machine in
 * {@link MachineRegistry#all()} so scripts can add recipes via:
 * <pre>{@code
 * ServerEvents.recipes(event => {
 *     event.recipes.mymod.my_machine({ input: ..., output: ... })
 * })
 * }</pre>
 * <p>
 * Each schema's id matches the machine's registry id; its keys' JSON layout
 * matches the corresponding Mekanism recipe serializer's codec. The serializer
 * is identified at runtime by the schema id (which is also the serializer's
 * registry id — they share a path during machine registration).
 * <p>
 * Where Mekanism uses {@code ItemStackIngredient}/{@code FluidStackIngredient}
 * (its own thin wrappers over NeoForge's {@code SizedIngredient}/
 * {@code SizedFluidIngredient}), we use the NeoForge equivalents because the
 * wire-level JSON is identical — Mekanism's codec is just an
 * {@code xmap} over the NeoForge codec.
 */
public final class KubeJSRecipeSchemas {

    private KubeJSRecipeSchemas() {}

    // ── Key helpers ───────────────────────────────────────────────────────

    private static RecipeKey<SizedIngredient> itemInputKey(String name) {
        return SizedIngredientComponent.FLAT.key(name, ComponentRole.INPUT);
    }

    private static RecipeKey<SizedFluidIngredient> fluidInputKey(String name) {
        return SizedFluidIngredientComponent.FLAT.key(name, ComponentRole.INPUT);
    }

    private static RecipeKey<ChemicalStackIngredient> chemicalInputKey(String name) {
        return ChemicalRecipeComponents.CHEMICAL_STACK_INGREDIENT.key(name, ComponentRole.INPUT);
    }

    private static RecipeKey<ItemStack> itemOutputKey(String name) {
        return ItemStackComponent.ITEM_STACK.outputKey(name);
    }

    private static RecipeKey<ItemStack> optionalItemOutputKey(String name) {
        return ItemStackComponent.OPTIONAL_ITEM_STACK.outputKey(name).optional(ItemStack.EMPTY);
    }

    private static RecipeKey<FluidStack> fluidOutputKey(String name) {
        return FluidStackComponent.FLUID_STACK.outputKey(name);
    }

    private static RecipeKey<ChemicalStack> chemicalOutputKey(String name) {
        return ChemicalRecipeComponents.CHEMICAL_STACK.outputKey(name);
    }

    // ── Registration ──────────────────────────────────────────────────────

    /** Called from the KubeJS plugin's {@code registerRecipeSchemas} hook. */
    public static void registerAll(RecipeSchemaRegistry registry) {
        for (RegisteredMachine machine : MachineRegistry.all()) {
            RecipeSchema schema = schemaFor(machine.definition());
            registry.register(machine.id(), schema);
        }
    }

    private static RecipeSchema schemaFor(MachineDefinition def) {
        return switch (def) {
            case ItemToItemDefinition ignored -> new RecipeSchema(
                    itemInputKey(SerializationConstants.INPUT),
                    itemOutputKey(SerializationConstants.OUTPUT));
            case ItemChemicalToItemDefinition ignored -> new RecipeSchema(
                    itemInputKey(SerializationConstants.ITEM_INPUT),
                    chemicalInputKey(SerializationConstants.CHEMICAL_INPUT),
                    itemOutputKey(SerializationConstants.OUTPUT),
                    BooleanComponent.BOOLEAN.key(SerializationConstants.PER_TICK_USAGE, ComponentRole.OTHER)
                            .optional(Boolean.FALSE));
            case CombinerDefinition ignored -> new RecipeSchema(
                    itemInputKey(SerializationConstants.MAIN_INPUT),
                    itemInputKey(SerializationConstants.EXTRA_INPUT),
                    itemOutputKey(SerializationConstants.OUTPUT));
            case SawmillDefinition ignored -> new RecipeSchema(
                    itemInputKey(SerializationConstants.INPUT),
                    optionalItemOutputKey(SerializationConstants.MAIN_OUTPUT),
                    optionalItemOutputKey(SerializationConstants.SECONDARY_OUTPUT),
                    NumberComponent.DOUBLE.key(SerializationConstants.SECONDARY_CHANCE, ComponentRole.OTHER)
                            .optional(0.0));
            case ItemToChemicalDefinition ignored -> new RecipeSchema(
                    itemInputKey(SerializationConstants.INPUT),
                    chemicalOutputKey(SerializationConstants.OUTPUT));
            case ChemicalToItemDefinition ignored -> new RecipeSchema(
                    chemicalInputKey(SerializationConstants.INPUT),
                    itemOutputKey(SerializationConstants.OUTPUT));
            case ChemicalToChemicalDefinition ignored -> new RecipeSchema(
                    chemicalInputKey(SerializationConstants.INPUT),
                    chemicalOutputKey(SerializationConstants.OUTPUT));
            case FluidToFluidDefinition ignored -> new RecipeSchema(
                    fluidInputKey(SerializationConstants.INPUT),
                    fluidOutputKey(SerializationConstants.OUTPUT));
        };
    }
}
