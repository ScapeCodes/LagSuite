package net.scape.project.lagSuite.managers;

import net.scape.project.lagSuite.LagSuite;
import net.scape.project.lagSuite.listeners.RedstoneCuller;
import net.scape.project.lagSuite.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class RedstoneTracker {

    private static final Map<Location, Integer> activity = new HashMap<>();

    public static void record(Location loc) {
        if (!LagSuite.getInstance().getRedstoneCullerManager().isEnabled()) return;
        activity.merge(loc, 1, Integer::sum);
    }

    public static void tick() {
        RedstoneCullerManager manager = LagSuite.getInstance().getRedstoneCullerManager();

        if (!manager.isEnabled()) {
            activity.clear(); // still clear so it doesn't accumulate
            return;
        }

        int threshold = manager.getThreshold();

        for (Map.Entry<Location, Integer> entry : activity.entrySet()) {
            Location loc = entry.getKey();
            int count = entry.getValue();

            if (count > threshold) {
                RedstoneCuller.cull(loc);

                if (manager.shouldAlertOps()) {
                    alertOps(loc, count);
                }
            }
        }

        activity.clear();
    }

    private static void alertOps(Location loc, int count) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
                Utils.msgPlayerConfig(player, "redstone-culled", "%location%", formatLoc(loc), "%count%", String.valueOf(count));
            }
        }
    }

    private static String formatLoc(Location loc) {
        return loc.getWorld().getName() + " [" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + "]";
    }
}
