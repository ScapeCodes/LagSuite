package net.scape.project.lagSuite.listeners;

import net.scape.project.lagSuite.LagSuite;
import net.scape.project.lagSuite.utils.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PerformanceListener implements Listener {

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (LagSuite.getInstance().getPerformanceMonitor().shouldBlockMobSpawning()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (LagSuite.getInstance().getPerformanceMonitor().shouldBlockDrops()) {
            event.setCancelled(true);
            Utils.msgPlayerConfig(event.getPlayer(), "temp-disable-item-drops");
        }
    }
}