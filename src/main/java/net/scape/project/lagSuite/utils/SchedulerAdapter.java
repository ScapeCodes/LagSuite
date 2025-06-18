package net.scape.project.lagSuite.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.scheduler.BukkitTask;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public class SchedulerAdapter {

    private static final boolean IS_FOLIA;

    static {
        IS_FOLIA = Bukkit.getServer().getVersion().contains("Folia");
    }

    public static void runAsync(JavaPlugin plugin, Runnable task) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    public static void runSync(JavaPlugin plugin, Runnable task) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public static void runSyncLater(JavaPlugin plugin, Runnable task, long delayTicks) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    public static Object runSyncRepeating(JavaPlugin plugin, Runnable task, long initialDelay, long period) {
        if (IS_FOLIA) {
            return Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), initialDelay, period);
        } else {
            return Bukkit.getScheduler().runTaskTimer(plugin, task, initialDelay, period);
        }
    }

    public static void cancelTask(Object task) {
        if (task == null) return;

        if (IS_FOLIA && task instanceof ScheduledTask foliaTask) {
            foliaTask.cancel();
        } else if (task instanceof BukkitTask bukkitTask) {
            bukkitTask.cancel();
        }
    }
}
