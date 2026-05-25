package com.kaliumstudios.mekanismcustommachines.integration.kubejs;

import com.kaliumstudios.mekanismcustommachines.api.MachineRegistry;
import com.kaliumstudios.mekanismcustommachines.api.MekanismCustomMachinesAPI;
import com.kaliumstudios.mekanismcustommachines.api.event.RegisterCustomMachinesEvent;
import com.kaliumstudios.mekanismcustommachines.integration.kubejs.recipe.KubeJSRecipeSchemas;
import com.kaliumstudios.mekanismcustommachines.integration.kubejs.startup.MekanismCustomMachinesEvents;
import com.kaliumstudios.mekanismcustommachines.integration.kubejs.startup.RegisterMachinesKubeEvent;

import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.script.ScriptType;

/**
 * KubeJS plugin that bridges scripts to the
 * {@link com.kaliumstudios.mekanismcustommachines.api.MachineRegistry MachineRegistry}.
 * <p>
 * Two surfaces are exposed:
 * <ol>
 *   <li><strong>Machine type registration</strong> (startup) — scripts call
 *       {@code MekanismCustomMachines.registerMachines(...)} to define new
 *       machines. Listeners are collected as the script loads; when our
 *       {@code @Mod} constructor fires
 *       {@link RegisterCustomMachinesEvent}, we post the corresponding
 *       KubeJS event so each listener runs and forwards its definition to the
 *       registry.</li>
 *   <li><strong>Recipe schemas</strong> (server reload) — for every machine
 *       in the registry, we register a {@code RecipeSchema} matching the
 *       machine's Mekanism recipe-serializer codec. Scripts can then add
 *       recipes via {@code ServerEvents.recipes(event => event.recipes.<ns>.<name>(...))}.</li>
 * </ol>
 * <p>
 * <strong>Load-order constraint:</strong> this mod must be loaded <em>after</em>
 * KubeJS so that its startup script manager has already executed (and any
 * {@code MekanismCustomMachines.registerMachines(...)} listeners are already
 * attached) by the time our {@code @Mod} constructor runs. The dependency is
 * declared in {@code neoforge.mods.toml}.
 */
public class MekanismCustomMachinesKubeJSPlugin implements KubeJSPlugin {

    @Override
    public void init() {
        MekanismCustomMachinesAPI.LOGGER.info("[KubeJS bridge] init() called — plugin loaded");
        // Subscribe before our @Mod constructor fires RegisterCustomMachinesEvent.
        // The handler bridges that NeoForge event to the KubeJS startup event,
        // letting scripts register machines synchronously while the registration
        // window is still open. We subscribe to MekanismCustomMachinesAPI.EVENT_BUS
        // (not NeoForge.EVENT_BUS) because the latter is shut down during mod
        // construction and would silently drop the event.
        MekanismCustomMachinesAPI.EVENT_BUS.addListener(
                RegisterCustomMachinesEvent.class,
                MekanismCustomMachinesKubeJSPlugin::onRegisterCustomMachines);
        MekanismCustomMachinesAPI.LOGGER.info(
                "[KubeJS bridge] subscribed to RegisterCustomMachinesEvent on MekanismCustomMachinesAPI.EVENT_BUS");
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(MekanismCustomMachinesEvents.GROUP);
        MekanismCustomMachinesAPI.LOGGER.info(
                "[KubeJS bridge] registered event group '{}' (handlers: {})",
                MekanismCustomMachinesEvents.GROUP.name,
                MekanismCustomMachinesEvents.GROUP.getHandlers().keySet());
    }

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        MekanismCustomMachinesAPI.LOGGER.info(
                "[KubeJS bridge] registerRecipeSchemas — machines available: {}",
                MachineRegistry.all().stream().map(m -> m.id().toString()).toList());
        KubeJSRecipeSchemas.registerAll(registry);
    }

    private static void onRegisterCustomMachines(RegisterCustomMachinesEvent event) {
        MekanismCustomMachinesAPI.LOGGER.info(
                "[KubeJS bridge] received RegisterCustomMachinesEvent — bridging to script handlers");
        RegisterMachinesKubeEvent kubeEvent = new RegisterMachinesKubeEvent(event);
        boolean hadListeners = MekanismCustomMachinesEvents.REGISTER_MACHINES.hasListeners();
        if (!hadListeners) {
            MekanismCustomMachinesAPI.LOGGER.warn(
                    "[KubeJS bridge] NO script listeners are registered for MekanismCustomMachines.registerMachines. "
                    + "Either no startup script called it, scripts ran AFTER our @Mod constructor "
                    + "(load-order bug), or the event group binding is broken.");
        }
        MekanismCustomMachinesEvents.REGISTER_MACHINES.post(ScriptType.STARTUP, kubeEvent);
        MekanismCustomMachinesAPI.LOGGER.info(
                "[KubeJS bridge] script handlers finished — {} machine(s) in registry now",
                MachineRegistry.all().size());
    }
}
