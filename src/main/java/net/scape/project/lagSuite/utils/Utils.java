package net.scape.project.lagSuite.utils;

import net.scape.project.lagSuite.LagSuite;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {

    private static Pattern p1 = Pattern.compile("\\{#([0-9A-Fa-f]{6})\\}");
    private static Pattern p2 = Pattern.compile("&#([A-Fa-f0-9]){6}");
    private static Pattern p3 = Pattern.compile("#([A-Fa-f0-9]){6}");
    private static Pattern p4 = Pattern.compile("<#([A-Fa-f0-9])>{6}");
    private static Pattern p5 = Pattern.compile("<#&([A-Fa-f0-9])>{6}");


    public static String format(String message) {
        if (isVersionLessThan("1.16")) {
            message = net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', message);
            return message;
        } else {
            message = net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', message);

            // Handle hex color codes
            Matcher hexMatcher = p1.matcher(message);
            while (hexMatcher.find()) {
                message = message.replace(hexMatcher.group(), net.md_5.bungee.api.ChatColor.of(hexMatcher.group().substring(1)).toString());
            }

            hexMatcher = p2.matcher(message);
            while (hexMatcher.find()) {
                message = message.replace(hexMatcher.group(), net.md_5.bungee.api.ChatColor.of(hexMatcher.group().substring(1)).toString());
            }

            Matcher[] matchers = {p3.matcher(message), p4.matcher(message), p5.matcher(message)};
            for (Matcher matcher : matchers) {
                while (matcher.find()) {
                    String hexColor = matcher.group().replaceAll("[<#&>]", "").substring(0, 6);
                    message = message.replace(matcher.group(), net.md_5.bungee.api.ChatColor.of(hexColor).toString());
                }
            }

            message = message.replace("<black>", "§0")
                    .replace("<dark_blue>", "§1")
                    .replace("<dark_green>", "§2")
                    .replace("<dark_aqua>", "§3")
                    .replace("<dark_red>", "§4")
                    .replace("<dark_purple>", "§5")
                    .replace("<gold>", "§6")
                    .replace("<gray>", "§7")
                    .replace("<dark_gray>", "§8")
                    .replace("<blue>", "§9")
                    .replace("<green>", "§a")
                    .replace("<aqua>", "§b")
                    .replace("<red>", "§c")
                    .replace("<light_purple>", "§d")
                    .replace("<yellow>", "§e")
                    .replace("<white>", "§f")
                    .replace("<obfuscated>", "§k")
                    .replace("<bold>", "§l")
                    .replace("<strikethrough>", "§m")
                    .replace("<underlined>", "§n")
                    .replace("<italic>", "§o")
                    .replace("<reset>", "§r");

            message = message.replace("$", "$");
            return message;
        }
    }

    public static boolean isValidVersion(String version) {
        return version.matches("\\d+(\\.\\d+)*"); // Matches version strings like "1", "1.2", "1.2.3", etc.
    }

    public static boolean isVersionLessThan(String version) {
        String serverVersion = Bukkit.getVersion();
        String[] serverParts = serverVersion.split(" ")[2].split("\\.");
        String[] targetParts = version.split("\\.");

        for (int i = 0; i < Math.min(serverParts.length, targetParts.length); i++) {
            if (!isValidVersion(serverParts[i]) || !isValidVersion(targetParts[i])) {
                // Handle invalid version format
                return false;
            }

            int serverPart = Integer.parseInt(serverParts[i]);
            int targetPart = Integer.parseInt(targetParts[i]);

            if (serverPart < targetPart) {
                return true;
            } else if (serverPart > targetPart) {
                return false;
            }
        }
        return serverParts.length < targetParts.length;
    }

    public static void msgPlayer(Player player, String... str) {
        for (String msg : str) {
            player.sendMessage(format(msg));
        }
    }

    public static void msgPlayer(CommandSender player, String... str) {
        for (String msg : str) {
            player.sendMessage(format(msg));
        }
    }

    public static void msgPlayerConfig(CommandSender player, String valueLang) {
        player.sendMessage(format(LagSuite.getInstance().getConfig().getString("language." + valueLang)));
    }

    public static void msgPlayerConfig(CommandSender player, String valueLang, @Nullable String r1, @Nullable String r2) {
        String lang = LagSuite.getInstance().getConfig().getString("language." + valueLang);
        lang = lang.replace(r1, r2);
        player.sendMessage(format(lang));
    }

    public static void titlePlayer(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(format(title), format(subtitle), fadeIn, stay, fadeOut);
    }

    public static void soundPlayer(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public static List<String> color(List<String> lore) {
        return lore.stream().map(Utils::format).collect(Collectors.toList());
    }
}
