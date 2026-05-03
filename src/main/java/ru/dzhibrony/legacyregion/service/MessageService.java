package ru.dzhibrony.legacyregion.service;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import ru.dzhibrony.legacyregion.config.MessageChannel;
import ru.dzhibrony.legacyregion.config.Messages;

import java.util.Locale;

public final class MessageService {

    private Messages messages;

    public MessageService(Messages messages) {
        this.messages = messages;
    }

    public void send(CommandSender sender, String message) {
        this.sendChat(sender, message);
    }

    public void send(CommandSender sender, String key, String message) {
        if (sender instanceof Player player) {
            this.send(player, key, message);
            return;
        }
        this.sendChat(sender, message);
    }

    public void send(Player player, String key, String message) {
        if (this.messages.channel(key) == MessageChannel.TOAST) {
            this.sendToast(player, message);
            return;
        }
        this.sendChat(player, message);
    }

    public void sendRaw(CommandSender sender, String message) {
        sender.sendMessage(this.color(message));
    }

    public void sendRaw(Player player, String key, String message) {
        if (this.messages.channel(key) == MessageChannel.TOAST) {
            this.sendToast(player, message);
            return;
        }
        this.sendRaw((CommandSender) player, message);
    }

    public void sendActionDenied(Player player) {
        String message = this.color(this.messages.actionDenied());
        switch (this.messages.actionDeniedDisplay().toLowerCase(Locale.ROOT)) {
            case "actionbar", "auctionbar" -> player.sendActionBar(message);
            case "popup" -> player.sendPopup(message);
            case "tip" -> player.sendTip(message);
            case "toast" -> player.sendToast(this.color(this.messages.toastTitle()), message);
            case "chat", "message" -> this.sendChat(player, this.messages.actionDenied());
            default -> this.send(player, this.messages.actionDenied());
        }
    }

    public String color(String message) {
        return TextFormat.colorize(message);
    }

    public Messages messages() {
        return this.messages;
    }

    public void updateMessages(Messages messages) {
        this.messages = messages;
    }

    private void sendChat(CommandSender sender, String message) {
        sender.sendMessage(this.color(this.messages.prefix() + message));
    }

    private void sendToast(Player player, String message) {
        player.sendToast(this.color(this.messages.toastTitle()), this.color(message));
    }
}
