package com.kaliumstudios.mekanismcustommachines.api.definition;

import java.util.Objects;

import com.kaliumstudios.mekanismcustommachines.api.energy.EnergyProfile;
import com.kaliumstudios.mekanismcustommachines.api.layout.RecipeViewerLayout;

import mekanism.common.registration.impl.SoundEventRegistryObject;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

/**
 * Definition for a combiner-style machine (two item inputs → one item output,
 * analogous to the Combiner).
 * <p>
 * Construct via {@link Builder}:
 * <pre>{@code
 * CombinerDefinition def = CombinerDefinition.builder("mymod:my_combiner")
 *     .processName("Combining")
 *     .energy(200, 10_000)
 *     .build();
 * }</pre>
 */
public record CombinerDefinition(
        ResourceLocation id,
        String processName,
        EnergyProfile energy,
        int baseTicksRequired,
        SoundEventRegistryObject<SoundEvent> sound,
        RecipeViewerLayout viewerLayout
) implements MachineDefinition {

    public CombinerDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(processName, "processName");
        Objects.requireNonNull(energy, "energy");
        Objects.requireNonNull(sound, "sound");
        Objects.requireNonNull(viewerLayout, "viewerLayout");
        if (baseTicksRequired <= 0) throw new IllegalArgumentException("baseTicksRequired must be > 0");
    }

    /** Returns a builder pre-populated with sensible defaults. */
    public static Builder builder(String id) {
        return builder(ResourceLocation.parse(id));
    }

    /** Returns a builder pre-populated with sensible defaults. */
    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public static final class Builder {
        private final ResourceLocation id;
        private String processName;
        private EnergyProfile energy = EnergyProfile.defaultMachine();
        private int baseTicksRequired = 200;
        private SoundEventRegistryObject<SoundEvent> sound = MekanismSounds.COMBINER;
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

        public CombinerDefinition build() {
            return new CombinerDefinition(id, processName, energy, baseTicksRequired, sound, viewerLayout);
        }
    }
}
