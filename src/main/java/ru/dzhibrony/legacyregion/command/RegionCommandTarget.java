package ru.dzhibrony.legacyregion.command;

import cn.nukkit.Player;
import ru.dzhibrony.legacyregion.config.Messages;
import ru.dzhibrony.legacyregion.model.Region;
import ru.dzhibrony.legacyregion.service.MessageService;
import ru.dzhibrony.legacyregion.service.RegionService;

import java.util.Optional;

final class RegionCommandTarget {

    private final RegionService regionService;
    private final MessageService messageService;

    RegionCommandTarget(RegionService regionService, MessageService messageService) {
        this.regionService = regionService;
        this.messageService = messageService;
    }

    Optional<Region> resolve(Player player) {
        Optional<Region> region = this.regionService.commandRegion(player);
        if (region.isEmpty()) {
            this.messageService.send(player, Messages.NO_REGION_FOR_COMMAND, this.messageService.messages().noRegionForCommand());
        }
        return region;
    }
}
