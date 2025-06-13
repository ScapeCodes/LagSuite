package net.scape.project.lagSuite.listeners;

import net.scape.project.lagSuite.LagSuite;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

public class HaltListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (LagSuite.getInstance().isHaltEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (LagSuite.getInstance().isHaltEnabled()) {
            Entity entity = event.getEntity();

            // Cancel dropped items
            if (entity instanceof Item) {
                event.setCancelled(true);
                return;
            }

            // Cancel projectiles (optional)
            if (entity instanceof Projectile) {
                event.setCancelled(true);
                return;
            }

            // Cancel mobs
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                event.setCancelled(true);
            }
        }
    }
}