package ru.dzhibrony.legacyregion.config;

import ru.dzhibrony.legacyregion.model.RegionDefinition;

import java.util.List;

public record PluginConfiguration(
        DatabaseSettings database,
        CommandSettings command,
        ProtectionSettings protection,
        ParticleSettings particles,
        HologramSettings holograms,
        TransferSettings transfer,
        Messages messages,
        MenuText menu,
        List<RegionDefinition> regionDefinitions
) {
}
