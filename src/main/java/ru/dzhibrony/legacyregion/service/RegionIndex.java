package ru.dzhibrony.legacyregion.service;

import ru.dzhibrony.legacyregion.model.Region;
import ru.dzhibrony.legacyregion.model.RegionLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class RegionIndex {

    private static final int CELL_SIZE = 16;
    private static final Comparator<Region> BY_RADIUS = Comparator.comparingInt(Region::radius).thenComparing(Region::id);
    private static final Comparator<Region> BY_ID = Comparator.comparing(Region::id);

    private final Map<String, Region> byId = new LinkedHashMap<>();
    private final Map<String, LinkedHashSet<String>> byOwner = new LinkedHashMap<>();
    private final Map<String, Map<Long, LinkedHashSet<String>>> byLevelCell = new LinkedHashMap<>();

    void replaceAll(Collection<Region> regions) {
        this.clear();
        regions.forEach(this::put);
    }

    void put(Region region) {
        this.remove(region.id());
        this.byId.put(region.id(), region);
        this.byOwner.computeIfAbsent(region.ownerKey(), key -> new LinkedHashSet<>()).add(region.id());
        this.indexCells(region);
    }

    void remove(String id) {
        this.removeFromOwners(id);
        this.removeFromCells(id);
        this.byId.remove(id);
    }

    Optional<Region> byId(String id) {
        return Optional.ofNullable(this.byId.get(id));
    }

    Optional<Region> anchorAt(RegionLocation location) {
        return this.byId(location.id())
                .filter(region -> region.location().equals(location));
    }

    List<Region> allRegions() {
        return new ArrayList<>(this.byId.values());
    }

    List<Region> ownedBy(String ownerKey) {
        return this.regionsFromIds(this.byOwner.get(ownerKey)).stream()
                .sorted(BY_ID)
                .toList();
    }

    List<Region> regionsAt(RegionLocation location) {
        return this.regionsFromIds(this.cellIds(location.levelName(), this.cell(location.x()), this.cell(location.z()))).stream()
                .filter(region -> region.contains(location))
                .sorted(BY_RADIUS)
                .toList();
    }

    List<Region> intersecting(Region target) {
        Set<String> ids = new LinkedHashSet<>();
        for (int cellX = this.minCell(target.minX()); cellX <= this.maxCell(target.maxX()); cellX++) {
            for (int cellZ = this.minCell(target.minZ()); cellZ <= this.maxCell(target.maxZ()); cellZ++) {
                LinkedHashSet<String> cellIds = this.cellIds(target.location().levelName(), cellX, cellZ);
                if (cellIds != null) {
                    ids.addAll(cellIds);
                }
            }
        }
        return this.regionsFromIds(ids).stream()
                .filter(region -> !region.id().equals(target.id()))
                .filter(target::intersects)
                .sorted(BY_RADIUS)
                .toList();
    }

    private void clear() {
        this.byId.clear();
        this.byOwner.clear();
        this.byLevelCell.clear();
    }

    private void indexCells(Region region) {
        Map<Long, LinkedHashSet<String>> levelCells = this.byLevelCell.computeIfAbsent(
                region.location().levelName(), key -> new LinkedHashMap<>());
        for (int cellX = this.minCell(region.minX()); cellX <= this.maxCell(region.maxX()); cellX++) {
            for (int cellZ = this.minCell(region.minZ()); cellZ <= this.maxCell(region.maxZ()); cellZ++) {
                levelCells.computeIfAbsent(this.cellKey(cellX, cellZ), key -> new LinkedHashSet<>()).add(region.id());
            }
        }
    }

    private void removeFromOwners(String id) {
        Region region = this.byId.get(id);
        if (region == null) {
            return;
        }
        LinkedHashSet<String> ids = this.byOwner.get(region.ownerKey());
        if (ids != null) {
            ids.remove(id);
            if (ids.isEmpty()) {
                this.byOwner.remove(region.ownerKey());
            }
        }
    }

    private void removeFromCells(String id) {
        Region region = this.byId.get(id);
        if (region == null) {
            return;
        }
        Map<Long, LinkedHashSet<String>> levelCells = this.byLevelCell.get(region.location().levelName());
        if (levelCells == null) {
            return;
        }
        for (int cellX = this.minCell(region.minX()); cellX <= this.maxCell(region.maxX()); cellX++) {
            for (int cellZ = this.minCell(region.minZ()); cellZ <= this.maxCell(region.maxZ()); cellZ++) {
                LinkedHashSet<String> ids = levelCells.get(this.cellKey(cellX, cellZ));
                if (ids != null) {
                    ids.remove(id);
                    if (ids.isEmpty()) {
                        levelCells.remove(this.cellKey(cellX, cellZ));
                    }
                }
            }
        }
        if (levelCells.isEmpty()) {
            this.byLevelCell.remove(region.location().levelName());
        }
    }

    private List<Region> regionsFromIds(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Region> regions = new ArrayList<>();
        ids.forEach(id -> this.byId(id).ifPresent(regions::add));
        return regions;
    }

    private LinkedHashSet<String> cellIds(String levelName, int cellX, int cellZ) {
        Map<Long, LinkedHashSet<String>> levelCells = this.byLevelCell.get(levelName);
        if (levelCells == null) {
            return null;
        }
        return levelCells.get(this.cellKey(cellX, cellZ));
    }

    private int minCell(int coordinate) {
        return this.cell(coordinate);
    }

    private int maxCell(int coordinate) {
        return this.cell(coordinate);
    }

    private int cell(int coordinate) {
        return Math.floorDiv(coordinate, CELL_SIZE);
    }

    private long cellKey(int cellX, int cellZ) {
        return (((long) cellX) << 32) ^ (cellZ & 0xffffffffL);
    }
}
