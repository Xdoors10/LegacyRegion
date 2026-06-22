package ru.dzhibrony.legacyregion.config;

import cn.nukkit.utils.Config;

final class ProtectionSettingsLoader {

    private final Config config;

    ProtectionSettingsLoader(Config config) {
        this.config = config;
    }

    ProtectionSettings load() {
        return new ProtectionSettings(
                this.config.getBoolean("protection.op-bypass", true),
                this.config.getBoolean("protection.prevent-overlap", true),
                this.config.getBoolean("protection.deny-drop-item", true),
                this.config.getBoolean("protection.deny-entity-damage", false),
                this.config.getBoolean("protection.protect-fire-spread", true),
                this.config.getBoolean("protection.denied-feedback.message-enabled", false),
                this.config.getBoolean("protection.denied-feedback.particle-enabled", true),
                this.config.getString("protection.denied-feedback.particle", "minecraft:basic_smoke_particle")
        );
    }
}
