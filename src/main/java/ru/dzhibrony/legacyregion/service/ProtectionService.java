package ru.dzhibrony.legacyregion.service;

import cn.nukkit.Player;
import ru.dzhibrony.legacyregion.config.ProtectionSettings;
import ru.dzhibrony.legacyregion.model.Region;
import ru.dzhibrony.legacyregion.model.RegionLocation;
import ru.dzhibrony.legacyregion.model.RegionMember;

import java.util.Optional;

public final class ProtectionService {

    private final RegionService regionService;
    private ProtectionSettings settings;

    public ProtectionService(RegionService regionService, ProtectionSettings settings) {
        this.regionService = regionService;
        this.settings = settings;
    }

    public Optional<Region> deniedRegion(Player player, RegionLocation location, RegionAction action) {
        if (this.bypasses(player)) {
            return Optional.empty();
        }
        return this.regionService.regionsAt(location).stream()
                .filter(region -> !this.allowed(player, region, location, action))
                .findFirst();
    }

    public boolean allowed(Player player, Region region, RegionLocation location, RegionAction action) {
        String playerKey = RegionMember.normalize(player.getName());
        if (region.isOwner(playerKey)) {
            return true;
        }
        return this.allowedMember(region, location, action, playerKey);
    }

    public void updateSettings(ProtectionSettings settings) {
        this.settings = settings;
    }

    private boolean allowedMember(Region region, RegionLocation location, RegionAction action, String playerKey) {
        if (!region.isMember(playerKey)) {
            return false;
        }
        return action != RegionAction.BREAK_REGION_BLOCK || this.canBreakRegionBlock(region, location);
    }

    private boolean canBreakRegionBlock(Region region, RegionLocation location) {
        return region.membersCanBreakRegionBlock() || !region.location().equals(location);
    }

    private boolean bypasses(Player player) {
        return this.settings.opBypass() && player.isOp();
    }
}
