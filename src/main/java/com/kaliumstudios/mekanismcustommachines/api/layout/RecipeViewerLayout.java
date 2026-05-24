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
}
