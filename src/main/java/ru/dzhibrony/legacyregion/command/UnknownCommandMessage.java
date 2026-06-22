package ru.dzhibrony.legacyregion.command;

import cn.nukkit.command.CommandSender;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.utils.TextFormat;

final class UnknownCommandMessage {

    private UnknownCommandMessage() {
    }

    static void send(CommandSender sender, String commandLabel) {
        sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.unknown", commandLabel));
    }
}
