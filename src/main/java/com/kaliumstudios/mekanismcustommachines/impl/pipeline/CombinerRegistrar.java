package com.kaliumstudios.mekanismcustommachines.impl.pipeline;

import com.kaliumstudios.mekanismcustommachines.api.definition.CombinerDefinition;
import com.kaliumstudios.mekanismcustommachines.impl.mixin.MekanismRecipeTypeMixin;
import com.kaliumstudios.mekanismcustommachines.impl.recipe.GenericCombinerRecipe;
import com.kaliumstudios.mekanismcustommachines.impl.registry.RegisteredMachineImpl;
import com.kaliumstudios.mekanismcustommachines.impl.tile.GenericCombinerTile;

import mekanism.api.recipes.CombinerRecipe;
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
import mekanism.common.recipe.lookup.cache.InputRecipeCache.DoubleItem;
import mekanism.common.recipe.serializer.MekanismRecipeSerializer;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.RecipeTypeRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Registers a combiner-style (Item + Item → Item) custom machine.
 * <p>
 * Follows the same pattern as {@link ItemToItemRegistrar} but with:
 * <ul>
 *   <li>{@link InputRecipeCache.DoubleItem} as the input cache</li>
 *   <li>Two input slots (with {@code addInput} called twice on the
 *       {@link ItemSlotsBuilder})</li>
 *   <li>{@link AttributeSideConfig#EXTRA_MACHINE} on the block to expose the
 *       second input as an "extra" slot type</li>
 * </ul>
 */
final class CombinerRegistrar implements MachineRegistrar<CombinerDefinition> {

    static final CombinerRegistrar INSTANCE = new CombinerRegistrar();

    private CombinerRegistrar() {}

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public RegisteredMachineImpl register(CombinerDefinition def, DeferredBundle bundle) {
        String path = def.id().getPath();

        // ── 1. Recipe type ──────────────────────────────────────────────────
        RecipeTypeRegistryObject<RecipeInput, CombinerRecipe, DoubleItem<CombinerRecipe>>
                recipeType = bundle.recipeTypes.registerMek(
                        path,
                        name -> MekanismRecipeTypeMixin.invokeConstructor(
                                name,
                                rt -> new DoubleItem<>(rt,
                                        CombinerRecipe::getMainInput,
                                        CombinerRecipe::getExtraInput)));

        // ── 2. Lazy holder references ───────────────────────────────────────
        TileEntityTypeRegistryObject[] tileHolder = new TileEntityTypeRegistryObject[1];
        ContainerTypeRegistryObject[] containerHolder = new ContainerTypeRegistryObject[1];
        DeferredHolder[] serializerHolder = new DeferredHolder[1];

        // ── 3. Block type (non-factory) ─────────────────────────────────────
        MachineBuilder<Machine<GenericCombinerTile>, GenericCombinerTile, ?> builder =
                MachineBuilder.createMachine(
                        () -> tileHolder[0],
                        () -> def.processName());
        builder.withGui(() -> containerHolder[0]);
        builder.withSound(def.sound());
        builder.withEnergyConfig(def.energy().usage(), def.energy().storage());
        builder.with(AttributeSideConfig.ELECTRIC_MACHINE);
        @SuppressWarnings("unchecked")
        Machine<GenericCombinerTile> blockType =
                (Machine<GenericCombinerTile>) builder.build();

        // ── 4. Block + item-block ───────────────────────────────────────────
        BlockRegistryObject<BlockTile<GenericCombinerTile, Machine<GenericCombinerTile>>,
                ItemBlockTooltip<BlockTile<GenericCombinerTile, Machine<GenericCombinerTile>>>>
                block = bundle.blocks.register(
                        path,
                        () -> new BlockTile<>(blockType,
                                props -> props.mapColor(BlockResourceInfo.STEEL.getMapColor())),
                        (b, props) -> new ItemBlockTooltip<>(b, true, props
                                .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                                .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.EXTRA_MACHINE)))
                .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM,
                        () -> ItemSlotsBuilder.builder()
                                .addInput(recipeType, DoubleItem::containsInputA)
                                .addInput(recipeType, DoubleItem::containsInputB)
                                .addOutput()
                                .addEnergy()
                                .build()));

        // ── 5. Recipe serializer ────────────────────────────────────────────
        DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericCombinerRecipe>> recipeSerializer =
                bundle.recipeSerializers.register(
                        path,
                        () -> MekanismRecipeSerializer.combining(
                                GenericCombinerRecipe.getFactory(
                                        recipeType,
                                        def.id(),
                                        (DeferredHolder) serializerHolder[0])));
        serializerHolder[0] = recipeSerializer;

        // ── 6. Tile entity type ─────────────────────────────────────────────
        TileEntityTypeRegistryObject<GenericCombinerTile> tileEntity =
                bundle.tileEntities.mekBuilder(
                                block,
                                GenericCombinerTile.factory(block, recipeType, def))
                        .clientTicker(TileEntityMekanism::tickClient)
                        .serverTicker(TileEntityMekanism::tickServer)
                        .withSimple(Capabilities.CONFIG_CARD)
                        .build();
        tileHolder[0] = tileEntity;

        // ── 7. Container type ───────────────────────────────────────────────
        ContainerTypeRegistryObject<MekanismTileContainer<GenericCombinerTile>> containerType =
                bundle.containerTypes.register(block, GenericCombinerTile.class);
        containerHolder[0] = containerType;

        return new RegisteredMachineImpl(def, block, tileEntity, recipeType, containerType);
    }
}
