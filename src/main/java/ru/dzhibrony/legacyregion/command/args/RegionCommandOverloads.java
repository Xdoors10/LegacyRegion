package ru.dzhibrony.legacyregion.command.args;

import cn.nukkit.Player;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.command.tree.node.PlayersNode;
import ru.dzhibrony.legacyregion.model.Region;
import ru.dzhibrony.legacyregion.model.RegionMember;
import ru.dzhibrony.legacyregion.service.RegionService;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Централизованное определение всех overload-ов (параметров) команды /region.
 * Вынесено в отдельный файл для читаемости и простоты масштабирования.
 */
public final class RegionCommandOverloads {

    /** Названия подкоманд, которые видит клиент в autocomplete. */
    public static final String[] SUBCOMMANDS = {"addmember", "removemember", "addregion", "back"};

    /** Подкоманды, требующие OP/permission (не показываются обычным игрокам). */
    public static final String[] ADMIN_SUBCOMMANDS = {"reload"};

    private RegionCommandOverloads() {
    }

    /**
     * Базовые overload-ы, не зависящие от конкретного игрока.
     */
    public static Map<String, CommandParameter[]> base() {
        Map<String, CommandParameter[]> parameters = new LinkedHashMap<>();
        parameters.put("subcommand", subcommandOverload());
        parameters.put("add-member", addMemberOverload());
        return parameters;
    }

    /**
     * Overload-ы, настроенные для конкретного игрока (динамический список мемберов).
     */
    public static Map<String, CommandParameter[]> forPlayer(Player player, RegionService regionService) {
        Map<String, CommandParameter[]> parameters = base();
        addRemoveMemberOverload(parameters, player, regionService);
        return parameters;
    }

    /**
     * Overload только с enum подкоманд.
     */
    public static CommandParameter[] subcommandOverload() {
        return new CommandParameter[]{
                CommandParameter.newEnum("subcommand", SUBCOMMANDS)
        };
    }

    /**
     * Overload для addmember: подкоманда + цель (игрок).
     */
    public static CommandParameter[] addMemberOverload() {
        return new CommandParameter[]{
                CommandParameter.newEnum("subcommand", new String[]{"addmember"}),
                CommandParameter.newType("player", CommandParamType.TARGET, new PlayersNode())
        };
    }

    /**
     * Overload для removemember с динамическим enum из мемберов региона.
     * Добавляется только если у игрока есть мемберы в текущем регионе.
     */
    public static void addRemoveMemberOverload(Map<String, CommandParameter[]> parameters,
                                               Player player, RegionService regionService) {
        String[] members = removableMembers(player, regionService);
        if (members.length == 0) {
            return;
        }
        parameters.put("remove-member", new CommandParameter[]{
                CommandParameter.newEnum("subcommand", new String[]{"removemember"}),
                CommandParameter.newEnum("player", members)
        });
    }

    private static String[] removableMembers(Player player, RegionService regionService) {
        if (player == null) {
            return new String[0];
        }
        return regionService.commandRegion(player)
                .map(RegionCommandOverloads::memberNames)
                .orElseGet(() -> new String[0]);
    }

    private static String[] memberNames(Region region) {
        return region.members().stream()
                .map(RegionMember::name)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toArray(String[]::new);
    }
}
