package ru.dzhibrony.legacyregion.model;

import java.util.Locale;

public record RegionMember(String name, String key) {

    public static RegionMember of(String name) {
        return new RegionMember(name, normalize(name));
    }

    public static String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
