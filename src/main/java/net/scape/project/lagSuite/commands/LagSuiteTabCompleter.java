package net.scape.project.lagSuite.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LagSuiteTabCompleter implements TabCompleter {

    private static final List<String> MAIN_COMMANDS = List.of(
            "clear",
            "halt",
            "tps",
            "chunks",
            "reload",
            "status",
            "help",
            "tpchunk",
            "report"
    );

    private static final List<String> CLEAR_ARGS = List.of("-droppeditems");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (args.length == 1) {
            // Suggest main commands matching partial input
            return StringUtil.copyPartialMatches(args[0], MAIN_COMMANDS, new ArrayList<>());
        }

        if (args.length == 2) {
            // If the first arg is 'clear', suggest the '-droppeditems' flag
            if ("clear".equalsIgnoreCase(args[0])) {
                return StringUtil.copyPartialMatches(args[1], CLEAR_ARGS, new ArrayList<>());
            }
        }

        // No suggestions for other arguments
        return Collections.emptyList();
    }
}