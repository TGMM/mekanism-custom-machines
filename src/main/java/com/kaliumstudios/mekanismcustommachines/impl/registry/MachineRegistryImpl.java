package com.kaliumstudios.mekanismcustommachines.impl.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.kaliumstudios.mekanismcustommachines.api.MachineRegistry;
import com.kaliumstudios.mekanismcustommachines.api.definition.MachineDefinition;
import com.kaliumstudios.mekanismcustommachines.api.handle.RegisteredMachine;
import com.kaliumstudios.mekanismcustommachines.impl.pipeline.DeferredBundle;
import com.kaliumstudios.mekanismcustommachines.impl.pipeline.RegistrarDispatcher;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;

/**
 * Implementation of {@link MachineRegistry.Delegate}.
 * <p>
 * Maintains a {@link LinkedHashMap} of all registered machines keyed by their
 * {@link ResourceLocation} id, preserving insertion order for screen registration.
 * <p>
 * One {@link DeferredBundle} is created per namespace (mod id). Multiple machines
 * under the same namespace share the same bundle and therefore the same set of
 * deferred registers — avoiding duplicate event bus subscriptions.
 */
public final class MachineRegistryImpl implements MachineRegistry.Delegate {

    private final Map<ResourceLocation, RegisteredMachine> machines = new LinkedHashMap<>();
    private final Map<String, DeferredBundle> bundlesByNamespace = new LinkedHashMap<>();
    private final IEventBus modEventBus;
    private boolean frozen = false;

    public MachineRegistryImpl(IEventBus modEventBus) {
        this.modEventBus = modEventBus;
    }

    @Override
    public RegisteredMachine register(MachineDefinition definition) {
        if (frozen) {
            throw new IllegalStateException(
                    "Cannot register machine '" + definition.id() +
                    "' — the registration window has already closed. " +
                    "Machines must be registered during @Mod construction via RegisterCustomMachinesEvent.");
        }

        ResourceLocation id = definition.id();
        if (machines.containsKey(id)) {
            throw new IllegalArgumentException("A machine with id '" + id + "' is already registered.");
        }

        DeferredBundle bundle = bundlesByNamespace.computeIfAbsent(
                id.getNamespace(),
                ns -> new DeferredBundle(ns, modEventBus));

        RegisteredMachine registered = RegistrarDispatcher.dispatch(definition, bundle);
        machines.put(id, registered);
        return registered;
    }

    @Override
    public Collection<RegisteredMachine> all() {
        return Collections.unmodifiableCollection(machines.values());
    }

    @Override
    public Optional<RegisteredMachine> get(ResourceLocation id) {
        return Optional.ofNullable(machines.get(id));
    }

    /**
     * Called by the main mod class after the mod event bus registration window
     * closes to prevent late registrations.
     */
    public void freeze() {
        this.frozen = true;
    }
}
