package com.kaliumstudios.mekanismcustommachines.impl.pipeline;

import com.kaliumstudios.mekanismcustommachines.api.definition.ItemToItemDefinition;
import com.kaliumstudios.mekanismcustommachines.impl.mixin.MekanismRecipeTypeMixin;
import com.kaliumstudios.mekanismcustommachines.impl.recipe.GenericItemToItemRecipe;
import com.kaliumstudios.mekanismcustommachines.impl.registry.RegisteredMachineImpl;
import com.kaliumstudios.mekanismcustommachines.impl.tile.GenericElectricMachineTile;

import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.common.attachments.component.AttachedEjector;
import mekanism.common.attachments.component.AttachedSideConfig;
import mekanism.common.attachments.containers.ContainerType;
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
 * Registers an Item→Item custom machine through the NeoForge deferred
 * registration system.
 * <p>
 * Back-references between holders are resolved lazily: the {@link Machine}
 * block type accepts a {@code Supplier<TileEntityTypeRegistryObject>} that is
 * only resolved when the tile type is first queried (after all
 * {@code RegisterEvent}s have fired). The same pattern applies to the
 * container type supplier passed to {@code withGui}.
 * <p>
 * Registration order within this method:
 * <ol>
 *   <li>Recipe type</li>
 *   <li>Block type descriptor (references tile + container via lazy suppliers)</li>
 *   <li>Block + item-block</li>
 *   <li>Recipe serializer (self-references serializer holder via lazy supplier)</li>
 *   <li>Tile entity type</li>
 *   <li>Container type</li>
 * </ol>
 */
final class ItemToItemRegistrar implements MachineRegistrar<ItemToItemDefinition> {

    static final ItemToItemRegistrar INSTANCE = new ItemToItemRegistrar();

    private ItemToItemRegistrar() {}

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public RegisteredMachineImpl register(ItemToItemDefinition def, DeferredBundle bundle) {
        String path = def.id().getPath();

        // ── 1. Recipe type ──────────────────────────────────────────────────
        RecipeTypeRegistryObject<SingleRecipeInput, ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>>
                recipeType = bundle.recipeTypes.registerMek(
                        path,
                        name -> MekanismRecipeTypeMixin.invokeConstructor(
                                name,
                                rt -> new SingleItem<>(rt, ItemStackToItemStackRecipe::getInput)));

        // ── 2. Lazy holder references ───────────────────────────────────────
        // These arrays are effectively final captures used inside lambdas below.
        // They are populated after each registration call and read lazily by
        // Mekanism when the block type resolves its tile/container references.
        TileEntityTypeRegistryObject[] tileHolder = new TileEntityTypeRegistryObject[1];
        ContainerTypeRegistryObject[] containerHolder = new ContainerTypeRegistryObject[1];
        DeferredHolder[] serializerHolder = new DeferredHolder[1];

        // ── 3. Block type (non-factory — no AttributeUpgradeable) ───────────
        // The builder chain returns BlockType due to wildcard self-type erasure;
        // we cast to Machine<TILE> which is always correct since createMachine()
        // constructs a Machine<TILE> internally.
        MachineBuilder<Machine<GenericElectricMachineTile>, GenericElectricMachineTile, ?> builder =
                MachineBuilder.createMachine(
                        () -> tileHolder[0],
                        () -> def.processName());
        builder.withGui(() -> containerHolder[0]);
        builder.withSound(def.sound());
        builder.withEnergyConfig(def.energy().usage(), def.energy().storage());
        builder.with(AttributeSideConfig.ELECTRIC_MACHINE);
        @SuppressWarnings("unchecked")
        Machine<GenericElectricMachineTile> blockType =
                (Machine<GenericElectricMachineTile>) builder.build();

        // ── 4. Block + item-block ───────────────────────────────────────────
        BlockRegistryObject<BlockTile<GenericElectricMachineTile, Machine<GenericElectricMachineTile>>,
                ItemBlockTooltip<BlockTile<GenericElectricMachineTile, Machine<GenericElectricMachineTile>>>>
                block = bundle.blocks.register(
                        path,
                        () -> new BlockTile<>(blockType,
                                props -> props.mapColor(BlockResourceInfo.STEEL.getMapColor())),
                        (b, props) -> new ItemBlockTooltip<>(b, true, props
                                .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                                .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.ELECTRIC_MACHINE)))
                .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM,
                        () -> ItemSlotsBuilder.builder()
                                .addInput(recipeType, SingleInputRecipeCache::containsInput)
                                .addOutput()
                                .addEnergy()
                                .build()));

        // ── 5. Recipe serializer ────────────────────────────────────────────
        DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericItemToItemRecipe>> recipeSerializer =
                bundle.recipeSerializers.register(
                        path,
                        () -> MekanismRecipeSerializer.itemToItem(
                                GenericItemToItemRecipe.getFactory(
                                        recipeType,
                                        def.id(),
                                        (DeferredHolder) serializerHolder[0])));
        serializerHolder[0] = recipeSerializer;

        // ── 6. Tile entity type ─────────────────────────────────────────────
        TileEntityTypeRegistryObject<GenericElectricMachineTile> tileEntity =
                bundle.tileEntities.mekBuilder(
                                block,
                                GenericElectricMachineTile.factory(block, recipeType, def))
                        .clientTicker(TileEntityMekanism::tickClient)
                        .serverTicker(TileEntityMekanism::tickServer)
                        .withSimple(Capabilities.CONFIG_CARD)
                        .build();
        tileHolder[0] = tileEntity;

        // ── 7. Container type ───────────────────────────────────────────────
        ContainerTypeRegistryObject<MekanismTileContainer<GenericElectricMachineTile>> containerType =
                bundle.containerTypes.register(block, GenericElectricMachineTile.class);
        containerHolder[0] = containerType;

        return new RegisteredMachineImpl(def, block, tileEntity, recipeType, containerType);
    }
}
