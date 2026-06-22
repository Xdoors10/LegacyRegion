package ru.dzhibrony.legacyregion.config;

import cn.nukkit.utils.Config;

final class HologramSettingsLoader {

    private final Config config;

    HologramSettingsLoader(Config config) {
        this.config = config;
    }

    HologramSettings load() {
        return new HologramSettings(
                this.config.getBoolean("hologram.enabled", true),
                this.config.getBoolean("hologram.require-line-of-sight", true),
                this.config.getString("hologram.title", ""),
                this.config.getString("hologram.text", "&fРегион&7: {name}\n&fВладелец&7: &6{owner}"),
                this.config.getDouble("hologram.y-offset", 2.2),
                this.config.getDouble("hologram.view-distance", 24.0),
                this.config.getInt("hologram.update-interval-ticks", 10)
        );
    }
}
