package ru.dzhibrony.legacyregion.service;

import cn.nukkit.block.Block;
import ru.dzhibrony.legacyregion.model.Region;
import ru.dzhibrony.legacyregion.model.RegionDefinition;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class RegionDefinitionRegistry {

    private List<RegionDefinition> definitions;
    private final Map<String, RegionDefinition> byKey = new LinkedHashMap<>();

    RegionDefinitionRegistry(List<RegionDefinition> definitions) {
        this.replace(definitions);
    }

    void replace(List<RegionDefinition> definitions) {
        this.definitions = List.copyOf(definitions);
        this.byKey.clear();
        this.definitions.forEach(definition -> this.byKey.put(definition.key(), definition));
    }

    Optional<RegionDefinition> match(Block block) {
        return this.definitions.stream()
                .filter(definition -> definition.matches(block))
                .findFirst();
    }

    Optional<RegionDefinition> byRegion(Region region) {
        return Optional.ofNullable(this.byKey.get(region.definitionKey()));
    }
}
