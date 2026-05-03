package ru.dzhibrony.legacyregion.service;

import ru.dzhibrony.legacyregion.model.Region;

public interface RegionChangeListener {

    void onRegionSaved(Region region);

    void onRegionDeleted(Region region);
}
