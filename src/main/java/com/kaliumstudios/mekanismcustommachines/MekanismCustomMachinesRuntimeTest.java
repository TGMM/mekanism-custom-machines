package com.kaliumstudios.mekanismcustommachines;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.kaliumstudios.mekanismcustommachines.mixin.MekanismRecipeTypeMixin;

import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.component.AttachedEjector;
import mekanism.common.attachments.component.AttachedSideConfig;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.item.ItemSlotsBuilder;
import mekanism.common.block.attribute.AttributeSideConfig;
import mekanism.common.block.prefab.BlockFactoryMachine;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.content.blocktype.Machine.FactoryMachine;
import mekanism.common.content.blocktype.Machine.MachineBuilder;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleItem;
import mekanism.common.recipe.lookup.cache.SingleInputRecipeCache;
import mekanism.common.recipe.serializer.MekanismRecipeSerializer;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.RecipeTypeDeferredRegister;
import mekanism.common.registration.impl.RecipeTypeRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MekanismCustomMachinesRuntimeTest {
        public static List<ContainerTypeRegistryObject<MekanismTileContainer<GenericMachineTileEntity>>> RegisterMachineAtRuntime(
                        IEventBus modEventBus, String modId,
                        MachineParameters parameters) {
                final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(modId);
                final ItemDeferredRegister ITEMS = new ItemDeferredRegister(modId);
                final TileEntityTypeDeferredRegister TILE_ENTITIES = new TileEntityTypeDeferredRegister(modId);
                final RecipeTypeDeferredRegister RECIPE_TYPES = new RecipeTypeDeferredRegister(modId);
                final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister
                                .create(Registries.RECIPE_SERIALIZER, modId);
                final ContainerTypeDeferredRegister CONTAINER_TYPES = new ContainerTypeDeferredRegister(
                                modId);

                BLOCKS.register(modEventBus);
                ITEMS.register(modEventBus);
                TILE_ENTITIES.register(modEventBus);
                RECIPE_TYPES.register(modEventBus);
                RECIPE_SERIALIZERS.register(modEventBus);
                CONTAINER_TYPES.register(modEventBus);

                BlockRegistryObject<BlockFactoryMachine<GenericMachineTileEntity, FactoryMachine<GenericMachineTileEntity>>, ItemBlockTooltip<BlockFactoryMachine<GenericMachineTileEntity, FactoryMachine<GenericMachineTileEntity>>>> machine = null;

                ResourceLocation processNameRL = ResourceLocation
                                .fromNamespaceAndPath(modId, parameters.processName);
                RecipeTypeRegistryObject<SingleRecipeInput, ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> recipeType = RegisterRecipeType(
                                RECIPE_TYPES,
                                processNameRL,
                                rt -> new SingleItem<>(rt,
                                                ItemStackToItemStackRecipe::getInput));

                AtomicReference<DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericItemToItemRecipe>>> recipeSerializer = new AtomicReference<>();
                AtomicReference<TileEntityTypeRegistryObject<GenericMachineTileEntity>> tileEntity = new AtomicReference<>();
                AtomicReference<ContainerTypeRegistryObject<MekanismTileContainer<GenericMachineTileEntity>>> containerType = new AtomicReference<>();

                FactoryMachine<GenericMachineTileEntity> blockType = MachineBuilder
                                .createFactoryMachine(() -> tileEntity.get(),
                                                // TODO: Use a different factory type
                                                MekanismLang.DESCRIPTION_ENRICHMENT_CHAMBER, FactoryType.ENRICHING)
                                .withGui(() -> containerType.get())
                                .withSound(MekanismSounds.ENRICHMENT_CHAMBER)
                                .withEnergyConfig(MekanismConfig.usage.enrichmentChamber,
                                                MekanismConfig.storage.enrichmentChamber)
                                .with(AttributeSideConfig.ELECTRIC_MACHINE)
                                .build();

                machine = BLOCKS
                                .register(parameters.machineName,
                                                () -> new BlockFactoryMachine<>(
                                                                blockType,
                                                                properties -> properties.mapColor(
                                                                                BlockResourceInfo.STEEL.getMapColor())),
                                                (block, properties) -> new ItemBlockTooltip<>(block, true, properties
                                                                .component(MekanismDataComponents.EJECTOR,
                                                                                AttachedEjector.DEFAULT)
                                                                .component(MekanismDataComponents.SIDE_CONFIG,
                                                                                AttachedSideConfig.ELECTRIC_MACHINE)))
                                .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM,
                                                () -> ItemSlotsBuilder.builder()
                                                                .addInput(recipeType,
                                                                                SingleInputRecipeCache::containsInput)
                                                                .addOutput()
                                                                .addEnergy()
                                                                .build()));

                recipeSerializer.set(RECIPE_SERIALIZERS.register(parameters.processName,
                                () -> MekanismRecipeSerializer.itemToItem(
                                                GenericItemToItemRecipe.getFactory(
                                                                recipeType,
                                                                parameters.machineName,
                                                                recipeSerializer.get()))));

                tileEntity.set(TILE_ENTITIES
                                .mekBuilder(machine,
                                                GenericMachineTileEntity.getFactory(machine, recipeType))
                                .clientTicker(TileEntityMekanism::tickClient)
                                .serverTicker(TileEntityMekanism::tickServer)
                                .withSimple(Capabilities.CONFIG_CARD)
                                .build());

                containerType.set(CONTAINER_TYPES
                                .register(machine,
                                                GenericMachineTileEntity.class));

                return List.of(containerType.get());
        }

        public static <VANILLA_INPUT extends RecipeInput, RECIPE extends MekanismRecipe<VANILLA_INPUT>, INPUT_CACHE extends IInputRecipeCache> RecipeTypeRegistryObject<VANILLA_INPUT, RECIPE, INPUT_CACHE> RegisterRecipeType(
                        RecipeTypeDeferredRegister recipeTypeDeferredRegister,
                        ResourceLocation name,
                        Function<MekanismRecipeType<VANILLA_INPUT, RECIPE, INPUT_CACHE>, INPUT_CACHE> inputCacheCreator) {
                return recipeTypeDeferredRegister.registerMek(name.getPath(),
                                registryName -> MekanismRecipeTypeMixin.invokeConstructor(registryName,
                                                inputCacheCreator));
        }
}
