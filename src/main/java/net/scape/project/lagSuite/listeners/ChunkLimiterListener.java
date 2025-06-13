package net.scape.project.lagSuite.listeners;

import net.scape.project.lagSuite.LagSuite;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class ChunkLimiterListener implements Listener {

    private final boolean isFolia = isFoliaEnvironment();

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        FileConfiguration config = LagSuite.getInstance().getConfig();
        Block block = e.getBlock();
        Chunk chunk = block.getChunk();
        Material type = block.getType();

        if (config.get("chunk-limiter.blocks." + type.name()) == null) return;
        if (config.getBoolean("chunk-limiter.bypass-permission") && e.getPlayer().hasPermission("lagsuite.bypass.chunklimits")) return;

        int maxAllowed = config.getInt("chunk-limiter.blocks." + type.name());

        Runnable task = () -> {
            int count = 0;
            World world = block.getWorld();
            int minX = chunk.getX() << 4;
            int minZ = chunk.getZ() << 4;
            int maxY = world.getMaxHeight();

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y <= maxY; y++) {
                    for (int z = 0; z < 16; z++) {
                        Material mat = chunk.getBlock(x, y, z).getType();
                        if (mat == type) count++;
                    }
                }
            }

            if (count >= maxAllowed) {
                Bukkit.getScheduler().runTask(LagSuite.getInstance(), () -> {
                    e.getPlayer().sendMessage(ChatColor.RED + "Too many " + type.name() + " blocks in this chunk!");
                    e.setCancelled(true); // Only works if still in same tick
                });
            }
        };

        if (isFolia) {
            try {
                Object scheduler = Bukkit.class.getMethod("getRegionScheduler").invoke(null);
                Method execute = scheduler.getClass().getMethod("execute", Plugin.class, World.class, int.class, int.class, Consumer.class);

                execute.invoke(
                        scheduler,
                        LagSuite.getInstance(),
                        chunk.getWorld(),
                        chunk.getX(),
                        chunk.getZ(),
                        (Consumer<Object>) (scheduledTask) -> task.run()
                );
            } catch (Exception ex) {
                ex.printStackTrace();
                Bukkit.getScheduler().runTaskAsynchronously(LagSuite.getInstance(), task);
            }
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(LagSuite.getInstance(), task);
        }
    }

    private boolean isFoliaEnvironment() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionScheduler");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}