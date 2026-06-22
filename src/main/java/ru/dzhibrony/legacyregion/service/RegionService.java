package ru.dzhibrony.legacyregion.service;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import ru.dzhibrony.legacyregion.config.ProtectionSettings;
import ru.dzhibrony.legacyregion.model.Region;
import ru.dzhibrony.legacyregion.model.RegionDefinition;
import ru.dzhibrony.legacyregion.model.RegionLocation;
import ru.dzhibrony.legacyregion.model.RegionMember;
import ru.dzhibrony.legacyregion.storage.RegionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RegionService {

    private final RegionRepository repository;
    private ProtectionSettings protectionSettings;
    private final RegionDefinitionRegistry definitions;
    private final RegionIndex regions = new RegionIndex();
    private final List<RegionChangeListener> listeners = new ArrayList<>();

    public RegionService(RegionRepository repository, ProtectionSettings protectionSettings, List<RegionDefinition> definitions) {
        this.repository = repository;
        this.protectionSettings = protectionSettings;
        this.definitions = new RegionDefinitionRegistry(definitions);
    }

    public void load() {
        this.repository.initialize();
        this.regions.replaceAll(this.repository.loadRegions());
    }

    public CreateRegionResult create(Player player, Block block) {
        Optional<RegionDefinition> definition = this.definition(block);
        if (definition.isEmpty()) {
            return CreateRegionResult.failed(CreateRegionStatus.NOT_REGION_BLOCK);
        }
        return this.createWithDefinition(player, block, definition.get());
    }

    public void save(Region region) {
        this.repository.saveRegion(region);
        this.regions.put(region);
        this.listeners.forEach(listener -> listener.onRegionSaved(region));
    }

    public void delete(Region region) {
        this.repository.deleteRegion(region.id());
        this.regions.remove(region.id());
        this.listeners.forEach(listener -> listener.onRegionDeleted(region));
    }

    public void addMember(Region region, RegionMember member) {
        region.addMember(member);
        this.repository.addMember(region.id(), member);
    }

    public void removeMember(Region region, String memberKey) {
        region.removeMember(memberKey);
        this.repository.removeMember(region.id(), memberKey);
    }

    public List<Region> regionsAt(RegionLocation location) {
        return this.regions.regionsAt(location);
    }

    public List<Region> ownedRegions(Player player) {
        String key = RegionMember.normalize(player.getName());
        return this.regions.ownedBy(key);
    }

    public Optional<Region> commandRegion(Player player) {
        List<Region> atPlayer = this.ownedAtPlayer(player);
        if (!atPlayer.isEmpty()) {
            return Optional.of(atPlayer.get(0));
        }
        return this.singleOwnedRegion(player);
    }

    public Optional<Region> byId(String id) {
        return this.regions.byId(id);
    }

    public Optional<Region> anchorAt(RegionLocation location) {
        return this.regions.anchorAt(location);
    }

    public List<Region> allRegions() {
        return this.regions.allRegions();
    }

    public void addListener(RegionChangeListener listener) {
        this.listeners.add(listener);
    }

    public void clearListeners() {
        this.listeners.clear();
    }

    public void updateConfiguration(ProtectionSettings protectionSettings, List<RegionDefinition> definitions) {
        this.protectionSettings = protectionSettings;
        this.definitions.replace(definitions);
    }

    public Optional<RegionDefinition> definition(Block block) {
        return this.definitions.match(block);
    }

    public String displayName(Region region) {
        return this.definition(region)
                .map(RegionDefinition::displayName)
                .orElse(region.displayName());
    }

    public String buttonIcon(Region region) {
        return this.definition(region)
                .map(RegionDefinition::buttonIcon)
                .orElse("");
    }

    public boolean breakRegionBlockFromExplosions(Region region) {
        return this.definition(region)
                .map(RegionDefinition::breakFromExplosions)
                .orElse(false);
    }

    public Optional<RegionDefinition> definition(Region region) {
        return this.definitions.byRegion(region);
    }

    private CreateRegionResult createWithDefinition(Player player, Block block, RegionDefinition definition) {
        Region region = this.newRegion(player, block, definition);
        if (this.overlaps(region)) {
            return CreateRegionResult.failed(CreateRegionStatus.OVERLAP);
        }
        this.save(region);
        return CreateRegionResult.created(region);
    }

    private Region newRegion(Player player, Block block, RegionDefinition definition) {
        return new Region(
                RegionLocation.from(block),
                definition.radius(),
                definition.key(),
                definition.displayName(),
                RegionMember.of(player.getName())
        );
    }

    private boolean overlaps(Region region) {
        return this.protectionSettings.preventOverlap()
                && !this.regions.intersecting(region).isEmpty();
    }

    private List<Region> ownedAtPlayer(Player player) {
        RegionLocation location = RegionLocation.from(player);
        return this.regionsAt(location).stream()
                .filter(region -> region.isOwner(RegionMember.normalize(player.getName())))
                .toList();
    }

    private Optional<Region> singleOwnedRegion(Player player) {
        List<Region> owned = this.ownedRegions(player);
        return owned.size() == 1 ? Optional.of(owned.get(0)) : Optional.empty();
    }
}
