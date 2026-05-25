package com.kaliumstudios.mekanismcustommachines.impl.asset;

import com.kaliumstudios.mekanismcustommachines.api.definition.ChemicalToChemicalDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.ChemicalToItemDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.CombinerDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.FluidToFluidDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.ItemChemicalToItemDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.ItemToChemicalDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.ItemToItemDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.MachineDefinition;

/**
 * Maps each {@link MachineDefinition} subtype to a Mekanism block whose
 * existing models/textures we can borrow for rendering.
 * <p>
 * The plan is to never ship per-machine assets in the addon jar — custom
 * machines reuse Mekanism's own machine models, swapping only the registry
 * id. This class is the single source of truth for those aliases; the
 * virtual resource pack in
 * {@link com.kaliumstudios.mekanismcustommachines.impl.asset.MachineAssetPack}
 * uses it to synthesise blockstate / item-model JSON at load time.
 * <p>
 * The aliased model paths are the {@code "model": …} values of Mekanism's
 * own blockstate variants (e.g. {@code mekanism:block/enrichment_chamber}),
 * so they include the active/inactive split out of the box.
 */
public final class FactoryTypeAssetResolver {

    private FactoryTypeAssetResolver() {}

    /** Resource-path of the Mekanism analog this definition reuses assets from. */
    public static String mekanismAnalog(MachineDefinition definition) {
        return switch (definition) {
            case ItemToItemDefinition ignored -> "enrichment_chamber";
            case ItemChemicalToItemDefinition ignored -> "osmium_compressor";
            case CombinerDefinition ignored -> "combiner";
            case com.kaliumstudios.mekanismcustommachines.api.definition.SawmillDefinition ignored -> "precision_sawmill";
            case ItemToChemicalDefinition ignored -> "chemical_oxidizer";
            case ChemicalToItemDefinition ignored -> "chemical_crystallizer";
            case ChemicalToChemicalDefinition ignored -> "isotopic_centrifuge";
            case FluidToFluidDefinition ignored -> "nutritional_liquifier";
        };
    }
}
