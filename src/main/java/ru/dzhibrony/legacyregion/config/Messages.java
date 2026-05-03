package ru.dzhibrony.legacyregion.config;

import java.util.Map;

public record Messages(
        String prefix,
        String toastTitle,
        String playerOnly,
        String usageAddMember,
        String usageRemoveMember,
        String installModeEnter,
        String installModeExit,
        String installModeNotActive,
        String notRegionBlock,
        String regionCreated,
        String regionOverlap,
        String actionDenied,
        String actionDeniedDisplay,
        String noOwnedRegions,
        String noRegionForCommand,
        String memberAddedToast,
        String memberRemovedToast,
        String playerNeverJoinedToast,
        String memberAlreadyAdded,
        String memberNotFound,
        String ownerChanged,
        String regionUpdated,
        String regionDeleted,
        String cannotAddYourself,
        String emptyMembers,
        String deletedRegionMenuName,
        String hiddenCoordinates,
        String reloadSuccess,
        String memberCannotBreakRegionBlock,
        Map<String, MessageChannel> channels
) {
    public static final String PLAYER_ONLY = "player-only";
    public static final String USAGE_ADD_MEMBER = "usage-addmember";
    public static final String USAGE_REMOVE_MEMBER = "usage-removemember";
    public static final String INSTALL_MODE_ENTER = "install-mode-enter";
    public static final String INSTALL_MODE_EXIT = "install-mode-exit";
    public static final String INSTALL_MODE_NOT_ACTIVE = "install-mode-not-active";
    public static final String NOT_REGION_BLOCK = "not-region-block";
    public static final String REGION_CREATED = "region-created";
    public static final String REGION_OVERLAP = "region-overlap";
    public static final String ACTION_DENIED = "action-denied";
    public static final String NO_OWNED_REGIONS = "no-owned-regions";
    public static final String NO_REGION_FOR_COMMAND = "no-region-for-command";
    public static final String MEMBER_ADDED = "member-added";
    public static final String MEMBER_REMOVED = "member-removed";
    public static final String PLAYER_NEVER_JOINED = "player-never-joined";
    public static final String MEMBER_ALREADY_ADDED = "member-already-added";
    public static final String MEMBER_NOT_FOUND = "member-not-found";
    public static final String OWNER_CHANGED = "owner-changed";
    public static final String REGION_UPDATED = "region-updated";
    public static final String REGION_DELETED = "region-deleted";
    public static final String CANNOT_ADD_YOURSELF = "cannot-add-yourself";
    public static final String EMPTY_MEMBERS = "empty-members";
    public static final String RELOAD_SUCCESS = "reload-success";
    public static final String MEMBER_CANNOT_BREAK_REGION_BLOCK = "member-cannot-break-region-block";

    public Messages {
        channels = Map.copyOf(channels);
    }

    public MessageChannel channel(String key) {
        return this.channels.getOrDefault(key, MessageChannel.CHAT);
    }
}
