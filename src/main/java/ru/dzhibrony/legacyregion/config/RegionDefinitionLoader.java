package ru.dzhibrony.legacyregion.config;

import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import ru.dzhibrony.legacyregion.model.RegionDefinition;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

final class RegionDefinitionLoader {

    private final Config config;
    private final File regionDirectory;

    RegionDefinitionLoader(Config config, File dataFolder) {
        this.config = config;
        this.regionDirectory = new File(dataFolder, "regions");
    }

    List<RegionDefinition> load() {
        List<RegionDefinition> definitions = new ArrayList<>();
        for (ConfigSection entry : this.regionEntries()) {
            this.loadDefinition(entry).ifPresent(definitions::add);
        }
        return definitions;
    }

    private List<ConfigSection> regionEntries() {
        Object raw = this.config.get("block-region-register");
        if (raw instanceof List<?> list) {
            return this.listEntries(list);
        }
        if (raw instanceof ConfigSection section) {
            return this.sectionEntries(section);
        }
        return List.of();
    }

    private List<ConfigSection> listEntries(List<?> list) {
        List<ConfigSection> entries = new ArrayList<>();
        for (Object item : list) {
            this.asSection(item, "").ifPresent(entries::add);
        }
        return entries;
    }

    private List<ConfigSection> sectionEntries(ConfigSection section) {
        if (section.exists("file")) {
            return List.of(section);
        }
        List<ConfigSection> entries = new ArrayList<>();
        section.forEach((key, value) -> this.asSection(value, key).ifPresent(entries::add));
        return entries;
    }

    @SuppressWarnings("unchecked")
    private Optional<ConfigSection> asSection(Object value, String key) {
        if (value instanceof ConfigSection section) {
            section.putIfAbsent("name", key);
            return Optional.of(section);
        }
        if (value instanceof Map<?, ?> map) {
            return Optional.of(new ConfigSection((Map<String, Object>) map));
        }
        return Optional.empty();
    }

    private Optional<RegionDefinition> loadDefinition(ConfigSection entry) {
        String fileName = entry.getString("file", "");
        if (fileName.isBlank()) {
            return Optional.empty();
        }
        Config file = new Config(this.definitionFile(fileName));
        if (!file.getBoolean("enabled", true)) {
            return Optional.empty();
        }
        return Optional.of(this.definitionFrom(entry, fileName, file));
    }

    private File definitionFile(String fileName) {
        return new File(this.regionDirectory, fileName);
    }

    private RegionDefinition definitionFrom(ConfigSection entry, String fileName, Config file) {
        String key = this.definitionKey(fileName);
        ConfigSection block = file.getSection("block");
        String name = file.getString("name", entry.getString("name", key));
        return new RegionDefinition(
                key,
                name,
                fileName,
                block.getString("namespace-id", ""),
                block.getInt("legacy-id", -1),
                block.getInt("damage", -1),
                Math.max(1, file.getInt("radius", 1)),
                this.buttonIcon(file),
                file.getBoolean("break-from-explosions", false)
        );
    }

    private String buttonIcon(Config file) {
        if (!file.getBoolean("button-icon-enabled", true)) {
            return "";
        }
        return file.getString("button-icon", file.getString("menu-icon", ""));
    }

    private String definitionKey(String fileName) {
        String cleanName = fileName.replace('\\', '/');
        cleanName = cleanName.substring(cleanName.lastIndexOf('/') + 1);
        return cleanName.replace(".yml", "").replace(".yaml", "").toLowerCase(Locale.ROOT);
    }
}
