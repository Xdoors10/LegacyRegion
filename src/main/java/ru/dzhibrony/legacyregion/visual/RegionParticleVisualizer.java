package ru.dzhibrony.legacyregion.visual;

import cn.nukkit.level.Level;
import cn.nukkit.level.particle.PortalParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.PluginTask;
import ru.dzhibrony.legacyregion.config.ParticleSettings;
import ru.dzhibrony.legacyregion.model.Region;

import java.util.HashSet;
import java.util.Set;
import java.util.function.IntConsumer;

public final class RegionParticleVisualizer {

    private final PluginBase plugin;
    private ParticleSettings settings;

    public RegionParticleVisualizer(PluginBase plugin, ParticleSettings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    public void show(Region region) {
        if (!this.settings.enabled()) {
            return;
        }
        RegionParticleTask task = new RegionParticleTask(this.plugin, this.settings, region);
        this.plugin.getServer().getScheduler().scheduleDelayedRepeatingTask(task, 0, this.settings.safeIntervalTicks());
    }

    public void updateSettings(ParticleSettings settings) {
        this.settings = settings;
    }

    private static final class RegionParticleTask extends PluginTask<PluginBase> {

        private final ParticleSettings settings;
        private final Region region;
        private int runs;

        private RegionParticleTask(PluginBase owner, ParticleSettings settings, Region region) {
            super(owner);
            this.settings = settings;
            this.region = region;
        }

        @Override
        public void onRun(int currentTick) {
            Level level = this.owner.getServer().getLevelByName(this.region.location().levelName());
            if (level == null) {
                this.cancel();
                return;
            }
            this.render(level);
            this.runs++;
            if (this.finished()) {
                this.cancel();
            }
        }

        private boolean finished() {
            return this.runs * this.settings.safeIntervalTicks() >= this.settings.durationTicks();
        }

        private void render(Level level) {
            int step = this.settings.safeStep();
            Set<String> rendered = new HashSet<>();
            int minX = this.region.minX();
            int maxX = this.region.maxX() + 1;
            int minY = this.region.minY();
            int maxY = this.region.maxY() + 1;
            int minZ = this.region.minZ();
            int maxZ = this.region.maxZ() + 1;

            this.renderAxis(minX, maxX, step, x -> this.renderXEdge(rendered, level, x, minY, maxY, minZ, maxZ));
            this.renderAxis(minY, maxY, step, y -> this.renderYEdge(rendered, level, y, minX, maxX, minZ, maxZ));
            this.renderAxis(minZ, maxZ, step, z -> this.renderZEdge(rendered, level, z, minX, maxX, minY, maxY));
        }

        private void renderAxis(int min, int max, int step, IntConsumer renderer) {
            for (int point = min; point <= max; point += step) {
                renderer.accept(point);
            }
            if ((max - min) % step != 0) {
                renderer.accept(max);
            }
        }

        private void renderXEdge(Set<String> rendered, Level level, int x, int minY, int maxY, int minZ, int maxZ) {
            this.spawn(rendered, level, x, minY, minZ);
            this.spawn(rendered, level, x, minY, maxZ);
            this.spawn(rendered, level, x, maxY, minZ);
            this.spawn(rendered, level, x, maxY, maxZ);
        }

        private void renderYEdge(Set<String> rendered, Level level, int y, int minX, int maxX, int minZ, int maxZ) {
            this.spawn(rendered, level, minX, y, minZ);
            this.spawn(rendered, level, minX, y, maxZ);
            this.spawn(rendered, level, maxX, y, minZ);
            this.spawn(rendered, level, maxX, y, maxZ);
        }

        private void renderZEdge(Set<String> rendered, Level level, int z, int minX, int maxX, int minY, int maxY) {
            this.spawn(rendered, level, minX, minY, z);
            this.spawn(rendered, level, minX, maxY, z);
            this.spawn(rendered, level, maxX, minY, z);
            this.spawn(rendered, level, maxX, maxY, z);
        }

        private void spawn(Set<String> rendered, Level level, int x, int y, int z) {
            if (!rendered.add(x + ":" + y + ":" + z)) {
                return;
            }
            Vector3 position = new Vector3(x, y, z);
            String identifier = this.particleIdentifier();
            if ("portal_particle".equalsIgnoreCase(identifier)) {
                level.addParticle(new PortalParticle(position));
                return;
            }
            level.addParticleEffect(position.asVector3f(), identifier, -1, level.getDimension());
        }

        private String particleIdentifier() {
            String identifier = this.settings.identifier();
            if ("composter".equalsIgnoreCase(identifier) || "cumposter".equalsIgnoreCase(identifier)) {
                return "minecraft:crop_growth_emitter";
            }
            return identifier;
        }
    }
}
