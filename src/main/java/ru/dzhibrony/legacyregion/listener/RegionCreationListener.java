package ru.dzhibrony.legacyregion.listener;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockPlaceEvent;
import ru.dzhibrony.legacyregion.config.Messages;
import ru.dzhibrony.legacyregion.service.CreateRegionResult;
import ru.dzhibrony.legacyregion.service.CreateRegionStatus;
import ru.dzhibrony.legacyregion.service.InstallModeService;
import ru.dzhibrony.legacyregion.service.MessageService;
import ru.dzhibrony.legacyregion.service.RegionService;
import ru.dzhibrony.legacyregion.visual.RegionParticleVisualizer;

public final class RegionCreationListener implements Listener {

    private final InstallModeService installModeService;
    private final RegionService regionService;
    private final MessageService messageService;
    private final RegionParticleVisualizer particleVisualizer;

    public RegionCreationListener(InstallModeService installModeService, RegionService regionService,
                                  MessageService messageService, RegionParticleVisualizer particleVisualizer) {
        this.installModeService = installModeService;
        this.regionService = regionService;
        this.messageService = messageService;
        this.particleVisualizer = particleVisualizer;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!this.installModeService.active(event.getPlayer())) {
            return;
        }
        CreateRegionResult result = this.regionService.create(event.getPlayer(), event.getBlock());
        this.handleResult(event, result);
    }

    private void handleResult(BlockPlaceEvent event, CreateRegionResult result) {
        if (result.status() == CreateRegionStatus.CREATED) {
            this.finishCreation(event, result);
            return;
        }
        event.setCancelled();
        this.sendFailure(event, result.status());
    }

    private void finishCreation(BlockPlaceEvent event, CreateRegionResult result) {
        this.installModeService.leave(event.getPlayer());
        this.messageService.send(event.getPlayer(), Messages.REGION_CREATED, this.messageService.messages().regionCreated());
        this.particleVisualizer.show(result.region());
    }

    private void sendFailure(BlockPlaceEvent event, CreateRegionStatus status) {
        if (status == CreateRegionStatus.OVERLAP) {
            this.messageService.send(event.getPlayer(), Messages.REGION_OVERLAP, this.messageService.messages().regionOverlap());
            return;
        }
        this.messageService.send(event.getPlayer(), Messages.NOT_REGION_BLOCK, this.messageService.messages().notRegionBlock());
    }
}
