package ru.dzhibrony.legacyregion.listener;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockExplodeEvent;
import cn.nukkit.event.block.BlockIgniteEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.block.DoorToggleEvent;
import cn.nukkit.event.block.ItemFrameUseEvent;
import cn.nukkit.event.block.SignChangeEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityExplodeEvent;
import cn.nukkit.event.entity.EntityInteractEvent;
import cn.nukkit.event.player.PlayerBucketEmptyEvent;
import cn.nukkit.event.player.PlayerBucketFillEvent;
import cn.nukkit.event.player.PlayerDropItemEvent;
import cn.nukkit.event.player.PlayerInteractEntityEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import ru.dzhibrony.legacyregion.config.Messages;
import ru.dzhibrony.legacyregion.config.ProtectionSettings;
import ru.dzhibrony.legacyregion.model.Region;
import ru.dzhibrony.legacyregion.model.RegionLocation;
import ru.dzhibrony.legacyregion.model.RegionMember;
import ru.dzhibrony.legacyregion.service.MessageService;
import ru.dzhibrony.legacyregion.service.ProtectionService;
import ru.dzhibrony.legacyregion.service.RegionAction;
import ru.dzhibrony.legacyregion.service.RegionService;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class RegionProtectionListener implements Listener {

    private final RegionService regionService;
    private final ProtectionService protectionService;
    private ProtectionSettings settings;
    private final MessageService messageService;

    public RegionProtectionListener(RegionService regionService, ProtectionService protectionService,
                                    ProtectionSettings settings, MessageService messageService) {
        this.regionService = regionService;
        this.protectionService = protectionService;
        this.settings = settings;
        this.messageService = messageService;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Optional<Region> region = this.anchorRegion(event.getBlock());
        if (region.isPresent()) {
            this.handleAnchorBreak(event, region.get());
            return;
        }
        this.cancelIfDenied(event, event.getPlayer(), event.getBlock(), event.getFace(), RegionAction.EDIT);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        this.cancelIfDenied(event, event.getPlayer(), event.getBlock(), RegionAction.EDIT, this.placeParticlePosition(event));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (this.blockAction(event.getAction())) {
            this.cancelIfDenied(event, event.getPlayer(), event.getBlock(), event.getFace(), RegionAction.EDIT);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDoorToggle(DoorToggleEvent event) {
        if (event.getPlayer() != null) {
            this.cancelIfDenied(event, event.getPlayer(), event.getBlock(), RegionAction.EDIT);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        this.cancelIfDenied(event, event.getPlayer(), event.getBlockClicked().getSide(event.getBlockFace()),
                RegionAction.EDIT, this.surfacePosition(event.getBlockClicked(), event.getBlockFace(), event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        this.cancelIfDenied(event, event.getPlayer(), event.getBlockClicked(), event.getBlockFace(), RegionAction.EDIT);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        this.cancelIfDenied(event, event.getPlayer(), event.getBlock(), RegionAction.EDIT);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemFrameUse(ItemFrameUseEvent event) {
        if (event.getPlayer() != null) {
            this.cancelIfDenied(event, event.getPlayer(), event.getBlock(), RegionAction.EDIT);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (this.settings.denyDropItem()) {
            this.cancelIfDenied(event, event.getPlayer(), RegionLocation.from(event.getPlayer()), RegionAction.EDIT);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        this.cancelIfDenied(event, event.getPlayer(), RegionLocation.from(event.getEntity()), RegionAction.EDIT);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (this.settings.denyEntityDamage() && event.getDamager() instanceof Player player) {
            this.cancelIfDenied(event, player, RegionLocation.from(event.getEntity()), RegionAction.EDIT);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityInteract(EntityInteractEvent event) {
        if (event.getEntity() instanceof Player player) {
            this.cancelIfDenied(event, player, event.getBlock(), RegionAction.EDIT);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        this.protectRegionAnchorsFromExplosion(event.getBlockList());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        this.protectRegionAnchorsFromExplosion(event.getAffectedBlocks());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (this.settings.protectFireSpread() && this.shouldCancelIgnite(event)) {
            event.setCancelled();
        }
    }

    public void updateSettings(ProtectionSettings settings) {
        this.settings = settings;
    }

    private void cancelIfDenied(Cancellable event, Player player, Block block, RegionAction action) {
        this.cancelIfDenied(event, player, block, null, action);
    }

    private void cancelIfDenied(Cancellable event, Player player, Block block, BlockFace face, RegionAction action) {
        this.cancelIfDenied(event, player, block, action, this.surfacePosition(block, face, player));
    }

    private void cancelIfDenied(Cancellable event, Player player, Block block, RegionAction action, Vector3 particlePosition) {
        this.cancelIfDenied(event, player, RegionLocation.from(block), action, particlePosition);
    }

    private void handleAnchorBreak(BlockBreakEvent event, Region region) {
        RegionLocation location = RegionLocation.from(event.getBlock());
        if (this.protectionService.deniedRegion(event.getPlayer(), location, RegionAction.BREAK_REGION_BLOCK).isPresent()) {
            if (this.memberCannotBreakAnchor(event.getPlayer(), region, location)) {
                this.cancelMemberAnchorBreak(event, event.getPlayer());
                return;
            }
            this.cancel(event, event.getPlayer(), location, this.surfacePosition(event.getBlock(), event.getFace(), event.getPlayer()));
            return;
        }
        // The saved region follows the real anchor block: if it is broken, the private area is removed too.
        this.regionService.delete(region);
        this.messageService.send(event.getPlayer(), Messages.REGION_DELETED, this.messageService.messages().regionDeleted());
    }

    private boolean memberCannotBreakAnchor(Player player, Region region, RegionLocation location) {
        String playerKey = RegionMember.normalize(player.getName());
        return region.location().equals(location)
                && region.isMember(playerKey)
                && !region.membersCanBreakRegionBlock();
    }

    private void cancelMemberAnchorBreak(Cancellable event, Player player) {
        event.setCancelled();
        this.messageService.sendRaw(player, Messages.MEMBER_CANNOT_BREAK_REGION_BLOCK, this.messageService.messages().memberCannotBreakRegionBlock());
    }

    private void cancelIfDenied(Cancellable event, Player player, RegionLocation location, RegionAction action) {
        if (this.protectionService.deniedRegion(player, location, action).isPresent()) {
            this.cancel(event, player, location, null);
        }
    }

    private void cancelIfDenied(Cancellable event, Player player, RegionLocation location,
                                RegionAction action, Vector3 particlePosition) {
        if (this.protectionService.deniedRegion(player, location, action).isPresent()) {
            this.cancel(event, player, location, particlePosition);
        }
    }

    private void cancel(Cancellable event, Player player, RegionLocation location, Vector3 particlePosition) {
        event.setCancelled();
        this.showDeniedFeedback(player, location, particlePosition);
    }

    private void showDeniedFeedback(Player player, RegionLocation location, Vector3 particlePosition) {
        if (this.settings.deniedMessageEnabled()) {
            this.messageService.sendActionDenied(player);
        }
        if (this.settings.deniedParticleEnabled()) {
            this.spawnDeniedParticle(player, location, particlePosition);
        }
    }

    private void spawnDeniedParticle(Player player, RegionLocation location, Vector3 particlePosition) {
        Level level = player.getServer().getLevelByName(location.levelName());
        if (level == null) {
            return;
        }
        Vector3 position = particlePosition == null ? this.centerPosition(location) : particlePosition;
        level.addParticleEffect(position.asVector3f(), this.settings.deniedParticle(), -1, level.getDimension());
    }

    private Vector3 placeParticlePosition(BlockPlaceEvent event) {
        if (event.getBlockAgainst() == null) {
            return this.surfacePosition(event.getBlock(), null, event.getPlayer());
        }
        BlockFace face = this.faceBetween(event.getBlockAgainst(), event.getBlock());
        return this.surfacePosition(event.getBlockAgainst(), face, event.getPlayer());
    }

    private Vector3 surfacePosition(Block block, BlockFace face, Player player) {
        BlockFace actualFace = face == null ? this.estimateFace(player, block) : face;
        Vector3 center = new Vector3(block.x + 0.5, block.y + 0.5, block.z + 0.5);
        Vector3 offset = actualFace.getUnitVector().multiply(0.54);
        return center.add(offset);
    }

    private Vector3 centerPosition(RegionLocation location) {
        return new Vector3(location.x() + 0.5, location.y() + 0.5, location.z() + 0.5);
    }

    private BlockFace faceBetween(Block from, Block to) {
        int dx = to.getFloorX() - from.getFloorX();
        int dy = to.getFloorY() - from.getFloorY();
        int dz = to.getFloorZ() - from.getFloorZ();
        return this.faceFromDelta(dx, dy, dz);
    }

    private BlockFace faceFromDelta(int dx, int dy, int dz) {
        int absX = Math.abs(dx);
        int absY = Math.abs(dy);
        int absZ = Math.abs(dz);
        if (absY >= absX && absY >= absZ) {
            return dy >= 0 ? BlockFace.UP : BlockFace.DOWN;
        }
        if (absX >= absZ) {
            return dx >= 0 ? BlockFace.EAST : BlockFace.WEST;
        }
        return dz >= 0 ? BlockFace.SOUTH : BlockFace.NORTH;
    }

    private BlockFace estimateFace(Player player, Block block) {
        Vector3 center = new Vector3(block.x + 0.5, block.y + 0.5, block.z + 0.5);
        Vector3 delta = player.getEyePosition().subtract(center);
        double absX = Math.abs(delta.x);
        double absY = Math.abs(delta.y);
        double absZ = Math.abs(delta.z);
        if (absY >= absX && absY >= absZ) {
            return delta.y >= 0 ? BlockFace.UP : BlockFace.DOWN;
        }
        if (absX >= absZ) {
            return delta.x >= 0 ? BlockFace.EAST : BlockFace.WEST;
        }
        return delta.z >= 0 ? BlockFace.SOUTH : BlockFace.NORTH;
    }

    private Optional<Region> anchorRegion(Block block) {
        RegionLocation location = RegionLocation.from(block);
        return this.regionService.regionsAt(location).stream()
                .filter(region -> region.location().equals(location))
                .findFirst();
    }

    private boolean blockAction(PlayerInteractEvent.Action action) {
        return action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK
                || action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK
                || action == PlayerInteractEvent.Action.PHYSICAL;
    }

    private void protectRegionAnchorsFromExplosion(Collection<Block> blocks) {
        if (blocks == null) {
            return;
        }
        Map<String, Region> breakableAnchors = new LinkedHashMap<>();
        blocks.removeIf(block -> this.shouldProtectAnchorFromExplosion(block, breakableAnchors));
        breakableAnchors.values().forEach(this.regionService::delete);
    }

    private boolean shouldProtectAnchorFromExplosion(Block block, Map<String, Region> breakableAnchors) {
        Optional<Region> anchor = this.anchorRegion(block);
        if (anchor.isEmpty()) {
            return false;
        }
        if (this.regionService.breakRegionBlockFromExplosions(anchor.get())) {
            breakableAnchors.put(anchor.get().id(), anchor.get());
            return false;
        }
        return true;
    }

    private boolean shouldCancelIgnite(BlockIgniteEvent event) {
        if (event.getEntity() instanceof Player player) {
            return this.protectionService.deniedRegion(player, RegionLocation.from(event.getBlock()), RegionAction.EDIT).isPresent();
        }
        return this.igniteFromOutside(event);
    }

    private boolean igniteFromOutside(BlockIgniteEvent event) {
        RegionLocation block = RegionLocation.from(event.getBlock());
        if (event.getSource() == null) {
            return !this.regionService.regionsAt(block).isEmpty();
        }
        RegionLocation source = RegionLocation.from(event.getSource());
        return this.regionService.regionsAt(block).stream().anyMatch(region -> !region.contains(source));
    }
}
