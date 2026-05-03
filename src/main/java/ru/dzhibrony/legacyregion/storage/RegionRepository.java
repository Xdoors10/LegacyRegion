package ru.dzhibrony.legacyregion.storage;

import ru.dzhibrony.legacyregion.model.Region;
import ru.dzhibrony.legacyregion.model.RegionMember;

import java.util.List;

public interface RegionRepository {

    void initialize();

    List<Region> loadRegions();

    void saveRegion(Region region);

    void deleteRegion(String regionId);

    void addMember(String regionId, RegionMember member);

    void removeMember(String regionId, String memberKey);
}
