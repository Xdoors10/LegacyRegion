package ru.dzhibrony.legacyregion.model;

import cn.nukkit.block.Block;
import cn.nukkit.level.LevelException;
import cn.nukkit.level.Position;

import java.util.Optional;

public record RegionLocation(String levelName, int x, int y, int z) {

    public static RegionLocation from(Block block) {
        return new RegionLocation(block.getLevelName(), block.getFloorX(), block.getFloorY(), block.getFloorZ());
    }

    public static RegionLocation from(Position position) {
        return new RegionLocation(position.getLevelName(), position.getFloorX(), position.getFloorY(), position.getFloorZ());
    }

    public static Optional<RegionLocation> tryFrom(Position position) {
        try {
            return Optional.of(from(position));
        } catch (LevelException ignored) {
            return Optional.empty();
        }
    }

    public String id() {
        return this.levelName + ":" + this.x + ":" + this.y + ":" + this.z;
    }

    public String coordinates() {
        return this.x + " " + this.y + " " + this.z;
    }
}
