package ru.dzhibrony.legacyregion.config;

import cn.nukkit.utils.Config;

final class TransferSettingsLoader {

    private final Config config;

    TransferSettingsLoader(Config config) {
        this.config = config;
    }

    TransferSettings load() {
        return new TransferSettings(this.config.getBoolean("settings.transfer.add-old-owner-as-member", true));
    }
}
