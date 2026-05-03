package ru.dzhibrony.legacyregion.config;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import ru.dzhibrony.legacyregion.model.RegionDefinition;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        Config messages = this.messagesConfig();
        Config menus = this.menusConfig();
        return new PluginConfiguration(
                this.database(config),
                this.command(config),
                this.protection(config),
                this.particles(config),
                this.holograms(config),
                this.transfer(config),
                this.messages(messages),
                this.menu(menus),
                this.regionDefinitions(config)
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

    private Config messagesConfig() {
        return new Config(new File(this.plugin.getDataFolder(), MESSAGES_FILE));
    }

    private Config menusConfig() {
        return new Config(new File(this.plugin.getDataFolder(), MENUS_FILE));
    }

    private DatabaseSettings database(Config config) {
        return new DatabaseSettings(
                config.getString("storage.type", "sqlite"),
                config.getString("storage.sqlite-file", "database.db"),
                config.getString("storage.mysql.host", "127.0.0.1:3306"),
                config.getString("storage.mysql.database", "legacy_region"),
                config.getString("storage.mysql.user", "root"),
                config.getString("storage.mysql.password", "")
        );
    }

    private CommandSettings command(Config config) {
        return new CommandSettings(
                config.getString("command.name", "rg"),
                config.getStringList("command.aliases"),
                config.getString("command.description", "Открыть меню регионов"),
                config.getString("command.permission", ""),
                config.getString("command.permission-message", "&cУ вас нет прав на эту команду.")
        );
    }

    private ProtectionSettings protection(Config config) {
        return new ProtectionSettings(
                config.getBoolean("protection.op-bypass", true),
                config.getBoolean("protection.prevent-overlap", true),
                config.getBoolean("protection.deny-drop-item", true),
                config.getBoolean("protection.deny-entity-damage", false),
                config.getBoolean("protection.protect-fire-spread", true),
                config.getBoolean("protection.denied-feedback.message-enabled", false),
                config.getBoolean("protection.denied-feedback.particle-enabled", true),
                config.getString("protection.denied-feedback.particle", "minecraft:basic_smoke_particle")
        );
    }

    private ParticleSettings particles(Config config) {
        return new ParticleSettings(
                config.getBoolean("install-mode.particles.enabled", true),
                config.getString("install-mode.particles.identifier", "minecraft:villager_angry"),
                config.getInt("install-mode.particles.duration-seconds", 5),
                config.getInt("install-mode.particles.interval-ticks", 10),
                config.getInt("install-mode.particles.step", 1)
        );
    }

    private HologramSettings holograms(Config config) {
        return new HologramSettings(
                config.getBoolean("hologram.enabled", true),
                config.getBoolean("hologram.require-line-of-sight", true),
                config.getString("hologram.title", ""),
                config.getString("hologram.text", "&fРегион&7: {name}\n&fВладелец&7: &6{owner}"),
                config.getDouble("hologram.y-offset", 2.2),
                config.getDouble("hologram.view-distance", 24.0),
                config.getInt("hologram.update-interval-ticks", 10)
        );
    }

    private TransferSettings transfer(Config config) {
        return new TransferSettings(config.getBoolean("settings.transfer.add-old-owner-as-member", true));
    }

    private Messages messages(Config messages) {
        Map<String, MessageChannel> channels = new LinkedHashMap<>();
        return new Messages(
                this.message(messages, "chat-message.prefix", "&f[Регионы]&r "),
                this.message(messages, "toast-message.title", "&fРегионы"),
                this.displayMessage(messages, Messages.PLAYER_ONLY, MessageChannel.CHAT, "&cЭта команда доступна только игроку.", channels),
                this.displayMessage(messages, Messages.USAGE_ADD_MEMBER, MessageChannel.CHAT, "&cИспользуйте: /rg addmember <ник>", channels),
                this.displayMessage(messages, Messages.USAGE_REMOVE_MEMBER, MessageChannel.CHAT, "&cИспользуйте: /rg removemember <ник>", channels),
                this.displayMessage(messages, Messages.INSTALL_MODE_ENTER, MessageChannel.CHAT, "&fВы вошли в режим установки, поставьте блок региона.", channels),
                this.displayMessage(messages, Messages.INSTALL_MODE_EXIT, MessageChannel.CHAT, "&fВы вышли из режима установки региона.", channels),
                this.displayMessage(messages, Messages.INSTALL_MODE_NOT_ACTIVE, MessageChannel.CHAT, "&cВы не находитесь в режиме установки региона.", channels),
                this.displayMessage(messages, Messages.NOT_REGION_BLOCK, MessageChannel.CHAT, "&cЭтот блок не зарегистрирован как блок региона.", channels),
                this.displayMessage(messages, Messages.REGION_CREATED, MessageChannel.CHAT, "&fРегион успешно создан", channels),
                this.displayMessage(messages, Messages.REGION_OVERLAP, MessageChannel.CHAT, "&cЭтот регион пересекается с уже созданным регионом.", channels),
                this.displayMessage(messages, Messages.ACTION_DENIED, MessageChannel.CHAT, "&cВы находитесь в чужом регионе", channels),
                this.message(messages, "chat-message.action-denied-display", "actionbar"),
                this.displayMessage(messages, Messages.NO_OWNED_REGIONS, MessageChannel.CHAT, "&cУ вас нет регионов.", channels),
                this.displayMessage(messages, Messages.NO_REGION_FOR_COMMAND, MessageChannel.CHAT, "&cВстаньте внутри своего региона или оставьте только один регион для команды без меню.", channels),
                this.displayMessage(messages, Messages.MEMBER_ADDED, MessageChannel.TOAST, "Игрок успешно добавлен в регион", channels),
                this.displayMessage(messages, Messages.MEMBER_REMOVED, MessageChannel.TOAST, "Игрок успешно удален из региона", channels),
                this.displayMessage(messages, Messages.PLAYER_NEVER_JOINED, MessageChannel.TOAST, "Такой игрок никогда не заходил на сервер", channels),
                this.displayMessage(messages, Messages.MEMBER_ALREADY_ADDED, MessageChannel.CHAT, "&cЭтот игрок уже добавлен в регион.", channels),
                this.displayMessage(messages, Messages.MEMBER_NOT_FOUND, MessageChannel.CHAT, "&cЭтот игрок не добавлен в регион.", channels),
                this.displayMessage(messages, Messages.OWNER_CHANGED, MessageChannel.CHAT, "&fВладелец региона изменен.", channels),
                this.displayMessage(messages, Messages.REGION_UPDATED, MessageChannel.CHAT, "&fНастройки региона сохранены.", channels),
                this.displayMessage(messages, Messages.REGION_DELETED, MessageChannel.CHAT, "&fРегион успешно удален.", channels),
                this.displayMessage(messages, Messages.CANNOT_ADD_YOURSELF, MessageChannel.CHAT, "&cВы уже владелец этого региона.", channels),
                this.displayMessage(messages, Messages.EMPTY_MEMBERS, MessageChannel.TOAST, "&cВ регионе нет добавленных игроков.", channels),
                this.message(messages, "chat-message.deleted-region-menu-name", "Удаленный регион"),
                this.message(messages, "chat-message.hidden-coordinates", "Координаты скрыты"),
                this.displayMessage(messages, Messages.RELOAD_SUCCESS, MessageChannel.CHAT, "&fКонфигурация и регионы перезагружены.", channels),
                this.displayMessage(messages, Messages.MEMBER_CANNOT_BREAK_REGION_BLOCK, MessageChannel.CHAT, "§6Владелец §cне разрешил §fвам §cломать §6регион§f.", channels),
                channels
        );
    }

    private String displayMessage(Config messages, String key, MessageChannel defaultChannel,
                                  String fallback, Map<String, MessageChannel> channels) {
        String chatPath = this.messagePath(MessageChannel.CHAT, key);
        String toastPath = this.messagePath(MessageChannel.TOAST, key);
        if (messages.exists(chatPath)) {
            channels.put(key, MessageChannel.CHAT);
            return messages.getString(chatPath, fallback);
        }
        if (messages.exists(toastPath)) {
            channels.put(key, MessageChannel.TOAST);
            return messages.getString(toastPath, fallback);
        }
        channels.put(key, defaultChannel);
        return messages.getString(this.messagePath(defaultChannel, key), fallback);
    }

    private String messagePath(MessageChannel channel, String key) {
        return (channel == MessageChannel.TOAST ? "toast-message." : "chat-message.") + key;
    }

    private String message(Config messages, String path, String fallback) {
        return messages.getString(path, fallback);
    }

    private MenuText menu(Config config) {
        return new MenuText(
                this.menuText(config, "regions-title", "Ваши регионы"),
                this.menuText(config, "regions-content", "Выберите свой блок региона"),
                this.menuText(config, "region-title-format", "{x} {y} {z}"),
                this.menuText(config, "region-button-format", "{name}\n{x} {y} {z}"),
                this.menuText(config, "region-button-hidden-format", "{name}\n{hidden}"),
                this.menuText(config, "add-member-button", "Добавь игрока\nв регион"),
                this.menuText(config, "remove-member-button", "Удалить игрока\nиз региона"),
                this.menuText(config, "settings-button", "Настройки региона"),
                this.menuText(config, "add-member-title", "Добавление игрока в регион"),
                this.menuText(config, "add-member-label", "Укажите ник игрока которого хотите добавить в свой регион"),
                this.menuText(config, "add-member-input", "Ник игрока"),
                this.menuText(config, "apply-button", "Применить"),
                this.menuText(config, "remove-member-title", "Удаление игрока из региона"),
                this.menuText(config, "remove-member-label", "Выберите игрока которого хотите удалить из региона"),
                this.menuText(config, "remove-member-dropdown", "Выбрать игрока"),
                this.menuText(config, "settings-title", "Настройка региона"),
                this.menuText(config, "settings-label", "Тут можно немного настроить ваш регион"),
                this.menuText(config, "allow-members-break-region-block", "Разрешить ломать регион добавленным игрокам"),
                this.menuText(config, "transfer-owner-dropdown", "Изменить владельца региона"),
                this.menuText(config, "transfer-owner-no-change", "Не менять"),
                this.menuText(config, "hide-coordinates", "Скрыть координаты привата из меню регионов"),
                this.menuText(config, "delete-region", "Удалить регион")
        );
    }

    private String menuText(Config config, String key, String fallback) {
        return config.getString(key, fallback);
    }

    private List<RegionDefinition> regionDefinitions(Config config) {
        List<RegionDefinition> definitions = new ArrayList<>();
        for (ConfigSection entry : this.regionEntries(config)) {
            this.loadDefinition(entry).ifPresent(definitions::add);
        }
        return definitions;
    }

    private List<ConfigSection> regionEntries(Config config) {
        Object raw = config.get("block-region-register");
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

    private java.util.Optional<ConfigSection> asSection(Object value, String key) {
        if (value instanceof ConfigSection section) {
            section.putIfAbsent("name", key);
            return java.util.Optional.of(section);
        }
        if (value instanceof Map<?, ?> map) {
            return java.util.Optional.of(new ConfigSection((Map<String, Object>) map));
        }
        return java.util.Optional.empty();
    }

    private java.util.Optional<RegionDefinition> loadDefinition(ConfigSection entry) {
        String fileName = entry.getString("file", "");
        if (fileName.isBlank()) {
            return java.util.Optional.empty();
        }
        Config file = new Config(this.definitionFile(fileName));
        if (!file.getBoolean("enabled", true)) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(this.definitionFrom(entry, fileName, file));
    }

    private File definitionFile(String fileName) {
        return new File(new File(this.plugin.getDataFolder(), "regions"), fileName);
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

