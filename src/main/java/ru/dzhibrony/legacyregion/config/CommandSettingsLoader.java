package ru.dzhibrony.legacyregion.config;

import cn.nukkit.utils.Config;

final class CommandSettingsLoader {

    private final Config config;

    CommandSettingsLoader(Config config) {
        this.config = config;
    }

    CommandSettings load() {
        return new CommandSettings(
                this.config.getString("command.name", "rg"),
                this.config.getStringList("command.aliases"),
                this.config.getString("command.description", "Открыть меню регионов"),
                this.config.getString("command.permission", ""),
                this.config.getString("command.permission-message", "&cУ вас нет прав на эту команду.")
        );
    }
}
