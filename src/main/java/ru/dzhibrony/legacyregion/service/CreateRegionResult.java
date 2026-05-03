package ru.dzhibrony.legacyregion.service;

import ru.dzhibrony.legacyregion.model.Region;

public record CreateRegionResult(CreateRegionStatus status, Region region) {

    public static CreateRegionResult failed(CreateRegionStatus status) {
        return new CreateRegionResult(status, null);
    }

    public static CreateRegionResult created(Region region) {
        return new CreateRegionResult(CreateRegionStatus.CREATED, region);
    }
}
