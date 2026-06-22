package ru.dzhibrony.legacyregion.config;

import cn.nukkit.utils.Config;

import java.util.LinkedHashMap;
import java.util.Map;

final class MessagesLoader {

    private final Config config;

    MessagesLoader(Config config) {
        this.config = config;
    }

    Messages load() {
        Map<String, MessageChannel> channels = new LinkedHashMap<>();
        return new Messages(
                this.message("chat-message.prefix", "&f[Регионы]&r "),
                this.message("toast-message.title", "&fРегионы"),
                this.displayMessage(Messages.PLAYER_ONLY, MessageChannel.CHAT, "&cЭта команда доступна только игроку.", channels),
                this.displayMessage(Messages.USAGE_ADD_MEMBER, MessageChannel.CHAT, "&cИспользуйте: /rg addmember <ник>", channels),
                this.displayMessage(Messages.USAGE_REMOVE_MEMBER, MessageChannel.CHAT, "&cИспользуйте: /rg removemember <ник>", channels),
                this.displayMessage(Messages.INSTALL_MODE_ENTER, MessageChannel.CHAT, "&fВы вошли в режим установки, поставьте блок региона.", channels),
                this.displayMessage(Messages.INSTALL_MODE_EXIT, MessageChannel.CHAT, "&fВы вышли из режима установки региона.", channels),
                this.displayMessage(Messages.INSTALL_MODE_NOT_ACTIVE, MessageChannel.CHAT, "&cВы не находитесь в режиме установки региона.", channels),
                this.displayMessage(Messages.NOT_REGION_BLOCK, MessageChannel.CHAT, "&cЭтот блок не зарегистрирован как блок региона.", channels),
                this.displayMessage(Messages.REGION_CREATED, MessageChannel.CHAT, "&fРегион успешно создан", channels),
                this.displayMessage(Messages.REGION_OVERLAP, MessageChannel.CHAT, "&cЭтот регион пересекается с уже созданным регионом.", channels),
                this.displayMessage(Messages.ACTION_DENIED, MessageChannel.CHAT, "&cВы находитесь в чужом регионе", channels),
                this.message("chat-message.action-denied-display", "actionbar"),
                this.displayMessage(Messages.NO_OWNED_REGIONS, MessageChannel.CHAT, "&cУ вас нет регионов.", channels),
                this.displayMessage(Messages.NO_REGION_FOR_COMMAND, MessageChannel.CHAT, "&cВстаньте внутри своего региона или оставьте только один регион для команды без меню.", channels),
                this.displayMessage(Messages.MEMBER_ADDED, MessageChannel.TOAST, "Игрок успешно добавлен в регион", channels),
                this.displayMessage(Messages.MEMBER_REMOVED, MessageChannel.TOAST, "Игрок успешно удален из региона", channels),
                this.displayMessage(Messages.PLAYER_NEVER_JOINED, MessageChannel.TOAST, "Такой игрок никогда не заходил на сервер", channels),
                this.displayMessage(Messages.MEMBER_ALREADY_ADDED, MessageChannel.CHAT, "&cЭтот игрок уже добавлен в регион.", channels),
                this.displayMessage(Messages.MEMBER_NOT_FOUND, MessageChannel.CHAT, "&cЭтот игрок не добавлен в регион.", channels),
                this.displayMessage(Messages.OWNER_CHANGED, MessageChannel.CHAT, "&fВладелец региона изменен.", channels),
                this.displayMessage(Messages.REGION_UPDATED, MessageChannel.CHAT, "&fНастройки региона сохранены.", channels),
                this.displayMessage(Messages.REGION_DELETED, MessageChannel.CHAT, "&fРегион успешно удален.", channels),
                this.displayMessage(Messages.CANNOT_ADD_YOURSELF, MessageChannel.CHAT, "&cВы уже владелец этого региона.", channels),
                this.displayMessage(Messages.EMPTY_MEMBERS, MessageChannel.TOAST, "&cВ регионе нет добавленных игроков.", channels),
                this.message("chat-message.deleted-region-menu-name", "Удаленный регион"),
                this.message("chat-message.hidden-coordinates", "Координаты скрыты"),
                this.displayMessage(Messages.RELOAD_SUCCESS, MessageChannel.CHAT, "&fКонфигурация и регионы перезагружены.", channels),
                this.displayMessage(Messages.MEMBER_CANNOT_BREAK_REGION_BLOCK, MessageChannel.CHAT, "§6Владелец §cне разрешил §fвам §cломать §6регион§f.", channels),
                channels
        );
    }

    private String displayMessage(String key, MessageChannel defaultChannel, String fallback,
                                  Map<String, MessageChannel> channels) {
        String chatPath = this.messagePath(MessageChannel.CHAT, key);
        String toastPath = this.messagePath(MessageChannel.TOAST, key);
        if (this.config.exists(chatPath)) {
            channels.put(key, MessageChannel.CHAT);
            return this.config.getString(chatPath, fallback);
        }
        if (this.config.exists(toastPath)) {
            channels.put(key, MessageChannel.TOAST);
            return this.config.getString(toastPath, fallback);
        }
        channels.put(key, defaultChannel);
        return this.config.getString(this.messagePath(defaultChannel, key), fallback);
    }

    private String messagePath(MessageChannel channel, String key) {
        return (channel == MessageChannel.TOAST ? "toast-message." : "chat-message.") + key;
    }

    private String message(String path, String fallback) {
        return this.config.getString(path, fallback);
    }
}
