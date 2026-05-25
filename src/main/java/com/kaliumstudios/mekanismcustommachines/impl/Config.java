package com.kaliumstudios.mekanismcustommachines.impl;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Mod configuration placeholder.
 * <p>
 * No configurable values are needed at this stage — machines are declared
 * either by other mods through {@link com.kaliumstudios.mekanismcustommachines.api.event.RegisterCustomMachinesEvent}
 * or by KubeJS scripts through the integration package, so there is nothing
 * runtime-config-worthy yet. Extend this when per-machine toggles or global
 * limits become relevant (e.g. v2 factory-tier support, Section 8 of PLAN.md).
 */
public class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static final ModConfigSpec SPEC = BUILDER.build();
}
