package ru.dzhibrony.legacyregion.command;

import cn.nukkit.Player;
import ru.dzhibrony.legacyregion.config.Messages;
import ru.dzhibrony.legacyregion.service.MessageService;

final class ReloadHandler {

    private final MessageService messageService;
    private final Runnable reloadAction;

    ReloadHandler(MessageService messageService, Runnable reloadAction) {
        this.messageService = messageService;
        this.reloadAction = reloadAction;
    }

    public void execute(Player player, String permission) {
        if (!player.hasPermission(permission)) {
            UnknownCommandMessage.send(player, "region");
            return;
        }
        this.reloadAction.run();
        this.messageService.send(player, Messages.RELOAD_SUCCESS, this.messageService.messages().reloadSuccess());
    }
}
