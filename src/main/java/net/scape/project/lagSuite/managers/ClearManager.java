package net.scape.project.lagSuite.managers;

import net.scape.project.lagSuite.LagSuite;
import net.scape.project.lagSuite.utils.SchedulerAdapter;
import net.scape.project.lagSuite.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.scape.project.lagSuite.utils.Utils.msgPlayer;

public class ClearManager {

    private int secondsUntilClear = -1;
    private int taskId = -1; // Store the task ID for cancellation

    public void clearEntities(@Nullable CommandSender player, List<World> worlds) {
        FileConfiguration config = LagSuite.getInstance().getConfig();
        List<String> exemptNames = config.getStringList("clear-exempt.entities");
        boolean named = config.getBoolean("clear-exempt.named");

        int count = 0;

        for (World world : worlds) {
            for (Entity entity : world.getEntities()) {
                String entityType = entity.getType().name();
                if (exemptNames.contains(entityType)) continue;
                //if (entity instanceof LivingEntity) continue;
                if (entity.getType() == EntityType.PLAYER) continue;
                if (named && entity.getCustomName() != null) continue;

                entity.remove();
                count++;
            }
        }

        if (player != null) {
            Utils.msgPlayerConfig(player, "cleared", "{amount}", "" + count);
        }
    }

    public void clearDroppedItems(@Nullable CommandSender player, List<World> worlds) {
        FileConfiguration config = LagSuite.getInstance().getConfig();
        List<String> exemptItems = config.getStringList("clear-exempt.dropped-items");
        boolean clearArrows = config.getBoolean("clear-exempt.clear-arrows-on-ground", false);

        int count = 0;

        for (World world : worlds) {
            for (Entity entity : world.getEntities()) {
                // Handle dropped items
                if (entity instanceof Item item) {
                    Material material = item.getItemStack().getType();
                    if (exemptItems.contains(material.name())) continue;

                    item.remove();
                    count++;
                }
                else if (clearArrows && entity instanceof org.bukkit.entity.Arrow arrow) {
                    if (arrow.isInBlock()) {
                        arrow.remove();
                        count++;
                    }
                }
            }
        }

        if (player != null) {
            Utils.msgPlayerConfig(player, "cleared-items", "{amount}", "" + count);
        }
    }

    public void scheduleItemClearTask() {
        FileConfiguration config = LagSuite.getInstance().getConfig();
        int timer = config.getInt("clear-interval.timer", 300);
        String broadcastMsg = config.getString("clear-interval.broadcast", "&a&lLagSuite &8&l> &aCleared items/entities successfully.");
        List<String> intervalList = config.getStringList("clear-interval.intervals");

        // Clear types
        boolean clearItems = config.getBoolean("clear-interval.types.items", true);
        boolean clearEntities = config.getBoolean("clear-interval.types.entities", true);

        // Exemptions
        List<String> exemptItems = config.getStringList("clear-exempt.dropped-items");
        List<String> exemptEntities = config.getStringList("clear-exempt.entities");
        boolean clearArrows = config.getBoolean("clear-exempt.clear-arrows-on-ground", false);
        boolean named = config.getBoolean("clear-exempt.named", false);

        boolean disable_interval_message_console = config.getBoolean("clear-exempt.disable-interval-message-console");

        Map<Integer, String> intervalMessages = new HashMap<>();
        for (String entry : intervalList) {
            try {
                String[] parts = entry.split(":", 3);
                int secondsBefore = Integer.parseInt(parts[0]);
                if (parts.length == 3 && parts[1].equalsIgnoreCase("msg")) {
                    intervalMessages.put(secondsBefore, parts[2]);
                }
            } catch (Exception e) {
                Bukkit.getLogger().warning("Invalid interval entry in config: " + entry);
            }
        }

        secondsUntilClear = timer;

        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(LagSuite.getInstance(), () -> {

            if (intervalMessages.containsKey(secondsUntilClear)) {
                if (!disable_interval_message_console) {
                    Bukkit.broadcastMessage(Utils.format(intervalMessages.get(secondsUntilClear)));
                } else {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        msgPlayer(player, intervalMessages.get(secondsUntilClear));
                    }
                }
            }

            if (secondsUntilClear <= 0) {
                List<Entity> toClear = new ArrayList<>();

                for (World world : Bukkit.getWorlds()) {
                    for (Entity entity : world.getEntities()) {

                        if (clearItems && entity instanceof Item item) {
                            Material material = item.getItemStack().getType();
                            if (!exemptItems.contains(material.name())) {
                                toClear.add(entity);
                            }
                        }

                        if (clearItems && clearArrows && entity instanceof org.bukkit.entity.Arrow arrow && arrow.isInBlock()) {
                            toClear.add(entity);
                        }

                        if (clearEntities) {
                            String entityType = entity.getType().name();
                            if (entity.getType() != EntityType.PLAYER &&
                                    !exemptEntities.contains(entityType) &&
                                    !(named && entity.getCustomName() != null)) {
                                toClear.add(entity);
                            }
                        }
                    }
                }

                SchedulerAdapter.runAsync(LagSuite.getInstance(), () -> {
                    int count = 0;
                    for (Entity entity : toClear) {
                        if (!entity.isDead() && entity.isValid()) {
                            SchedulerAdapter.runSync(LagSuite.getInstance(), entity::remove);
                            count++;
                        }
                    }

                    String finalMessage = broadcastMsg.replace("{amount}", String.valueOf(count));

                    SchedulerAdapter.runSync(LagSuite.getInstance(), () ->
                            Bukkit.broadcastMessage(Utils.format(finalMessage))
                    );
                });

                secondsUntilClear = timer;

            } else {
                secondsUntilClear--;
            }

        }, 0L, 20L); // every second
    }

    public void clearTask() {
        secondsUntilClear = -1;
        Bukkit.getServer().getScheduler().cancelTask(taskId);
    }

    public int getSecondsUntilClear() {
        return secondsUntilClear;
    }
}
