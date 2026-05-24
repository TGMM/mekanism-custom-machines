package com.kaliumstudios.mekanismcustommachines.impl;

import com.kaliumstudios.mekanismcustommachines.api.MekanismCustomMachinesAPI;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = MekanismCustomMachinesAPI.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = MekanismCustomMachinesAPI.MODID, value = Dist.CLIENT)
public class MekanismCustomMachinesClient {

    public MekanismCustomMachinesClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        MekanismCustomMachinesAPI.LOGGER.info("MekanismCustomMachines client setup complete. " +
                "Player: {}", Minecraft.getInstance().getUser().getName());
    }
}
