package ru.dzhibrony.legacyregion.model;

import cn.nukkit.block.Block;

public record RegionDefinition(
        String key,
        String displayName,
        String fileName,
        String namespaceId,
        int legacyId,
        int damage,
        int radius,
        String buttonIcon,
        boolean breakFromExplosions
) {

    public boolean matches(Block block) {
        return this.matchesLegacy(block) || this.matchesNamespace(block);
    }

    private boolean matchesLegacy(Block block) {
        boolean damageMatches = this.damage < 0 || block.getDamage() == this.damage;
        return this.legacyId >= 0 && block.getId() == this.legacyId && damageMatches;
    }

    private boolean matchesNamespace(Block block) {
        String namespace = this.safeNamespace(block);
        return !this.namespaceId.isBlank() && this.namespaceId.equals(namespace);
    }

    private String safeNamespace(Block block) {
        try {
            return block.toItem().getNamespaceId();
        } catch (RuntimeException exception) {
            return "";
        }
    }
}
