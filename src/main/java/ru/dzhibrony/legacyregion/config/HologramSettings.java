package ru.dzhibrony.legacyregion.config;

public record HologramSettings(
        boolean enabled,
        boolean requireLineOfSight,
        String title,
        String text,
        double yOffset,
        double viewDistance,
        int updateIntervalTicks
) {

    public int safeUpdateIntervalTicks() {
        return Math.max(1, this.updateIntervalTicks);
    }

    public double safeViewDistance() {
        return Math.max(1.0, this.viewDistance);
    }
}
