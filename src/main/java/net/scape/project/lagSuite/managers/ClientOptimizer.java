package net.scape.project.lagSuite.managers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import net.scape.project.lagSuite.LagSuite;
import net.scape.project.lagSuite.utils.SchedulerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClientOptimizer implements Listener {

    private final ProtocolManager protocolManager;
    private boolean enabled;
    private boolean suppressParticles;
    private boolean suppressFireworks;
    private int maxEntitiesVisible;
    private boolean smoothTeleports;

    public ClientOptimizer() {
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        loadConfig();
        if (enabled) {
            registerPacketListeners();
            Bukkit.getPluginManager().registerEvents(this, LagSuite.getInstance());
            startEntityHider();
        }
    }

    private void loadConfig() {
        FileConfiguration config = LagSuite.getInstance().getConfig();
        enabled = config.getBoolean("client-optimizer.enabled", true);
        suppressParticles = config.getBoolean("client-optimizer.suppress-particles", true);
        suppressFireworks = config.getBoolean("client-optimizer.suppress-fireworks", true);
        maxEntitiesVisible = config.getInt("client-optimizer.max-entities-visible", 50);
        smoothTeleports = config.getBoolean("client-optimizer.smooth-teleports", true);
    }

    private void registerPacketListeners() {
        if (suppressParticles) {
            protocolManager.addPacketListener(new PacketAdapter(LagSuite.getInstance(), PacketType.Play.Server.WORLD_PARTICLES) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    event.setCancelled(true);
                }
            });
        }

        if (suppressFireworks) {
            protocolManager.addPacketListener(new PacketAdapter(LagSuite.getInstance(), PacketType.Play.Server.SPAWN_ENTITY) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    if (event.getPacket().getEntityTypeModifier().read(0).name().equalsIgnoreCase("FIREWORK")) {
                        event.setCancelled(true);
                    }
                }
            });
        }
    }

    private void startEntityHider() {
        SchedulerAdapter.runSyncRepeating(LagSuite.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                List<Entity> nearby = player.getNearbyEntities(128, 128, 128);
                int count = 0;
                for (Entity entity : nearby) {
                    if (count >= maxEntitiesVisible) {
                        player.hideEntity(LagSuite.getInstance(), entity);
                    } else {
                        player.showEntity(LagSuite.getInstance(), entity);
                        count++;
                    }
                }
            }
        }, 20L, 100L); // initial delay 1 second, repeats every 5 seconds
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!smoothTeleports) return;

        Player player = event.getPlayer();
        player.setInvulnerable(true);

        SchedulerAdapter.runSyncLater(LagSuite.getInstance(), () -> player.setInvulnerable(false), 40L);
    }
}
