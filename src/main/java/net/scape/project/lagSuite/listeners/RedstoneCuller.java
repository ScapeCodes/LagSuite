package net.scape.project.lagSuite.listeners;

import net.scape.project.lagSuite.managers.RedstoneTracker;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Powerable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

public class RedstoneCuller implements Listener {

    @EventHandler
    public void onRedstoneChange(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        RedstoneTracker.record(loc);
    }

    public static void cull(Location loc) {
        Block block = loc.getBlock();
        if (block.getBlockData() instanceof Powerable powerable) {
            powerable.setPowered(false);
            block.setBlockData(powerable);
            block.getWorld().createExplosion(loc, 0F);
        }
    }
}