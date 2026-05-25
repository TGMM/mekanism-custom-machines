package com.kaliumstudios.mekanismcustommachines.integration.kubejs;

import com.kaliumstudios.mekanismcustommachines.api.MekanismCustomMachinesAPI;
import com.kaliumstudios.mekanismcustommachines.api.event.RegisterCustomMachinesEvent;
import com.kaliumstudios.mekanismcustommachines.integration.kubejs.recipe.KubeJSRecipeSchemas;
import com.kaliumstudios.mekanismcustommachines.integration.kubejs.startup.MekanismCustomMachinesEvents;
import com.kaliumstudios.mekanismcustommachines.integration.kubejs.startup.RegisterMachinesKubeEvent;

import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.neoforged.neoforge.common.NeoForge;

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
        // Subscribe before our @Mod constructor fires RegisterCustomMachinesEvent.
        // The handler bridges that NeoForge event to the KubeJS startup event,
        // letting scripts register machines synchronously while the registration
        // window is still open.
        NeoForge.EVENT_BUS.addListener(MekanismCustomMachinesKubeJSPlugin::onRegisterCustomMachines);
        MekanismCustomMachinesAPI.LOGGER.info("[KubeJS bridge] init complete");
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(MekanismCustomMachinesEvents.GROUP);
    }

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        KubeJSRecipeSchemas.registerAll(registry);
    }

    private static void onRegisterCustomMachines(RegisterCustomMachinesEvent event) {
        RegisterMachinesKubeEvent kubeEvent = new RegisterMachinesKubeEvent(event);
        MekanismCustomMachinesEvents.REGISTER_MACHINES.post(ScriptType.STARTUP, kubeEvent);
    }
}
