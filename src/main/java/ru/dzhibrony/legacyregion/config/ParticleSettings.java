package ru.dzhibrony.legacyregion.config;

public record ParticleSettings(
        boolean enabled,
        String identifier,
        int durationSeconds,
        int intervalTicks,
        int step
) {

    public int durationTicks() {
        return Math.max(1, this.durationSeconds) * 20;
    }

    public int safeIntervalTicks() {
        return Math.max(1, this.intervalTicks);
    }

    public int safeStep() {
        return Math.max(1, this.step);
    }
}
