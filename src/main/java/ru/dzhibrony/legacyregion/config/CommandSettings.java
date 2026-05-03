package ru.dzhibrony.legacyregion.config;

import java.util.List;

public record CommandSettings(
        String name,
        List<String> aliases,
        String description,
        String permission,
        String permissionMessage
) {
}
