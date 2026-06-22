package ru.dzhibrony.legacyregion.command;

import cn.nukkit.Player;
import ru.dzhibrony.legacyregion.config.Messages;
import ru.dzhibrony.legacyregion.model.Region;
import ru.dzhibrony.legacyregion.model.RegionMember;
import ru.dzhibrony.legacyregion.service.MessageService;
import ru.dzhibrony.legacyregion.service.RegionMemberService;
import ru.dzhibrony.legacyregion.service.RemoveMemberStatus;

final class RemoveMemberHandler {

    private final RegionCommandTarget target;
    private final RegionMemberService memberService;
    private final MessageService messageService;

    RemoveMemberHandler(RegionCommandTarget target, RegionMemberService memberService, MessageService messageService) {
        this.target = target;
        this.memberService = memberService;
        this.messageService = messageService;
    }

    public void execute(Player player, String[] args) {
        if (args.length < 2) {
            this.messageService.send(player, Messages.USAGE_REMOVE_MEMBER, this.messageService.messages().usageRemoveMember());
            return;
        }
        this.target.resolve(player).ifPresent(region -> this.removeMember(player, region, args[1]));
    }

    private void removeMember(Player player, Region region, String targetName) {
        RemoveMemberStatus status = this.memberService.removeMember(region, RegionMember.normalize(targetName));
        if (status == RemoveMemberStatus.REMOVED) {
            this.messageService.send(player, Messages.MEMBER_REMOVED, this.messageService.messages().memberRemovedToast());
            player.sendCommandData();
            return;
        }
        this.messageService.send(player, Messages.MEMBER_NOT_FOUND, this.messageService.messages().memberNotFound());
    }
}
