package com.kaliumstudios.mekanismcustommachines.api;

import java.util.Collection;
import java.util.Optional;

import com.kaliumstudios.mekanismcustommachines.api.definition.MachineDefinition;
import com.kaliumstudios.mekanismcustommachines.api.handle.RegisteredMachine;

import net.minecraft.resources.ResourceLocation;

/**
 * Primary entry point for registering custom Mekanism machines.
 * <p>
 * Machines must be registered <em>before</em> NeoForge's {@code RegisterEvent}
 * fires — i.e. during the {@code @Mod} constructor phase, typically inside a
 * handler for {@link com.kaliumstudios.mekanismcustommachines.api.event.RegisterCustomMachinesEvent}.
 * <p>
 * The backing implementation is in {@code impl} and must not be accessed
 * directly by downstream consumers.
 */
public final class MachineRegistry {

    private static Delegate delegate;

    private MachineRegistry() {}

    /**
     * Registers a custom machine described by the given {@link MachineDefinition}.
     * <p>
     * Returns a {@link RegisteredMachine} handle that can be stored to query
     * registry holders after the game has finished loading.
     *
     * @throws IllegalStateException if called after {@code RegisterEvent} has fired
     */
    public static RegisteredMachine register(MachineDefinition definition) {
        assertDelegate();
        return delegate.register(definition);
    }

    /** Returns an unmodifiable view of all machines registered so far. */
    public static Collection<RegisteredMachine> all() {
        assertDelegate();
        return delegate.all();
    }

    /** Looks up a registered machine by its registry id. */
    public static Optional<RegisteredMachine> get(ResourceLocation id) {
        assertDelegate();
        return delegate.get(id);
    }

    // ── Internal wiring ────────────────────────────────────────────────────

    /** Called once by {@code impl.MekanismCustomMachines} during mod construction. */
    public static void setDelegate(Delegate d) {
        if (delegate != null) throw new IllegalStateException("MachineRegistry delegate already set");
        delegate = d;
    }

    private static void assertDelegate() {
        if (delegate == null)
            throw new IllegalStateException(
                    "MachineRegistry has not been initialised yet. " +
                    "Ensure MekanismCustomMachines is loaded before calling this.");
    }

    /**
     * Internal SPI implemented by {@code MachineRegistryImpl}.
     * Not part of the public API — do not implement externally.
     */
    public interface Delegate {
        RegisteredMachine register(MachineDefinition definition);
        Collection<RegisteredMachine> all();
        Optional<RegisteredMachine> get(ResourceLocation id);
    }
}
