package net.scape.project.lagSuite.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.scape.project.lagSuite.LagSuite;
import net.scape.project.lagSuite.managers.ClearManager;
import net.scape.project.lagSuite.utils.SchedulerAdapter;
import net.scape.project.lagSuite.utils.Utils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LagSuiteCommand implements CommandExecutor {

    private final LagSuite plugin;

    public LagSuiteCommand(LagSuite plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(ChatColor.YELLOW + "LagSuite Commands:");
            sender.sendMessage(ChatColor.GREEN + "/lagsuite clear" + ChatColor.GRAY + " - Clear ground items/entities.");
            sender.sendMessage(ChatColor.GREEN + "/lagsuite clear -droppeditems" + ChatColor.GRAY + " - Clear only dropped items.");
            sender.sendMessage(ChatColor.GREEN + "/lagsuite tps" + ChatColor.GRAY + " - Show server TPS");
            sender.sendMessage(ChatColor.GREEN + "/lagsuite chunks" + ChatColor.GRAY + " - Show laggy chunks");
            sender.sendMessage(ChatColor.GREEN + "/lagsuite tpchunk tpchunk <world> <x> <y> <z>" + ChatColor.GRAY + " - Teleport to a world location.");
            sender.sendMessage(ChatColor.GREEN + "/lagsuite halt" + ChatColor.GRAY + " - Halts servers activity.");
            sender.sendMessage(ChatColor.GREEN + "/lagsuite report" + ChatColor.GRAY + " - System Hardware grading/report.");
            sender.sendMessage(ChatColor.GREEN + "/lagsuite reload" + ChatColor.GRAY + " - Reload config.");
            sender.sendMessage(ChatColor.GREEN + "/lagsuite status" + ChatColor.GRAY + " - Show entity stats.");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "clear":
                List<World> worlds = Bukkit.getWorlds();
                ClearManager clearManager = LagSuite.getInstance().getClearManager();

                if (args.length > 1 && args[1].equalsIgnoreCase("-droppeditems")) {
                    clearManager.clearDroppedItems(sender, worlds);
                } else {
                    clearManager.clearEntities(sender, worlds);
                }
                return true;
            case "halt":
                boolean newState = !LagSuite.getInstance().isHaltEnabled();
                LagSuite.getInstance().setHaltEnabled(newState);

                String status = newState ? ChatColor.RED + "Halt mode ENABLED." : ChatColor.GREEN + "Halt mode DISABLED.";
                sender.sendMessage(ChatColor.YELLOW + "LagSuite: " + status);

                if (newState) {
                    LagSuite.getInstance().getHaltManager().startHaltActionBarTask();
                }
                return true;
            case "tps":
                double tps = Bukkit.getServer().getTPS()[0];
                Utils.msgPlayerConfig(sender, "tps", "{tps}", "" + tps);
                return true;

            case "chunks":
                Utils.msgPlayerConfig(sender, "largest-chunks-header");

                SchedulerAdapter.runAsync(LagSuite.getInstance(), () -> {
                    Map<Chunk, Integer> chunkEntityCounts = new HashMap<>();

                    for (World world : Bukkit.getWorlds()) {
                        for (Chunk chunk : world.getLoadedChunks()) {
                            int count = chunk.getEntities().length;
                            chunkEntityCounts.put(chunk, count);
                        }
                    }

                    List<Map.Entry<Chunk, Integer>> topChunkEntries = chunkEntityCounts.entrySet().stream()
                            .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                            .limit(5)
                            .toList();

                    SchedulerAdapter.runSync(LagSuite.getInstance(), () -> {
                        for (Map.Entry<Chunk, Integer> entry : topChunkEntries) {
                            Chunk chunk = entry.getKey();
                            int entityCount = entry.getValue();
                            World world = chunk.getWorld();
                            String worldName = world.getName();

                            int chunkX = chunk.getX();
                            int chunkZ = chunk.getZ();

                            // Center of chunk
                            int blockX = (chunkX << 4) + 8;
                            int blockZ = (chunkZ << 4) + 8;
                            int blockY = world.getHighestBlockYAt(blockX, blockZ) + 1;

                            TextComponent base = new TextComponent(ChatColor.GRAY + " - " + worldName +
                                    " [" + chunkX + ", " + chunkZ + "]: " + entityCount + " entities ");

                            TextComponent tp = new TextComponent(ChatColor.GREEN + "[TP]");
                            tp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    new ComponentBuilder("Click to teleport to this chunk").create()));
                            tp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    "/lagsuite tpchunk " + worldName + " " + blockX + " " + blockY + " " + blockZ));

                            base.addExtra(tp);

                            if (sender instanceof Player player) {
                                player.spigot().sendMessage(base);
                            } else {
                                // Console fallback (no clickable support)
                                sender.sendMessage(ChatColor.GRAY + " - " + worldName +
                                        " [" + chunkX + ", " + chunkZ + "]: " + entityCount + " entities");
                            }
                        }
                    });
                });
                return true;

            case "tpchunk":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                    return true;
                }

                if (args.length < 5) {
                    sender.sendMessage(ChatColor.RED + "Usage: /lagsuite tpchunk <world> <x> <y> <z>");
                    return true;
                }

                String worldName = args[1];
                int x, y, z;

                try {
                    x = Integer.parseInt(args[2]);
                    y = Integer.parseInt(args[3]);
                    z = Integer.parseInt(args[4]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Coordinates must be numbers.");
                    return true;
                }

                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    sender.sendMessage(ChatColor.RED + "World not found: " + worldName);
                    return true;
                }

                Location location = new Location(world, x + 0.5, y, z + 0.5); // center player on block
                player.teleport(location);
                player.sendMessage(ChatColor.GREEN + "Teleported to chunk at [" + (x >> 4) + ", " + (z >> 4) + "]");
                return true;

            case "report":
                Runtime runtime = Runtime.getRuntime();
                int mb = 1024 * 1024;

                long maxMemory = runtime.maxMemory() / mb;
                long allocatedMemory = runtime.totalMemory() / mb;
                long freeMemory = runtime.freeMemory() / mb;
                long usedMemory = allocatedMemory - freeMemory;

                com.sun.management.OperatingSystemMXBean osBean =
                        (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

                double cpuLoad = osBean.getSystemCpuLoad(); // value from 0.0 to 1.0
                int cpuPercent = (int) (cpuLoad * 100);

                String cpuGrade = cpuPercent < 30 ? "A" : cpuPercent < 60 ? "B" : cpuPercent < 85 ? "C" : "D";
                String ramGrade = usedMemory < (maxMemory * 0.5) ? "A" : usedMemory < (maxMemory * 0.75) ? "B" : "C";

                sender.sendMessage(ChatColor.YELLOW + "Server Hardware Report:");
                sender.sendMessage(ChatColor.GRAY + "  ▸ CPU Load: " + ChatColor.AQUA + cpuPercent + "%" + ChatColor.GRAY + " (Grade: " + ChatColor.GREEN + cpuGrade + ChatColor.GRAY + ")");
                sender.sendMessage(ChatColor.GRAY + "  ▸ Memory Usage: " + ChatColor.AQUA + usedMemory + "MB/" + maxMemory + "MB" + ChatColor.GRAY + " (Grade: " + ChatColor.GREEN + ramGrade + ChatColor.GRAY + ")");
                sender.sendMessage(ChatColor.GRAY + "  ▸ System Load Average: " + ChatColor.AQUA + osBean.getSystemLoadAverage());
                return true;


            case "reload":
                plugin.reload();
                Utils.msgPlayerConfig(sender, "reload");
                return true;

            case "status":
                int entities = 0;
                int chunks = 0;
                for (World w : Bukkit.getWorlds()) {
                    for (Chunk chunk : w.getLoadedChunks()) {
                        chunks++;
                        entities += chunk.getEntities().length;
                    }
                }
                sender.sendMessage(ChatColor.AQUA + "Loaded Chunks: " + chunks);
                sender.sendMessage(ChatColor.AQUA + "Total Entities: " + entities);
                return true;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Type /lagsuite for help.");
                return true;
        }
    }
}