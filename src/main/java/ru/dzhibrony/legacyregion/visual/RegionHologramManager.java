package ru.dzhibrony.legacyregion.visual;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.particle.FloatingTextParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.TextFormat;
import ru.dzhibrony.legacyregion.config.HologramSettings;
import ru.dzhibrony.legacyregion.model.Region;
import ru.dzhibrony.legacyregion.service.RegionChangeListener;
import ru.dzhibrony.legacyregion.service.RegionService;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class RegionHologramManager implements RegionChangeListener {

    private static final double SAMPLE_DENSITY = 4.0;

    private final PluginBase plugin;
    private final HologramSettings settings;
    private final RegionService regionService;
    private final Map<String, HologramView> holograms = new HashMap<>();
    private boolean closed;

    public RegionHologramManager(PluginBase plugin, HologramSettings settings, RegionService regionService) {
        this.plugin = plugin;
        this.settings = settings;
        this.regionService = regionService;
    }

    public void start(Collection<Region> regions) {
        if (!this.settings.enabled()) {
            return;
        }
        this.closed = false;
        regions.forEach(this::createHologram);
        this.scheduleVisibilityTask();
    }

    public void close() {
        this.closed = true;
        this.holograms.values().forEach(HologramView::hideAll);
        this.holograms.clear();
    }

    @Override
    public void onRegionSaved(Region region) {
        if (!this.closed && this.settings.enabled()) {
            this.createHologram(region);
        }
    }

    @Override
    public void onRegionDeleted(Region region) {
        if (this.closed) {
            return;
        }
        HologramView view = this.holograms.remove(region.id());
        if (view != null) {
            view.hideAll();
        }
    }

    private void createHologram(Region region) {
        this.onRegionDeleted(region);
        Level level = this.plugin.getServer().getLevelByName(region.location().levelName());
        if (level != null) {
            this.holograms.put(region.id(), new HologramView(level, region));
        }
    }

    private void scheduleVisibilityTask() {
        HologramVisibilityTask task = new HologramVisibilityTask(this.plugin, this);
        this.plugin.getServer().getScheduler().scheduleDelayedRepeatingTask(task, 1, this.settings.safeUpdateIntervalTicks());
    }

    private void updateVisibility() {
        if (this.closed) {
            return;
        }
        this.holograms.values().forEach(HologramView::updateVisibility);
    }

    private boolean closed() {
        return this.closed;
    }

    private String title() {
        return TextFormat.colorize(this.settings.title());
    }

    private String text(Region region) {
        // The hologram uses the live region file name, not the value saved in the database.
        String text = this.settings.text()
                .replace("{name}", this.regionService.displayName(region))
                .replace("{owner}", region.ownerName());
        return TextFormat.colorize(text);
    }

    private final class HologramView {

        private final Level level;
        private final Vector3 position;
        private final FloatingTextParticle particle;
        private final Map<UUID, Player> viewers = new HashMap<>();

        private HologramView(Level level, Region region) {
            this.level = level;
            this.position = RegionHologramManager.this.position(region);
            Location location = Location.fromObject(this.position, level);
            this.particle = new FloatingTextParticle(location, RegionHologramManager.this.title(), RegionHologramManager.this.text(region));
        }

        private void updateVisibility() {
            Set<UUID> visibleNow = new HashSet<>();
            for (Player player : this.level.getPlayers().values()) {
                if (this.canSee(player)) {
                    visibleNow.add(player.getUniqueId());
                    this.show(player);
                }
            }
            this.hideMissing(visibleNow);
        }

        private void hideAll() {
            new HashSet<>(this.viewers.keySet()).forEach(this::hide);
        }

        private boolean canSee(Player player) {
            return player.isOnline()
                    && this.inDistance(player)
                    && RegionHologramManager.this.hasLineOfSight(player, this.level, this.position);
        }

        private boolean inDistance(Player player) {
            double distance = RegionHologramManager.this.settings.safeViewDistance();
            return player.distanceSquared(this.position) <= distance * distance;
        }

        private void show(Player player) {
            if (this.viewers.containsKey(player.getUniqueId())) {
                return;
            }
            this.particle.setInvisible(false);
            this.level.addParticle(this.particle, player);
            this.viewers.put(player.getUniqueId(), player);
        }

        private void hideMissing(Set<UUID> visibleNow) {
            new HashSet<>(this.viewers.keySet()).stream()
                    .filter(uuid -> !visibleNow.contains(uuid))
                    .forEach(this::hide);
        }

        private void hide(UUID uuid) {
            Player player = this.viewers.remove(uuid);
            if (player != null && player.isOnline()) {
                this.particle.setInvisible(true);
                this.level.addParticle(this.particle, player);
            }
        }
    }

    private Vector3 position(Region region) {
        return new Vector3(
                region.location().x() + 0.5,
                region.location().y() + this.settings.yOffset(),
                region.location().z() + 0.5
        );
    }

    private boolean hasLineOfSight(Player player, Level level, Vector3 target) {
        if (!this.settings.requireLineOfSight()) {
            return true;
        }
        return this.clearTrace(level, player.getEyePosition(), target);
    }

    private boolean clearTrace(Level level, Vector3 start, Vector3 target) {
        Vector3 delta = target.subtract(start);
        int steps = Math.max(1, (int) Math.ceil(start.distance(target) * SAMPLE_DENSITY));
        for (int step = 1; step < steps; step++) {
            if (this.blocksSight(level, start, delta, step, steps)) {
                return false;
            }
        }
        return true;
    }

    private boolean blocksSight(Level level, Vector3 start, Vector3 delta, int step, int steps) {
        double ratio = step / (double) steps;
        Vector3 point = start.add(delta.multiply(ratio));
        if (this.sameBlock(start, point)) {
            return false;
        }
        return this.occludes(level.getBlock(point, false));
    }

    private boolean occludes(Block block) {
        return block.isSolid() && !block.isTransparent() && !block.canPassThrough();
    }

    private boolean sameBlock(Vector3 first, Vector3 second) {
        return first.getFloorX() == second.getFloorX()
                && first.getFloorY() == second.getFloorY()
                && first.getFloorZ() == second.getFloorZ();
    }

    private static final class HologramVisibilityTask extends PluginTask<PluginBase> {

        private final RegionHologramManager manager;

        private HologramVisibilityTask(PluginBase owner, RegionHologramManager manager) {
            super(owner);
            this.manager = manager;
        }

        @Override
        public void onRun(int currentTick) {
            if (this.manager.closed()) {
                this.cancel();
                return;
            }
            this.manager.updateVisibility();
        }
    }
}
