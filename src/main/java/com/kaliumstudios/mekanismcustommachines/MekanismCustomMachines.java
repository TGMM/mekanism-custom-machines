package com.kaliumstudios.mekanismcustommachines;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(MekanismCustomMachines.MODID)
public class MekanismCustomMachines {
    public static final String MODID = "mekanismcustommachines";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(MODID);
    public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(MODID);
    public static final TileEntityTypeDeferredRegister TILE_ENTITIES = new TileEntityTypeDeferredRegister(MODID);

    public MekanismCustomMachines(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        TILE_ENTITIES.register(modEventBus);
    }
}
