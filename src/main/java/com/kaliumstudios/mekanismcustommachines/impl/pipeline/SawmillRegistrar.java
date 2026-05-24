package com.kaliumstudios.mekanismcustommachines.impl.pipeline;

import com.kaliumstudios.mekanismcustommachines.api.definition.SawmillDefinition;
import com.kaliumstudios.mekanismcustommachines.impl.mixin.MekanismRecipeTypeMixin;
import com.kaliumstudios.mekanismcustommachines.impl.recipe.GenericSawmillRecipe;
import com.kaliumstudios.mekanismcustommachines.impl.registry.RegisteredMachineImpl;
import com.kaliumstudios.mekanismcustommachines.impl.tile.GenericSawmillTile;

import mekanism.api.recipes.SawmillRecipe;
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
import mekanism.common.recipe.serializer.SawmillRecipeSerializer;
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
 * Registers a sawmill-style (Item → Item + chance secondary) custom machine.
 * <p>
 * Differs from {@link ItemToItemRegistrar} in:
 * <ul>
 *   <li>Two output slots (primary + secondary chance) in the item-block container</li>
 *   <li>Uses {@link SawmillRecipeSerializer} (a custom serializer class, not
 *       a {@code MekanismRecipeSerializer.X} factory method)</li>
 * </ul>
 */
final class SawmillRegistrar implements MachineRegistrar<SawmillDefinition> {

    static final SawmillRegistrar INSTANCE = new SawmillRegistrar();

    private SawmillRegistrar() {}

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public RegisteredMachineImpl register(SawmillDefinition def, DeferredBundle bundle) {
        String path = def.id().getPath();

        // ── 1. Recipe type ──────────────────────────────────────────────────
        RecipeTypeRegistryObject<SingleRecipeInput, SawmillRecipe, SingleItem<SawmillRecipe>>
                recipeType = bundle.recipeTypes.registerMek(
                        path,
                        name -> MekanismRecipeTypeMixin.invokeConstructor(
                                name,
                                rt -> new SingleItem<>(rt, SawmillRecipe::getInput)));

        // ── 2. Lazy holder references ───────────────────────────────────────
        TileEntityTypeRegistryObject[] tileHolder = new TileEntityTypeRegistryObject[1];
        ContainerTypeRegistryObject[] containerHolder = new ContainerTypeRegistryObject[1];
        DeferredHolder[] serializerHolder = new DeferredHolder[1];

        // ── 3. Block type (non-factory) ─────────────────────────────────────
        MachineBuilder<Machine<GenericSawmillTile>, GenericSawmillTile, ?> builder =
                MachineBuilder.createMachine(
                        () -> tileHolder[0],
                        () -> def.processName());
        builder.withGui(() -> containerHolder[0]);
        builder.withSound(def.sound());
        builder.withEnergyConfig(def.energy().usage(), def.energy().storage());
        builder.with(AttributeSideConfig.ELECTRIC_MACHINE);
        @SuppressWarnings("unchecked")
        Machine<GenericSawmillTile> blockType =
                (Machine<GenericSawmillTile>) builder.build();

        // ── 4. Block + item-block ───────────────────────────────────────────
        BlockRegistryObject<BlockTile<GenericSawmillTile, Machine<GenericSawmillTile>>,
                ItemBlockTooltip<BlockTile<GenericSawmillTile, Machine<GenericSawmillTile>>>>
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
                                .addOutput()
                                .addEnergy()
                                .build()));

        // ── 5. Recipe serializer ────────────────────────────────────────────
        DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericSawmillRecipe>> recipeSerializer =
                bundle.recipeSerializers.register(
                        path,
                        () -> (RecipeSerializer<GenericSawmillRecipe>) (RecipeSerializer<?>) new SawmillRecipeSerializer(
                                GenericSawmillRecipe.getFactory(
                                        recipeType,
                                        def.id(),
                                        (DeferredHolder) serializerHolder[0])));
        serializerHolder[0] = recipeSerializer;

        // ── 6. Tile entity type ─────────────────────────────────────────────
        TileEntityTypeRegistryObject<GenericSawmillTile> tileEntity =
                bundle.tileEntities.mekBuilder(
                                block,
                                GenericSawmillTile.factory(block, recipeType, def))
                        .clientTicker(TileEntityMekanism::tickClient)
                        .serverTicker(TileEntityMekanism::tickServer)
                        .withSimple(Capabilities.CONFIG_CARD)
                        .build();
        tileHolder[0] = tileEntity;

        // ── 7. Container type ───────────────────────────────────────────────
        ContainerTypeRegistryObject<MekanismTileContainer<GenericSawmillTile>> containerType =
                bundle.containerTypes.register(block, GenericSawmillTile.class);
        containerHolder[0] = containerType;

        return new RegisteredMachineImpl(def, block, tileEntity, recipeType, containerType);
    }
}
