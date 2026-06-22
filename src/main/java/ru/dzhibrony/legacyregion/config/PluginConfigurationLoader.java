package ru.dzhibrony.legacyregion.config;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;

import java.io.File;

public final class PluginConfigurationLoader {

    private static final String MESSAGES_FILE = "messages.yml";
    private static final String MENUS_FILE = "menus.yml";
    private static final String[] DEFAULT_REGION_FILES = {
            "regions/diamond_region.yml",
            "regions/emerald_region.yml",
            "regions/ancient_debris_region.yml",
            "regions/netherite_region.yml"
    };

    private final PluginBase plugin;

    public PluginConfigurationLoader(PluginBase plugin) {
        this.plugin = plugin;
    }

    public PluginConfiguration load() {
        this.saveDefaults();
        Config config = this.reloadMainConfig();
        Config messages = this.resourceConfig(MESSAGES_FILE);
        Config menus = this.resourceConfig(MENUS_FILE);
        return new PluginConfiguration(
                new DatabaseSettingsLoader(config).load(),
                new CommandSettingsLoader(config).load(),
                new ProtectionSettingsLoader(config).load(),
                new ParticleSettingsLoader(config).load(),
                new HologramSettingsLoader(config).load(),
                new TransferSettingsLoader(config).load(),
                new MessagesLoader(messages).load(),
                new MenuTextLoader(menus).load(),
                new RegionDefinitionLoader(config, this.plugin.getDataFolder()).load()
        );
    }

    private void saveDefaults() {
        this.plugin.saveDefaultConfig();
        this.plugin.saveResource(MESSAGES_FILE, false);
        this.plugin.saveResource(MENUS_FILE, false);
        for (String file : DEFAULT_REGION_FILES) {
            this.plugin.saveResource(file, false);
        }
    }

    private Config reloadMainConfig() {
        this.plugin.reloadConfig();
        return this.plugin.getConfig();
    }

    private Config resourceConfig(String fileName) {
        return new Config(new File(this.plugin.getDataFolder(), fileName));
    }
}
