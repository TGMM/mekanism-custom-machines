package com.kaliumstudios.mekanismcustommachines.api.recipe;

/**
 * Reserved for a future fluent helper that constructs Mekanism recipe instances
 * for custom machines at server-reload time.
 * <p>
 * In v1, recipe instances are added directly via KubeJS {@code ServerEvents.recipes}
 * or through CraftTweaker scripts. This class is a placeholder to reserve the
 * API surface for v2.
 */
public final class CustomRecipeBuilder {
    private CustomRecipeBuilder() {}
}
