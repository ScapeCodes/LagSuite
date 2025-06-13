package net.scape.project.lagSuite.managers;

import net.scape.project.lagSuite.LagSuite;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class HaltManager {

    public void startHaltActionBarTask() {
        Bukkit.getScheduler().runTaskTimer(LagSuite.getInstance(), () -> {
            if (LagSuite.getInstance().isHaltEnabled()) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.isOp()) {
                        player.spigot().sendMessage(
                                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                                new net.md_5.bungee.api.chat.TextComponent(ChatColor.RED + "" + ChatColor.BOLD + "⚠ Halting is ENABLED!")
                        );
                    }
                }
            }
        }, 0L, 100L); // Every 5 seconds (100 ticks)
    }
}
