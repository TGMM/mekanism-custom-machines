package com.kaliumstudios.mekanismcustommachines.impl.asset;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import com.kaliumstudios.mekanismcustommachines.api.MachineRegistry;
import com.kaliumstudios.mekanismcustommachines.api.MekanismCustomMachinesAPI;
import com.kaliumstudios.mekanismcustommachines.api.handle.RegisteredMachine;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;

/**
 * Virtual resource pack that synthesises blockstate JSON, item-model JSON and
 * language entries for every machine currently in {@link MachineRegistry}.
 * <p>
 * Custom machines never ship per-machine textures. Instead, each
 * {@link com.kaliumstudios.mekanismcustommachines.api.definition.MachineDefinition}
 * subtype is aliased through {@link FactoryTypeAssetResolver} to a Mekanism
 * machine whose blockstate/model is reused verbatim (with the rotation
 * variants and active/inactive split that come with it).
 * <p>
 * Registered with the pack repository through
 * {@link net.neoforged.neoforge.event.AddPackFindersEvent} on the mod event
 * bus. Resources are generated on demand, so the pack reflects whichever
 * machines exist in the registry at the moment Minecraft queries it.
 */
public final class MachineAssetPack implements PackResources {

    public static final String PACK_ID = MekanismCustomMachinesAPI.MODID + ":machine_assets";

    private static final String BLOCKSTATES_PREFIX = "blockstates/";
    private static final String ITEM_MODELS_PREFIX = "models/item/";
    private static final String LANG_PATH = "lang/en_us.json";
    private static final String JSON_SUFFIX = ".json";

    private static final PackLocationInfo LOCATION = new PackLocationInfo(
            PACK_ID,
            Component.literal("Mekanism: Custom Machines (generated assets)"),
            PackSource.BUILT_IN,
            Optional.empty());

    private static final PackSelectionConfig SELECTION_CONFIG =
            new PackSelectionConfig(true, Pack.Position.TOP, false);

