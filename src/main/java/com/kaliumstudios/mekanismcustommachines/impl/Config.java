package com.kaliumstudios.mekanismcustommachines.impl;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Mod configuration.
 * Placeholder — extend as needed when per-machine config values are added in v2.
 */
public class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue REGISTER_TEST_MACHINE = BUILDER
            .comment("Whether to register the built-in test_chamber machine. " +
                     "Disable in production packs that don't need it.")
            .define("registerTestMachine", true);

    static final ModConfigSpec SPEC = BUILDER.build();
}
