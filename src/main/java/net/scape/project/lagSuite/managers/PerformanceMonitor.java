package net.scape.project.lagSuite.managers;

import net.scape.project.lagSuite.LagSuite;
import net.scape.project.lagSuite.utils.SchedulerAdapter;
import net.scape.project.lagSuite.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class PerformanceMonitor {

    private double threshold;
    private boolean alertOps;
    private boolean clearItems;
    private boolean clearEntities;
    private boolean disableDrops;
    private boolean disableMobSpawning;

    private boolean tpsLow = false;

    public PerformanceMonitor() {
        loadConfig();
    }

    public void loadConfig() {
        ConfigurationSection config = LagSuite.getInstance().getConfig().getConfigurationSection("performance-monitor.cull-on-low-tps");
        this.threshold = config.getDouble("tps", 10);
        this.clearItems = config.getBoolean("types.items", true);
        this.clearEntities = config.getBoolean("types.entities", true);
        this.alertOps = LagSuite.getInstance().getConfig().getBoolean("performance-monitor.alert-ops", true);
        this.disableDrops = config.getBoolean("disable-drops", true);
        this.disableMobSpawning = config.getBoolean("disable-mob-spawning", true);
    }

    public void start() {
        if (!LagSuite.getInstance().getConfig().getBoolean("performance-monitor.enable")) return;

        SchedulerAdapter.runSyncRepeating(LagSuite.getInstance(), () -> {
            double tps = Bukkit.getTPS()[0];
            boolean nowLow = tps <= threshold;

            if (nowLow && !tpsLow) {
                notifyLowTps(tps);
                autoCull();
            }

            tpsLow = nowLow;

        }, 0L, 100L); // every 5 seconds
    }

    private void notifyLowTps(double currentTps) {
        if (!alertOps) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
                Utils.msgPlayerConfig(player, "performance-tps-drop", "%tps%", "" + currentTps);
            }
        }
    }

    private void autoCull() {
        List<World> worlds = Bukkit.getWorlds();
        ClearManager clearManager = LagSuite.getInstance().getClearManager();

        if (clearItems) {
            clearManager.clearDroppedItems(Bukkit.getConsoleSender(), worlds);
        }

        if (clearEntities) {
            clearManager.clearEntities(Bukkit.getConsoleSender(), worlds);
        }
    }

    public boolean isTpsLow() {
        return tpsLow;
    }

    public boolean shouldBlockDrops() {
        return tpsLow && disableDrops;
    }

    public boolean shouldBlockMobSpawning() {
        return tpsLow && disableMobSpawning;
    }
}