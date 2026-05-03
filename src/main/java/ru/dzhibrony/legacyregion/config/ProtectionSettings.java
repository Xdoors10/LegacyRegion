package ru.dzhibrony.legacyregion.config;

public record ProtectionSettings(
        boolean opBypass,
        boolean preventOverlap,
        boolean denyDropItem,
        boolean denyEntityDamage,
        boolean protectFireSpread,
        boolean deniedMessageEnabled,
        boolean deniedParticleEnabled,
        String deniedParticle
) {
}
