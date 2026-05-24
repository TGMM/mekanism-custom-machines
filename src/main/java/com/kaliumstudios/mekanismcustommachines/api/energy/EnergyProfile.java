package com.kaliumstudios.mekanismcustommachines.api.energy;

import java.util.function.LongSupplier;

/**
 * Describes the energy usage and storage capacity of a custom machine.
 * <p>
 * Both values are expressed as {@link LongSupplier}s to match Mekanism's
 * {@code withEnergyConfig(LongSupplier, LongSupplier)} builder API. This also
 * allows values to be read lazily from NeoForge config entries.
 * <p>
 * Use {@link #of} for fixed values, or {@link #borrowFrom} to delegate to an
 * existing Mekanism config entry (e.g.
 * {@code MekanismConfig.usage.enrichmentChamber}).
 */
public record EnergyProfile(LongSupplier usage, LongSupplier storage) {

    /**
     * Creates a profile from fixed long values (in FE).
     */
    public static EnergyProfile of(long usageFE, long storageFE) {
        return new EnergyProfile(() -> usageFE, () -> storageFE);
    }

    /**
     * Creates a profile that delegates to existing config suppliers at runtime.
     * Suitable for borrowing values from Mekanism's own machine configs.
     */
    public static EnergyProfile borrowFrom(LongSupplier usage, LongSupplier storage) {
        return new EnergyProfile(usage, storage);
    }

    /** Convenience: 200 FE usage, 10 000 FE storage — reasonable defaults for a basic machine. */
    public static EnergyProfile defaultMachine() {
        return of(200, 10_000);
    }
}
