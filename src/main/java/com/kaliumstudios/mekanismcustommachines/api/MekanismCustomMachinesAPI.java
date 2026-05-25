package com.kaliumstudios.mekanismcustommachines.api;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.IEventBus;

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

    /**
     * The mod's dedicated event bus for cross-mod, construction-time events
     * — currently only {@link com.kaliumstudios.mekanismcustommachines.api.event.RegisterCustomMachinesEvent}.
     * <p>
     * We can't use {@code NeoForge.EVENT_BUS} for this because that bus is
     * created in a shut-down state and only started <em>after</em> all
     * {@code @Mod} constructors finish; any event posted on it during mod
     * construction is silently dropped. Mod-specific registration events on
     * NeoForge's side go through the per-mod mod-event-bus instead — but
     * that bus is private to each mod, so other mods can't subscribe.
     * <p>
     * This bus is created with no {@code startShutdown()} and no class
     * filter, so it is dispatch-ready from the moment this class is loaded.
     * Other mods that want to register custom machines should subscribe to
     * it from their own {@code @Mod} constructor:
     * <pre>{@code
     * MekanismCustomMachinesAPI.EVENT_BUS.addListener(
     *     RegisterCustomMachinesEvent.class,
     *     event -> event.register(...)
     * );
     * }</pre>
     */
    public static final IEventBus EVENT_BUS = BusBuilder.builder().build();

    private MekanismCustomMachinesAPI() {}
}
