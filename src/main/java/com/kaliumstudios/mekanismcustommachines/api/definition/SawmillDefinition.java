package com.kaliumstudios.mekanismcustommachines.api.definition;

import java.util.Objects;

import com.kaliumstudios.mekanismcustommachines.api.energy.EnergyProfile;
import com.kaliumstudios.mekanismcustommachines.api.layout.RecipeViewerLayout;

import mekanism.common.registration.impl.SoundEventRegistryObject;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

/**
 * Definition for a sawmill-style machine (one item input → main output plus an
 * optional secondary output produced at a configurable chance, analogous to
 * the Precision Sawmill).
 * <p>
 * Recipes for sawmill-style machines specify their own per-recipe secondary
 * chance; this definition holds only machine-wide config (energy, sound, etc).
 */
public record SawmillDefinition(
        ResourceLocation id,
        String processName,
        EnergyProfile energy,
        int baseTicksRequired,
        SoundEventRegistryObject<SoundEvent> sound,
        RecipeViewerLayout viewerLayout
) implements MachineDefinition {

    public SawmillDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(processName, "processName");
        Objects.requireNonNull(energy, "energy");
        Objects.requireNonNull(sound, "sound");
        Objects.requireNonNull(viewerLayout, "viewerLayout");
        if (baseTicksRequired <= 0) throw new IllegalArgumentException("baseTicksRequired must be > 0");
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
        private SoundEventRegistryObject<SoundEvent> sound = MekanismSounds.PRECISION_SAWMILL;
        private RecipeViewerLayout viewerLayout = RecipeViewerLayout.ELECTRIC;

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

        public Builder sound(SoundEventRegistryObject<SoundEvent> sound) {
            this.sound = Objects.requireNonNull(sound);
            return this;
        }

        public Builder viewerLayout(RecipeViewerLayout layout) {
            this.viewerLayout = Objects.requireNonNull(layout);
            return this;
        }

        public SawmillDefinition build() {
            return new SawmillDefinition(id, processName, energy, baseTicksRequired, sound, viewerLayout);
        }
    }
}
