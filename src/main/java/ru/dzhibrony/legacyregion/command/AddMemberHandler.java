package ru.dzhibrony.legacyregion.command;

import cn.nukkit.Player;
import ru.dzhibrony.legacyregion.config.Messages;
import ru.dzhibrony.legacyregion.model.Region;
import ru.dzhibrony.legacyregion.service.AddMemberStatus;
import ru.dzhibrony.legacyregion.service.MessageService;
import ru.dzhibrony.legacyregion.service.RegionMemberService;

final class AddMemberHandler {

    private final RegionCommandTarget target;
    private final RegionMemberService memberService;
    private final MessageService messageService;

    AddMemberHandler(RegionCommandTarget target, RegionMemberService memberService, MessageService messageService) {
        this.target = target;
        this.memberService = memberService;
        this.messageService = messageService;
    }

    public void execute(Player player, String[] args) {
        if (args.length < 2) {
            this.messageService.send(player, Messages.USAGE_ADD_MEMBER, this.messageService.messages().usageAddMember());
            return;
        }
        this.target.resolve(player).ifPresent(region -> this.addMember(player, region, args[1]));
    }

    private void addMember(Player player, Region region, String targetName) {
        AddMemberStatus status = this.memberService.addMember(region, targetName);
        switch (status) {
            case ADDED -> {
                this.messageService.send(player, Messages.MEMBER_ADDED, this.messageService.messages().memberAddedToast());
                player.sendCommandData();
            }
            case PLAYER_NEVER_JOINED -> this.messageService.send(player, Messages.PLAYER_NEVER_JOINED, this.messageService.messages().playerNeverJoinedToast());
            case ALREADY_MEMBER -> this.messageService.send(player, Messages.MEMBER_ALREADY_ADDED, this.messageService.messages().memberAlreadyAdded());
            case OWNER -> this.messageService.send(player, Messages.CANNOT_ADD_YOURSELF, this.messageService.messages().cannotAddYourself());
        }
    }
}
