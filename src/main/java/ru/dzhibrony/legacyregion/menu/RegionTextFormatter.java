package ru.dzhibrony.legacyregion.menu;

import ru.dzhibrony.legacyregion.config.MenuText;
import ru.dzhibrony.legacyregion.config.Messages;
import ru.dzhibrony.legacyregion.model.Region;

public final class RegionTextFormatter {

    private final MenuText menuText;
    private final Messages messages;

    public RegionTextFormatter(MenuText menuText, Messages messages) {
        this.menuText = menuText;
        this.messages = messages;
    }

    public String regionTitle(Region region) {
        return this.applyRegion(this.menuText.regionTitleFormat(), region);
    }

    public String regionButton(Region region) {
        String format = region.hiddenCoordinates()
                ? this.menuText.regionButtonHiddenFormat()
                : this.menuText.regionButtonFormat();
        return this.applyRegion(format, region);
    }

    private String applyRegion(String text, Region region) {
        return text.replace("{name}", region.displayName())
                .replace("{x}", String.valueOf(region.location().x()))
                .replace("{y}", String.valueOf(region.location().y()))
                .replace("{z}", String.valueOf(region.location().z()))
                .replace("{hidden}", this.messages.hiddenCoordinates());
    }
}
