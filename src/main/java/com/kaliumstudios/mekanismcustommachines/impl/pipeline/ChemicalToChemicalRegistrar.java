package com.kaliumstudios.mekanismcustommachines.impl.pipeline;

import com.kaliumstudios.mekanismcustommachines.api.definition.ChemicalToChemicalDefinition;
import com.kaliumstudios.mekanismcustommachines.impl.mixin.MekanismRecipeTypeMixin;
import com.kaliumstudios.mekanismcustommachines.impl.recipe.GenericChemicalToChemicalRecipe;
import com.kaliumstudios.mekanismcustommachines.impl.registry.RegisteredMachineImpl;
import com.kaliumstudios.mekanismcustommachines.impl.tile.GenericChemicalToChemicalTile;

import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.api.recipes.vanilla_input.SingleChemicalRecipeInput;
import mekanism.common.attachments.component.AttachedEjector;
import mekanism.common.attachments.component.AttachedSideConfig;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.chemical.ChemicalTanksBuilder;
import mekanism.common.attachments.containers.item.ItemSlotsBuilder;
import mekanism.common.block.attribute.AttributeSideConfig;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.content.blocktype.Machine.MachineBuilder;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleChemical;
import mekanism.common.recipe.serializer.MekanismRecipeSerializer;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.RecipeTypeRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Registers a Chemical→Chemical custom machine (centrifuge/activator analog).
 */
final class ChemicalToChemicalRegistrar implements MachineRegistrar<ChemicalToChemicalDefinition> {

    static final ChemicalToChemicalRegistrar INSTANCE = new ChemicalToChemicalRegistrar();

    private ChemicalToChemicalRegistrar() {}

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public RegisteredMachineImpl register(ChemicalToChemicalDefinition def, DeferredBundle bundle) {
        String path = def.id().getPath();

        // ── 1. Recipe type ──────────────────────────────────────────────────
        RecipeTypeRegistryObject<SingleChemicalRecipeInput, ChemicalToChemicalRecipe, SingleChemical<ChemicalToChemicalRecipe>>
                recipeType = bundle.recipeTypes.registerMek(
                        path,
                        name -> MekanismRecipeTypeMixin.invokeConstructor(
                                name,
                                rt -> new SingleChemical<>(rt, ChemicalToChemicalRecipe::getInput)));

        // ── 2. Lazy holder references ───────────────────────────────────────
        TileEntityTypeRegistryObject[] tileHolder = new TileEntityTypeRegistryObject[1];
        ContainerTypeRegistryObject[] containerHolder = new ContainerTypeRegistryObject[1];
        DeferredHolder[] serializerHolder = new DeferredHolder[1];

        // ── 3. Block type (non-factory) ─────────────────────────────────────
        MachineBuilder<Machine<GenericChemicalToChemicalTile>, GenericChemicalToChemicalTile, ?> builder =
                MachineBuilder.createMachine(
                        () -> tileHolder[0],
                        () -> def.processName());
        builder.withGui(() -> containerHolder[0]);
        builder.withSound(def.sound());
        builder.withEnergyConfig(def.energy().usage(), def.energy().storage());
        builder.with(AttributeSideConfig.ADVANCED_ELECTRIC_MACHINE);
        @SuppressWarnings("unchecked")
        Machine<GenericChemicalToChemicalTile> blockType =
                (Machine<GenericChemicalToChemicalTile>) builder.build();

        // ── 4. Block + item-block ───────────────────────────────────────────
        BlockRegistryObject<BlockTile<GenericChemicalToChemicalTile, Machine<GenericChemicalToChemicalTile>>,
                ItemBlockTooltip<BlockTile<GenericChemicalToChemicalTile, Machine<GenericChemicalToChemicalTile>>>>
                block = bundle.blocks.register(
                        path,
                        () -> new BlockTile<>(blockType,
                                props -> props.mapColor(BlockResourceInfo.STEEL.getMapColor())),
                        (b, props) -> new ItemBlockTooltip<>(b, true, props
                                .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                                .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.CENTRIFUGE)))
                .forItemHolder(holder -> holder
                        .addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                                .addBasic(def.maxChemical(), recipeType, SingleChemical::containsInput)
                                .addBasic(def.maxChemical())
                                .build())
                        .addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                                .addChemicalFillSlot(0)
                                .addChemicalDrainSlot(1)
                                .addEnergy()
                                .build()));

        // ── 5. Recipe serializer ────────────────────────────────────────────
        DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericChemicalToChemicalRecipe>> recipeSerializer =
                bundle.recipeSerializers.register(
                        path,
                        () -> MekanismRecipeSerializer.chemicalToChemical(
                                GenericChemicalToChemicalRecipe.getFactory(
                                        recipeType,
                                        def.id(),
                                        (DeferredHolder) serializerHolder[0])));
        serializerHolder[0] = recipeSerializer;

        // ── 6. Tile entity type ─────────────────────────────────────────────
        TileEntityTypeRegistryObject<GenericChemicalToChemicalTile> tileEntity =
                bundle.tileEntities.mekBuilder(
                                block,
                                GenericChemicalToChemicalTile.factory(block, recipeType, def))
                        .clientTicker(TileEntityMekanism::tickClient)
                        .serverTicker(TileEntityMekanism::tickServer)
                        .withSimple(Capabilities.CONFIG_CARD)
                        .build();
        tileHolder[0] = tileEntity;

        // ── 7. Container type ───────────────────────────────────────────────
        ContainerTypeRegistryObject<MekanismTileContainer<GenericChemicalToChemicalTile>> containerType =
                bundle.containerTypes.register(block, GenericChemicalToChemicalTile.class);
        containerHolder[0] = containerType;

        return new RegisteredMachineImpl(def, block, tileEntity, recipeType, containerType);
    }
}
