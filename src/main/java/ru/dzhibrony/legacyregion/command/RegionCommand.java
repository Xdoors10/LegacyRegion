package ru.dzhibrony.legacyregion.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandData;
import cn.nukkit.command.data.CommandDataVersions;
import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.command.data.CommandOverload;
import cn.nukkit.command.data.CommandParameter;
import ru.dzhibrony.legacyregion.command.args.RegionCommandOverloads;
import ru.dzhibrony.legacyregion.config.CommandSettings;
import ru.dzhibrony.legacyregion.config.Messages;
import ru.dzhibrony.legacyregion.menu.RegionMenu;
import ru.dzhibrony.legacyregion.service.InstallModeService;
import ru.dzhibrony.legacyregion.service.MessageService;
import ru.dzhibrony.legacyregion.service.RegionMemberService;
import ru.dzhibrony.legacyregion.service.RegionService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class RegionCommand extends Command {

    private static final String RELOAD_PERMISSION = "legacyregion.reload";

    private final RegionService regionService;
    private final MessageService messageService;
    private final RegionMenu menu;

    private final AddMemberHandler addMemberHandler;
    private final RemoveMemberHandler removeMemberHandler;
    private final InstallModeHandler installModeHandler;
    private final ReloadHandler reloadHandler;

    public RegionCommand(CommandSettings settings, RegionMenu menu, RegionService regionService,
                         RegionMemberService memberService, InstallModeService installModeService,
                         MessageService messageService, Runnable reloadAction) {
        super(settings.name(), settings.description(), "/" + settings.name(), settings.aliases().toArray(String[]::new));
        this.regionService = regionService;
        this.messageService = messageService;
        this.menu = menu;

        RegionCommandTarget target = new RegionCommandTarget(regionService, messageService);
        this.addMemberHandler = new AddMemberHandler(target, memberService, messageService);
        this.removeMemberHandler = new RemoveMemberHandler(target, memberService, messageService);
        this.installModeHandler = new InstallModeHandler(installModeService, messageService);
        this.reloadHandler = new ReloadHandler(messageService, reloadAction);

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
        this.dispatch(player, commandLabel, args);
        return true;
    }

    @Override
    public CommandDataVersions generateCustomCommandData(Player player) {
        if (!this.testPermissionSilent(player)) {
            return null;
        }
        CommandData data = this.commandData.clone();
        data.description = this.getDescription();

        String[] aliases = this.getAliases();
        if (aliases.length > 0) {
            List<String> aliasList = new ArrayList<>(Arrays.asList(aliases));
            if (!aliasList.contains(this.getName())) {
                aliasList.add(this.getName());
            }
            data.aliases = new CommandEnum(this.getName() + "Aliases", aliasList);
        }

        data.overloads = new LinkedHashMap<>();
        Map<String, CommandParameter[]> parameters = RegionCommandOverloads.forPlayer(player, this.regionService);
        parameters.forEach((key, params) -> {
            CommandOverload overload = new CommandOverload();
            overload.input.parameters = params;
            data.overloads.put(key, overload);
        });

        CommandDataVersions versions = new CommandDataVersions();
        versions.versions.add(data);
        return versions;
    }

    private void dispatch(Player player, String commandLabel, String[] args) {
        if (args.length == 0) {
            this.menu.openRegions(player);
            return;
        }
        String subcommand = args[0].toLowerCase(Locale.ROOT);
        switch (subcommand) {
            case "addmember" -> this.addMemberHandler.execute(player, args);
            case "removemember" -> this.removeMemberHandler.execute(player, args);
            case "addregion" -> this.installModeHandler.enter(player);
            case "back" -> this.installModeHandler.leave(player);
            case "reload" -> this.reloadHandler.execute(player, RELOAD_PERMISSION);
            default -> this.sendUnknownCommand(player, commandLabel);
        }
    }

    private void configure(CommandSettings settings) {
        this.setCommandParameters(RegionCommandOverloads.base());
        if (!settings.permission().isBlank()) {
            this.setPermission(settings.permission());
            this.setPermissionMessage(settings.permissionMessage());
        }
    }

    private void sendUnknownCommand(Player player, String commandLabel) {
        player.sendMessage(new cn.nukkit.lang.TranslationContainer(
                cn.nukkit.utils.TextFormat.RED + "%commands.generic.unknown", commandLabel));
    }
}
