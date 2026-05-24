package com.kaliumstudios.mekanismcustommachines.api;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Public API façade for MekanismCustomMachines.
 * <p>
 * This is the stable entry point for downstream consumers. All types in the
 * {@code api} package are considered part of the public API and will follow
 * semver stability guarantees from v1.0 onward.
 * <p>
 * Internal implementation classes live under
 * {@code com.kaliumstudios.mekanismcustommachines.impl} and must never be
 * imported by downstream code.
 */
public final class MekanismCustomMachinesAPI {

    public static final String MODID = "mekanismcustommachines";
    public static final Logger LOGGER = LogUtils.getLogger();

    private MekanismCustomMachinesAPI() {}
}
