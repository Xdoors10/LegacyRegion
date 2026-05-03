package ru.dzhibrony.legacyregion.service;

import cn.nukkit.Player;
import cn.nukkit.Server;
import ru.dzhibrony.legacyregion.model.RegionMember;

public final class PlayerResolver {

    private final Server server;

    public PlayerResolver(Server server) {
        this.server = server;
    }

    public boolean hasJoined(String playerName) {
        return this.online(playerName) != null
                || this.server.lookupName(playerName).isPresent()
                || this.server.getOfflinePlayerData(playerName) != null;
    }

    public RegionMember member(String playerName) {
        Player online = this.online(playerName);
        if (online != null) {
            return RegionMember.of(online.getName());
        }
        return RegionMember.of(playerName);
    }

    private Player online(String playerName) {
        return this.server.getPlayerExact(playerName);
    }
}
