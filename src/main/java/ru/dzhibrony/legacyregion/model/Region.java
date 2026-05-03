package ru.dzhibrony.legacyregion.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Region {

    private final String id;
    private final RegionLocation location;
    private final int radius;
    private final String definitionKey;
    private final String displayName;
    private final Map<String, RegionMember> members = new LinkedHashMap<>();
    private String ownerName;
    private String ownerKey;
    private boolean hiddenCoordinates;
    private boolean membersCanBreakRegionBlock;

    public Region(RegionLocation location, int radius, String definitionKey, String displayName, RegionMember owner) {
        this.id = location.id();
        this.location = location;
        this.radius = radius;
        this.definitionKey = definitionKey;
        this.displayName = displayName;
        this.ownerName = owner.name();
        this.ownerKey = owner.key();
    }

    public boolean contains(RegionLocation point) {
        return this.sameLevel(point) && this.insideCube(point);
    }

    public boolean intersects(Region other) {
        return this.sameLevel(other.location) && this.overlapsAxis(other);
    }

    public boolean isOwner(String playerKey) {
        return this.ownerKey.equals(playerKey);
    }

    public boolean isMember(String playerKey) {
        return this.members.containsKey(playerKey);
    }

    public void addMember(RegionMember member) {
        this.members.put(member.key(), member);
    }

    public void removeMember(String playerKey) {
        this.members.remove(playerKey);
    }

    public void transferOwner(RegionMember newOwner, boolean keepOldOwnerAsMember) {
        RegionMember oldOwner = new RegionMember(this.ownerName, this.ownerKey);
        this.members.remove(newOwner.key());
        this.ownerName = newOwner.name();
        this.ownerKey = newOwner.key();
        this.addOldOwner(oldOwner, keepOldOwnerAsMember);
    }

    private void addOldOwner(RegionMember oldOwner, boolean keepOldOwnerAsMember) {
        if (keepOldOwnerAsMember) {
            this.addMember(oldOwner);
        }
    }

    private boolean overlapsAxis(Region other) {
        return this.minX() <= other.maxX() && this.maxX() >= other.minX()
                && this.minY() <= other.maxY() && this.maxY() >= other.minY()
                && this.minZ() <= other.maxZ() && this.maxZ() >= other.minZ();
    }

    private boolean insideCube(RegionLocation point) {
        return point.x() >= this.minX() && point.x() <= this.maxX()
                && point.y() >= this.minY() && point.y() <= this.maxY()
                && point.z() >= this.minZ() && point.z() <= this.maxZ();
    }

    private boolean sameLevel(RegionLocation point) {
        return this.location.levelName().equals(point.levelName());
    }

    public int minX() {
        return this.location.x() - this.radius;
    }

    public int maxX() {
        return this.location.x() + this.radius;
    }

    public int minY() {
        return this.location.y() - this.radius;
    }

    public int maxY() {
        return this.location.y() + this.radius;
    }

    public int minZ() {
        return this.location.z() - this.radius;
    }

    public int maxZ() {
        return this.location.z() + this.radius;
    }

    public String id() {
        return this.id;
    }

    public RegionLocation location() {
        return this.location;
    }

    public int radius() {
        return this.radius;
    }

    public String definitionKey() {
        return this.definitionKey;
    }

    public String displayName() {
        return this.displayName;
    }

    public String ownerName() {
        return this.ownerName;
    }

    public String ownerKey() {
        return this.ownerKey;
    }

    public boolean hiddenCoordinates() {
        return this.hiddenCoordinates;
    }

    public void hiddenCoordinates(boolean hiddenCoordinates) {
        this.hiddenCoordinates = hiddenCoordinates;
    }

    public boolean membersCanBreakRegionBlock() {
        return this.membersCanBreakRegionBlock;
    }

    public void membersCanBreakRegionBlock(boolean membersCanBreakRegionBlock) {
        this.membersCanBreakRegionBlock = membersCanBreakRegionBlock;
    }

    public Collection<RegionMember> members() {
        return this.members.values();
    }
}