    /** Factory for the {@link Pack} that wraps this resources implementation. */
    public static Pack createPack() {
        // Pack.ResourcesSupplier has two methods, so we can't use a lambda.
        Pack.ResourcesSupplier supplier = new Pack.ResourcesSupplier() {
            @Override
            public PackResources openPrimary(PackLocationInfo info) {
                return new MachineAssetPack();
            }

            @Override
            public PackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
                return new MachineAssetPack();
            }
        };
        return Pack.readMetaAndCreate(LOCATION, supplier, PackType.CLIENT_RESOURCES, SELECTION_CONFIG);
    }

    private MachineAssetPack() {}

    @Override
    public PackLocationInfo location() {
        return LOCATION;
    }

    @Override
    public IoSupplier<InputStream> getRootResource(String... elements) {
        if (elements.length == 1 && PACK_META.equals(elements[0])) {
            return supplierOf(packMetaJson());
        }
        return null;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type != PackType.CLIENT_RESOURCES) {
            return null;
        }

        String path = location.getPath();

        // blockstates/<machine_path>.json
        if (path.startsWith(BLOCKSTATES_PREFIX) && path.endsWith(JSON_SUFFIX)) {
            String machinePath = stripBoth(path, BLOCKSTATES_PREFIX, JSON_SUFFIX);
            return findMachine(location.getNamespace(), machinePath)
                    .map(this::blockstateJsonFor)
                    .map(MachineAssetPack::supplierOf)
                    .orElse(null);
        }

        // models/item/<machine_path>.json
        if (path.startsWith(ITEM_MODELS_PREFIX) && path.endsWith(JSON_SUFFIX)) {
            String machinePath = stripBoth(path, ITEM_MODELS_PREFIX, JSON_SUFFIX);
            return findMachine(location.getNamespace(), machinePath)
                    .map(this::itemModelJsonFor)
                    .map(MachineAssetPack::supplierOf)
                    .orElse(null);
        }

        // lang/en_us.json (per namespace)
        if (path.equals(LANG_PATH)) {
            String langJson = langJsonFor(location.getNamespace());
            return langJson == null ? null : supplierOf(langJson);
        }

        return null;
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput resourceOutput) {
        if (type != PackType.CLIENT_RESOURCES) {
            return;
        }
        for (RegisteredMachine machine : MachineRegistry.all()) {
            if (!machine.id().getNamespace().equals(namespace)) {
                continue;
            }
            String machinePath = machine.id().getPath();

            ResourceLocation bs = ResourceLocation.fromNamespaceAndPath(
                    namespace, BLOCKSTATES_PREFIX + machinePath + JSON_SUFFIX);
            if (bs.getPath().startsWith(path)) {
                resourceOutput.accept(bs, supplierOf(blockstateJsonFor(machine)));
            }

            ResourceLocation item = ResourceLocation.fromNamespaceAndPath(
                    namespace, ITEM_MODELS_PREFIX + machinePath + JSON_SUFFIX);
            if (item.getPath().startsWith(path)) {
                resourceOutput.accept(item, supplierOf(itemModelJsonFor(machine)));
            }
        }

        ResourceLocation lang = ResourceLocation.fromNamespaceAndPath(namespace, LANG_PATH);
        if (lang.getPath().startsWith(path)) {
            String langJson = langJsonFor(namespace);
            if (langJson != null) {
                resourceOutput.accept(lang, supplierOf(langJson));
            }
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        if (type != PackType.CLIENT_RESOURCES) {
            return Set.of();
        }
        Set<String> namespaces = new HashSet<>();
        for (RegisteredMachine machine : MachineRegistry.all()) {
            namespaces.add(machine.id().getNamespace());
        }
        return namespaces;
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) throws IOException {
        return null;
    }

    @Override
    public void close() {}

    // ── JSON generation ──────────────────────────────────────────────────

    private String blockstateJsonFor(RegisteredMachine machine) {
        String analog = FactoryTypeAssetResolver.mekanismAnalog(machine.definition());
        String baseModel = "mekanism:block/" + analog;
        String activeModel = "mekanism:block/" + analog;
        // Mekanism's blockstate JSONs reference an "_active" suffix for the
        // active variant, but not every machine has one. We optimistically use
        // it; if it doesn't exist the renderer falls back to the base model.
        // (Verified: enrichment_chamber, osmium_compressor, combiner,
        // precision_sawmill, chemical_oxidizer, chemical_crystallizer,
        // isotopic_centrifuge, and nutritional_liquifier all have _active
        // variants in Mekanism 10.7.x.)
        activeModel = baseModel + "_active";

        return """
                {
                  "variants": {
                    "facing=north,active=false": { "model": "%1$s" },
                    "facing=south,active=false": { "model": "%1$s", "y": 180 },
                    "facing=east,active=false":  { "model": "%1$s", "y":  90 },
                    "facing=west,active=false":  { "model": "%1$s", "y": -90 },
                    "facing=north,active=true":  { "model": "%2$s" },
                    "facing=south,active=true":  { "model": "%2$s", "y": 180 },
                    "facing=east,active=true":   { "model": "%2$s", "y":  90 },
                    "facing=west,active=true":   { "model": "%2$s", "y": -90 }
                  }
                }
                """.formatted(baseModel, activeModel);
    }

    private String itemModelJsonFor(RegisteredMachine machine) {
        String analog = FactoryTypeAssetResolver.mekanismAnalog(machine.definition());
        return "{\"parent\": \"mekanism:block/" + analog + "\"}";
    }

    @Nullable
    private String langJsonFor(String namespace) {
        Map<String, String> entries = new HashMap<>();
        for (RegisteredMachine machine : MachineRegistry.all()) {
            if (!machine.id().getNamespace().equals(namespace)) {
                continue;
            }
            String displayName = humaniseId(machine.id().getPath());
            entries.put("block." + namespace + "." + machine.id().getPath(), displayName);
            entries.put("item." + namespace + "." + machine.id().getPath(), displayName);
        }
        if (entries.isEmpty()) {
            return null;
        }
        JsonObject json = new JsonObject();
        entries.forEach(json::addProperty);
        return json.toString();
    }

    private static String humaniseId(String path) {
        String[] parts = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) sb.append(part.substring(1));
        }
        return sb.toString();
    }

    private static String packMetaJson() {
        int packFormat = SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES);
        return "{\"pack\": {\"description\": \"Mekanism: Custom Machines virtual assets\", \"pack_format\": "
                + packFormat + "}}";
    }

    // ── Utilities ────────────────────────────────────────────────────────

    private static Optional<RegisteredMachine> findMachine(String namespace, String path) {
        return MachineRegistry.get(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    private static String stripBoth(String s, String prefix, String suffix) {
        return s.substring(prefix.length(), s.length() - suffix.length());
    }

    private static IoSupplier<InputStream> supplierOf(String content) {
        byte[] bytes = content.getBytes();
        return () -> new ByteArrayInputStream(bytes);
    }
}
