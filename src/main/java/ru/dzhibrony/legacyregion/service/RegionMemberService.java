package ru.dzhibrony.legacyregion.service;

import ru.dzhibrony.legacyregion.config.TransferSettings;
import ru.dzhibrony.legacyregion.model.Region;
import ru.dzhibrony.legacyregion.model.RegionMember;

public final class RegionMemberService {

    private final RegionService regionService;
    private final PlayerResolver playerResolver;
    private TransferSettings transferSettings;

    public RegionMemberService(RegionService regionService, PlayerResolver playerResolver, TransferSettings transferSettings) {
        this.regionService = regionService;
        this.playerResolver = playerResolver;
        this.transferSettings = transferSettings;
    }

    public AddMemberStatus addMember(Region region, String playerName) {
        if (!this.playerResolver.hasJoined(playerName)) {
            return AddMemberStatus.PLAYER_NEVER_JOINED;
        }
        RegionMember member = this.playerResolver.member(playerName);
        return this.addKnownMember(region, member);
    }

    public RemoveMemberStatus removeMember(Region region, String playerKey) {
        if (!region.isMember(playerKey)) {
            return RemoveMemberStatus.NOT_MEMBER;
        }
        this.regionService.removeMember(region, playerKey);
        return RemoveMemberStatus.REMOVED;
    }

    public void transferOwner(Region region, RegionMember newOwner) {
        RegionMember oldOwner = new RegionMember(region.ownerName(), region.ownerKey());
        this.regionService.removeMember(region, newOwner.key());
        region.transferOwner(newOwner, false);
        this.keepOldOwnerIfNeeded(region, oldOwner);
        this.regionService.save(region);
    }

    public void updateTransferSettings(TransferSettings transferSettings) {
        this.transferSettings = transferSettings;
    }

    private void keepOldOwnerIfNeeded(Region region, RegionMember oldOwner) {
        if (this.transferSettings.addOldOwnerAsMember()) {
            this.regionService.addMember(region, oldOwner);
        }
    }

    private AddMemberStatus addKnownMember(Region region, RegionMember member) {
        if (region.isOwner(member.key())) {
            return AddMemberStatus.OWNER;
        }
        if (region.isMember(member.key())) {
            return AddMemberStatus.ALREADY_MEMBER;
        }
        this.regionService.addMember(region, member);
        return AddMemberStatus.ADDED;
    }
}
