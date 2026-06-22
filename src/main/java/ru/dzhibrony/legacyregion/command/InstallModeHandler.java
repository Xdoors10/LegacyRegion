package ru.dzhibrony.legacyregion.command;

import cn.nukkit.Player;
import ru.dzhibrony.legacyregion.config.Messages;
import ru.dzhibrony.legacyregion.service.InstallModeService;
import ru.dzhibrony.legacyregion.service.MessageService;

final class InstallModeHandler {

    private final InstallModeService installModeService;
    private final MessageService messageService;

    InstallModeHandler(InstallModeService installModeService, MessageService messageService) {
        this.installModeService = installModeService;
        this.messageService = messageService;
    }

    public void enter(Player player) {
        this.installModeService.enter(player);
        this.messageService.send(player, Messages.INSTALL_MODE_ENTER, this.messageService.messages().installModeEnter());
    }

    public void leave(Player player) {
        if (this.installModeService.leave(player)) {
            this.messageService.send(player, Messages.INSTALL_MODE_EXIT, this.messageService.messages().installModeExit());
            return;
        }
        this.messageService.send(player, Messages.INSTALL_MODE_NOT_ACTIVE, this.messageService.messages().installModeNotActive());
    }
}
