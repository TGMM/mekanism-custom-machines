package com.kaliumstudios.mekanismcustommachines.api.layout;

/**
 * Defines the recipe viewer (JEI/EMI) overlay position and size for a machine's
 * recipe category.
 * <p>
 * Coordinates are relative to the machine GUI's top-left corner, matching the
 * convention used by Mekanism's own {@code RVRecipeTypeWrapper}.
 */
public record RecipeViewerLayout(int xOffset, int yOffset, int width, int height) {

    /**
     * Preset matching Mekanism's standard electric machine GUI
     * (single item input → single item output, 144×54 area).
     */
    public static final RecipeViewerLayout ELECTRIC = new RecipeViewerLayout(-28, -16, 144, 54);

    /**
     * Preset for advanced electric machines (item + chemical input).
     */
    public static final RecipeViewerLayout ADVANCED_ELECTRIC = new RecipeViewerLayout(-28, -16, 168, 72);

    /**
     * Preset matching Mekanism's Chemical Oxidizer / Pigment Extractor
     * (single item input → single chemical output).
     */
    public static final RecipeViewerLayout CHEMICAL_OUT = new RecipeViewerLayout(-20, -12, 132, 62);

    /**
     * Preset matching Mekanism's Chemical Crystallizer
     * (single chemical input → single item output).
     */
    public static final RecipeViewerLayout CRYSTALLIZER = new RecipeViewerLayout(-5, -3, 147, 79);

    /**
     * Preset matching Mekanism's Solar Neutron Activator / Isotopic Centrifuge
     * (single chemical input → single chemical output).
     */
    public static final RecipeViewerLayout CHEMICAL_TO_CHEMICAL = new RecipeViewerLayout(-4, -13, 168, 60);

    /**
     * Preset matching Mekanism's Thermal Evaporation Plant
     * (single fluid input → single fluid output).
     */
    public static final RecipeViewerLayout FLUID_TO_FLUID = new RecipeViewerLayout(-3, -12, 176, 62);
}
