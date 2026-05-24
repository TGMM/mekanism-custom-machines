package com.kaliumstudios.mekanismcustommachines.api.definition;

import java.util.Objects;

import com.kaliumstudios.mekanismcustommachines.api.energy.EnergyProfile;
import com.kaliumstudios.mekanismcustommachines.api.layout.RecipeViewerLayout;

import mekanism.common.registration.impl.SoundEventRegistryObject;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * Definition for a fluid-to-fluid machine (single fluid input, single fluid
 * output — conceptually like a single-block thermal evaporation plant).
 * <p>
 * Mekanism's only built-in fluid→fluid machine (the Thermal Evaporation Plant)
 * is a multi-block. This definition powers a custom single-block alternative
 * that consumes the {@code EVAPORATING}-style {@code FluidToFluidRecipe}.
 */
public record FluidToFluidDefinition(
        ResourceLocation id,
        String processName,
        EnergyProfile energy,
        int baseTicksRequired,
        int maxFluid,
        SoundEventRegistryObject<SoundEvent> sound,
        RecipeViewerLayout viewerLayout
) implements MachineDefinition {

    public FluidToFluidDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(processName, "processName");
        Objects.requireNonNull(energy, "energy");
        Objects.requireNonNull(sound, "sound");
        Objects.requireNonNull(viewerLayout, "viewerLayout");
        if (baseTicksRequired <= 0) throw new IllegalArgumentException("baseTicksRequired must be > 0");
        if (maxFluid <= 0) throw new IllegalArgumentException("maxFluid must be > 0");
    }

    public static Builder builder(String id) {
        return builder(ResourceLocation.parse(id));
    }

    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public static final class Builder {
        private final ResourceLocation id;
        private String processName;
        private EnergyProfile energy = EnergyProfile.defaultMachine();
        private int baseTicksRequired = 200;
        private int maxFluid = 10 * FluidType.BUCKET_VOLUME;
        private SoundEventRegistryObject<SoundEvent> sound = MekanismSounds.NUTRITIONAL_LIQUIFIER;
        private RecipeViewerLayout viewerLayout = RecipeViewerLayout.FLUID_TO_FLUID;

        private Builder(ResourceLocation id) {
            this.id = Objects.requireNonNull(id, "id");
            this.processName = id.getPath().replace('_', ' ');
        }

        public Builder processName(String processName) {
            this.processName = Objects.requireNonNull(processName);
            return this;
        }

        public Builder energy(EnergyProfile energy) {
            this.energy = Objects.requireNonNull(energy);
            return this;
        }

        public Builder energy(long usageFE, long storageFE) {
            return energy(EnergyProfile.of(usageFE, storageFE));
        }

        public Builder ticks(int ticks) {
            this.baseTicksRequired = ticks;
            return this;
        }

        public Builder maxFluid(int capacity) {
            this.maxFluid = capacity;
            return this;
        }

        public Builder sound(SoundEventRegistryObject<SoundEvent> sound) {
            this.sound = Objects.requireNonNull(sound);
            return this;
        }

        public Builder viewerLayout(RecipeViewerLayout layout) {
            this.viewerLayout = Objects.requireNonNull(layout);
            return this;
        }

        public FluidToFluidDefinition build() {
            return new FluidToFluidDefinition(id, processName, energy, baseTicksRequired, maxFluid, sound, viewerLayout);
        }
    }
}
