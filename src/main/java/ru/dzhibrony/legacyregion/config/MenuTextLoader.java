package ru.dzhibrony.legacyregion.config;

import cn.nukkit.utils.Config;

final class MenuTextLoader {

    private final Config config;

    MenuTextLoader(Config config) {
        this.config = config;
    }

    MenuText load() {
        return new MenuText(
                this.text("regions-title", "Ваши регионы"),
                this.text("regions-content", "Выберите свой блок региона"),
                this.text("region-title-format", "{x} {y} {z}"),
                this.text("region-button-format", "{name}\n{x} {y} {z}"),
                this.text("region-button-hidden-format", "{name}\n{hidden}"),
                this.text("add-member-button", "Добавь игрока\nв регион"),
                this.text("remove-member-button", "Удалить игрока\nиз региона"),
                this.text("settings-button", "Настройки региона"),
                this.text("add-member-title", "Добавление игрока в регион"),
                this.text("add-member-label", "Укажите ник игрока которого хотите добавить в свой регион"),
                this.text("add-member-input", "Ник игрока"),
                this.text("apply-button", "Применить"),
                this.text("remove-member-title", "Удаление игрока из региона"),
                this.text("remove-member-label", "Выберите игрока которого хотите удалить из региона"),
                this.text("remove-member-dropdown", "Выбрать игрока"),
                this.text("settings-title", "Настройка региона"),
                this.text("settings-label", "Тут можно немного настроить ваш регион"),
                this.text("allow-members-break-region-block", "Разрешить ломать регион добавленным игрокам"),
                this.text("transfer-owner-dropdown", "Изменить владельца региона"),
                this.text("transfer-owner-no-change", "Не менять"),
                this.text("hide-coordinates", "Скрыть координаты привата из меню регионов"),
                this.text("delete-region", "Удалить регион")
        );
    }

    private String text(String key, String fallback) {
        return this.config.getString(key, fallback);
    }
}
