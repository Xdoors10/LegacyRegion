package ru.dzhibrony.legacyregion.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandData;
import cn.nukkit.command.data.CommandDataVersions;
import cn.nukkit.command.data.CommandOverload;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.command.tree.node.PlayersNode;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.utils.TextFormat;
import ru.dzhibrony.legacyregion.config.CommandSettings;
import ru.dzhibrony.legacyregion.config.Messages;
import ru.dzhibrony.legacyregion.menu.RegionMenu;
import ru.dzhibrony.legacyregion.model.Region;
import ru.dzhibrony.legacyregion.model.RegionMember;
import ru.dzhibrony.legacyregion.service.AddMemberStatus;
import ru.dzhibrony.legacyregion.service.InstallModeService;
import ru.dzhibrony.legacyregion.service.MessageService;
import ru.dzhibrony.legacyregion.service.RegionMemberService;
import ru.dzhibrony.legacyregion.service.RegionService;
import ru.dzhibrony.legacyregion.service.RemoveMemberStatus;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class RegionCommand extends Command {

    private static final String[] SUBCOMMANDS = {"addmember", "removemember", "addregion", "back"};

    private final RegionMenu menu;
    private final RegionService regionService;
    private final RegionMemberService memberService;
    private final InstallModeService installModeService;
    private final MessageService messageService;
    private final Runnable reloadAction;

    public RegionCommand(CommandSettings settings, RegionMenu menu, RegionService regionService,
                         RegionMemberService memberService, InstallModeService installModeService,
                         MessageService messageService, Runnable reloadAction) {
        super(settings.name(), settings.description(), "/" + settings.name(), settings.aliases().toArray(String[]::new));
        this.menu = menu;
        this.regionService = regionService;
        this.memberService = memberService;
        this.installModeService = installModeService;
        this.messageService = messageService;
        this.reloadAction = reloadAction;
        this.configure(settings);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }
        if (!(sender instanceof Player player)) {
            this.messageService.send(sender, Messages.PLAYER_ONLY, this.messageService.messages().playerOnly());
            return true;
        }
        this.executePlayer(player, commandLabel, args);
        return true;
    }

    @Override
    public CommandDataVersions generateCustomCommandData(Player player) {
        Map<String, CommandParameter[]> previousParameters = this.getCommandParameters();
        Map<String, CommandOverload> previousOverloads = this.getDefaultCommandData().overloads;
        this.setCommandParameters(this.parametersFor(player));
        this.getDefaultCommandData().overloads = new LinkedHashMap<>();
        try {
            CommandDataVersions versions = super.generateCustomCommandData(player);
            versions.versions.replaceAll(this::detachOverloads);
            return versions;
        } finally {
            this.setCommandParameters(previousParameters);
            this.getDefaultCommandData().overloads = previousOverloads;
        }
    }

    private void executePlayer(Player player, String commandLabel, String[] args) {
        if (args.length == 0) {
            this.menu.openRegions(player);
            return;
        }
        switch (args[0].toLowerCase()) {
            case "addregion" -> this.enterInstallMode(player);
            case "back" -> this.leaveInstallMode(player);
            case "addmember" -> this.addMember(player, args);
            case "removemember" -> this.removeMember(player, args);
            case "reload" -> this.reload(player, commandLabel);
            default -> this.sendUnknownCommand(player, commandLabel);
        }
    }

    private void reload(Player player, String commandLabel) {
        if (!player.isOp()) {
            this.sendUnknownCommand(player, commandLabel);
            return;
        }
        this.reloadAction.run();
        this.messageService.send(player, Messages.RELOAD_SUCCESS, this.messageService.messages().reloadSuccess());
    }

    private void sendUnknownCommand(CommandSender sender, String commandLabel) {
        // Используем тот же ключ перевода, который Lumi отправляет для неизвестных команд.
        sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.unknown", commandLabel));
    }

    private void enterInstallMode(Player player) {
        this.installModeService.enter(player);
        this.messageService.send(player, Messages.INSTALL_MODE_ENTER, this.messageService.messages().installModeEnter());
    }

    private void leaveInstallMode(Player player) {
        if (this.installModeService.leave(player)) {
            this.messageService.send(player, Messages.INSTALL_MODE_EXIT, this.messageService.messages().installModeExit());
            return;
        }
        this.messageService.send(player, Messages.INSTALL_MODE_NOT_ACTIVE, this.messageService.messages().installModeNotActive());
    }

    private void addMember(Player player, String[] args) {
        if (args.length < 2) {
            this.messageService.send(player, Messages.USAGE_ADD_MEMBER, this.messageService.messages().usageAddMember());
            return;
        }
        this.targetRegion(player).ifPresent(region -> this.addMemberToRegion(player, region, args[1]));
    }

    private void addMemberToRegion(Player player, Region region, String targetName) {
        AddMemberStatus status = this.memberService.addMember(region, targetName);
        switch (status) {
            case ADDED -> {
                this.messageService.send(player, Messages.MEMBER_ADDED, this.messageService.messages().memberAddedToast());
                this.refreshCommandData(player);
            }
            case PLAYER_NEVER_JOINED -> this.messageService.send(player, Messages.PLAYER_NEVER_JOINED, this.messageService.messages().playerNeverJoinedToast());
            case ALREADY_MEMBER -> this.messageService.send(player, Messages.MEMBER_ALREADY_ADDED, this.messageService.messages().memberAlreadyAdded());
            case OWNER -> this.messageService.send(player, Messages.CANNOT_ADD_YOURSELF, this.messageService.messages().cannotAddYourself());
        }
    }

    private void removeMember(Player player, String[] args) {
        if (args.length < 2) {
            this.messageService.send(player, Messages.USAGE_REMOVE_MEMBER, this.messageService.messages().usageRemoveMember());
            return;
        }
        this.targetRegion(player).ifPresent(region -> this.removeMemberFromRegion(player, region, args[1]));
    }

    private void removeMemberFromRegion(Player player, Region region, String targetName) {
        String memberKey = RegionMember.normalize(targetName);
        RemoveMemberStatus status = this.memberService.removeMember(region, memberKey);
        if (status == RemoveMemberStatus.REMOVED) {
            this.messageService.send(player, Messages.MEMBER_REMOVED, this.messageService.messages().memberRemovedToast());
            this.refreshCommandData(player);
            return;
        }
        this.messageService.send(player, Messages.MEMBER_NOT_FOUND, this.messageService.messages().memberNotFound());
    }

    private Optional<Region> targetRegion(Player player) {
        Optional<Region> region = this.regionService.commandRegion(player);
        if (region.isEmpty()) {
            this.messageService.send(player, Messages.NO_REGION_FOR_COMMAND, this.messageService.messages().noRegionForCommand());
        }
        return region;
    }

    private void configure(CommandSettings settings) {
        this.setCommandParameters(this.parametersFor(null));
        if (!settings.permission().isBlank()) {
            this.setPermission(settings.permission());
            this.setPermissionMessage(settings.permissionMessage());
        }
    }

    private Map<String, CommandParameter[]> parametersFor(Player player) {
        Map<String, CommandParameter[]> parameters = new LinkedHashMap<>();
        parameters.put("subcommand", new CommandParameter[]{
                CommandParameter.newEnum("subcommand", SUBCOMMANDS)
        });
        parameters.put("add-member", new CommandParameter[]{
                CommandParameter.newEnum("subcommand", new String[]{"addmember"}),
                CommandParameter.newType("player", CommandParamType.TARGET, new PlayersNode())
        });
        this.addRemoveMemberParameters(parameters, player);
        return parameters;
    }

    private CommandData detachOverloads(CommandData data) {
        CommandData copy = data.clone();
        copy.overloads = new LinkedHashMap<>(data.overloads);
        return copy;
    }

    private void addRemoveMemberParameters(Map<String, CommandParameter[]> parameters, Player player) {
        String[] members = this.removableMembers(player);
        if (members.length == 0) {
            return;
        }
        parameters.put("remove-member", new CommandParameter[]{
                CommandParameter.newEnum("subcommand", new String[]{"removemember"}),
                CommandParameter.newEnum("player", members)
        });
    }

    private String[] removableMembers(Player player) {
        if (player == null) {
            return new String[0];
        }
        return this.regionService.commandRegion(player)
                .map(this::memberNames)
                .orElseGet(() -> new String[0]);
    }

    private String[] memberNames(Region region) {
        return region.members().stream()
                .map(RegionMember::name)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toArray(String[]::new);
    }

    private void refreshCommandData(Player player) {
        player.sendCommandData();
    }
}
