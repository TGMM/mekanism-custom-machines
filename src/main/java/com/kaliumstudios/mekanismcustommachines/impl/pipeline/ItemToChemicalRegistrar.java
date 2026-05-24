package com.kaliumstudios.mekanismcustommachines.impl.pipeline;

import com.kaliumstudios.mekanismcustommachines.api.definition.ItemToChemicalDefinition;
import com.kaliumstudios.mekanismcustommachines.impl.mixin.MekanismRecipeTypeMixin;
import com.kaliumstudios.mekanismcustommachines.impl.recipe.GenericItemToChemicalRecipe;
import com.kaliumstudios.mekanismcustommachines.impl.registry.RegisteredMachineImpl;
import com.kaliumstudios.mekanismcustommachines.impl.tile.GenericItemToChemicalTile;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
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
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleItem;
import mekanism.common.recipe.lookup.cache.SingleInputRecipeCache;
import mekanism.common.recipe.serializer.MekanismRecipeSerializer;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.RecipeTypeRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Registers an Item→Chemical custom machine.
 */
final class ItemToChemicalRegistrar implements MachineRegistrar<ItemToChemicalDefinition> {

    static final ItemToChemicalRegistrar INSTANCE = new ItemToChemicalRegistrar();

    private ItemToChemicalRegistrar() {}

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public RegisteredMachineImpl register(ItemToChemicalDefinition def, DeferredBundle bundle) {
        String path = def.id().getPath();

        // ── 1. Recipe type ──────────────────────────────────────────────────
        RecipeTypeRegistryObject<SingleRecipeInput, ItemStackToChemicalRecipe, SingleItem<ItemStackToChemicalRecipe>>
                recipeType = bundle.recipeTypes.registerMek(
                        path,
                        name -> MekanismRecipeTypeMixin.invokeConstructor(
                                name,
                                rt -> new SingleItem<>(rt, ItemStackToChemicalRecipe::getInput)));

        // ── 2. Lazy holder references ───────────────────────────────────────
        TileEntityTypeRegistryObject[] tileHolder = new TileEntityTypeRegistryObject[1];
        ContainerTypeRegistryObject[] containerHolder = new ContainerTypeRegistryObject[1];
        DeferredHolder[] serializerHolder = new DeferredHolder[1];

        // ── 3. Block type (non-factory) ─────────────────────────────────────
        MachineBuilder<Machine<GenericItemToChemicalTile>, GenericItemToChemicalTile, ?> builder =
                MachineBuilder.createMachine(
                        () -> tileHolder[0],
                        () -> def.processName());
        builder.withGui(() -> containerHolder[0]);
        builder.withSound(def.sound());
        builder.withEnergyConfig(def.energy().usage(), def.energy().storage());
        builder.with(AttributeSideConfig.ADVANCED_ELECTRIC_MACHINE);
        @SuppressWarnings("unchecked")
        Machine<GenericItemToChemicalTile> blockType =
                (Machine<GenericItemToChemicalTile>) builder.build();

        // ── 4. Block + item-block ───────────────────────────────────────────
        BlockRegistryObject<BlockTile<GenericItemToChemicalTile, Machine<GenericItemToChemicalTile>>,
                ItemBlockTooltip<BlockTile<GenericItemToChemicalTile, Machine<GenericItemToChemicalTile>>>>
                block = bundle.blocks.register(
                        path,
                        () -> new BlockTile<>(blockType,
                                props -> props.mapColor(BlockResourceInfo.STEEL.getMapColor())),
                        (b, props) -> new ItemBlockTooltip<>(b, true, props
                                .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                                .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.CHEMICAL_OUT_MACHINE)))
                .forItemHolder(holder -> holder
                        .addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                                .addBasic(def.maxChemical())
                                .build())
                        .addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                                .addInput(recipeType, SingleInputRecipeCache::containsInput)
                                .addChemicalDrainSlot(0)
                                .addEnergy()
                                .build()));

        // ── 5. Recipe serializer ────────────────────────────────────────────
        DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericItemToChemicalRecipe>> recipeSerializer =
                bundle.recipeSerializers.register(
                        path,
                        () -> MekanismRecipeSerializer.itemToChemical(
                                GenericItemToChemicalRecipe.getFactory(
                                        recipeType,
                                        def.id(),
                                        (DeferredHolder) serializerHolder[0]),
                                ChemicalStack.MAP_CODEC,
                                ChemicalStack.STREAM_CODEC));
        serializerHolder[0] = recipeSerializer;

        // ── 6. Tile entity type ─────────────────────────────────────────────
        TileEntityTypeRegistryObject<GenericItemToChemicalTile> tileEntity =
                bundle.tileEntities.mekBuilder(
                                block,
                                GenericItemToChemicalTile.factory(block, recipeType, def))
                        .clientTicker(TileEntityMekanism::tickClient)
                        .serverTicker(TileEntityMekanism::tickServer)
                        .withSimple(Capabilities.CONFIG_CARD)
                        .build();
        tileHolder[0] = tileEntity;

        // ── 7. Container type ───────────────────────────────────────────────
        ContainerTypeRegistryObject<MekanismTileContainer<GenericItemToChemicalTile>> containerType =
                bundle.containerTypes.register(block, GenericItemToChemicalTile.class);
        containerHolder[0] = containerType;

        return new RegisteredMachineImpl(def, block, tileEntity, recipeType, containerType);
    }
}
