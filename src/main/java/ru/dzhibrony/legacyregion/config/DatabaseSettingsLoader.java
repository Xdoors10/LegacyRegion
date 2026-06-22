package ru.dzhibrony.legacyregion.config;

import cn.nukkit.utils.Config;

final class DatabaseSettingsLoader {

    private final Config config;

    DatabaseSettingsLoader(Config config) {
        this.config = config;
    }

    DatabaseSettings load() {
        return new DatabaseSettings(
                this.config.getString("storage.type", "sqlite"),
                this.config.getString("storage.sqlite-file", "database.db"),
                this.config.getString("storage.mysql.host", "127.0.0.1:3306"),
                this.config.getString("storage.mysql.database", "legacy_region"),
                this.config.getString("storage.mysql.user", "root"),
                this.config.getString("storage.mysql.password", "")
        );
    }
}
