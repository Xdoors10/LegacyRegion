package ru.dzhibrony.legacyregion.listener;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.level.Location;
import cn.nukkit.plugin.PluginBase;
import ru.dzhibrony.legacyregion.model.Region;
import ru.dzhibrony.legacyregion.service.RegionService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class RegionCommandSuggestionListener implements Listener {

    private final PluginBase plugin;
    private final RegionService regionService;
    private final Map<UUID, String> lastCommandRegions = new HashMap<>();

    public RegionCommandSuggestionListener(PluginBase plugin, RegionService regionService) {
        this.plugin = plugin;
        this.regionService = regionService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        this.refreshAllSoon();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        this.lastCommandRegions.remove(event.getPlayer().getUniqueId());
        this.refreshAllSoon();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (this.sameBlock(event.getFrom(), event.getTo())) {
            return;
        }
        this.refreshWhenTargetRegionChanged(event.getPlayer());
    }

    private void refreshAllSoon() {
        // Let Lumi finish its own join/quit bookkeeping before rebuilding command suggestions.
        this.plugin.getServer().getScheduler().scheduleDelayedTask(this.plugin, this::refreshAll, 20);
    }

    private void refreshAll() {
        this.plugin.getServer().getOnlinePlayers().values().forEach(this::refreshAndRemember);
    }

    private void refreshWhenTargetRegionChanged(Player player) {
        String regionId = this.targetRegionId(player);
        UUID uuid = player.getUniqueId();
        if (regionId.equals(this.lastCommandRegions.get(uuid))) {
            return;
        }
        this.lastCommandRegions.put(uuid, regionId);
        player.sendCommandData();
    }

    private void refreshAndRemember(Player player) {
        this.lastCommandRegions.put(player.getUniqueId(), this.targetRegionId(player));
        player.sendCommandData();
    }

    private String targetRegionId(Player player) {
        return this.regionService.commandRegion(player)
                .map(Region::id)
                .orElse("");
    }

    private boolean sameBlock(Location from, Location to) {
        return from.getFloorX() == to.getFloorX()
                && from.getFloorY() == to.getFloorY()
                && from.getFloorZ() == to.getFloorZ()
                && from.getLevelName().equals(to.getLevelName());
    }
}
