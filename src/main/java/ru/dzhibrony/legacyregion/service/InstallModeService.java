package ru.dzhibrony.legacyregion.service;

import cn.nukkit.Player;
import ru.dzhibrony.legacyregion.model.RegionMember;

import java.util.HashSet;
import java.util.Set;

public final class InstallModeService {

    private final Set<String> players = new HashSet<>();

    public void enter(Player player) {
        this.players.add(this.key(player));
    }

    public boolean leave(Player player) {
        return this.players.remove(this.key(player));
    }

    public boolean active(Player player) {
        return this.players.contains(this.key(player));
    }

    private String key(Player player) {
        return RegionMember.normalize(player.getName());
    }
}
