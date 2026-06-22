package ru.dzhibrony.legacyregion.config;

import cn.nukkit.utils.Config;

final class ParticleSettingsLoader {

    private final Config config;

    ParticleSettingsLoader(Config config) {
        this.config = config;
    }

    ParticleSettings load() {
        return new ParticleSettings(
                this.config.getBoolean("install-mode.particles.enabled", true),
                this.config.getString("install-mode.particles.identifier", "minecraft:villager_angry"),
                this.config.getInt("install-mode.particles.duration-seconds", 5),
                this.config.getInt("install-mode.particles.interval-ticks", 10),
                this.config.getInt("install-mode.particles.step", 1)
        );
    }
}
