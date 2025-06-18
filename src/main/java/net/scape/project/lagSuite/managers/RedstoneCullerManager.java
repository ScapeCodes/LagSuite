package net.scape.project.lagSuite.managers;

import net.scape.project.lagSuite.LagSuite;
import net.scape.project.lagSuite.utils.SchedulerAdapter;
import org.bukkit.configuration.file.FileConfiguration;

public class RedstoneCullerManager {

    private boolean enabled;
    private int threshold;
    private boolean alertOps;
    private int tickInterval;

    public RedstoneCullerManager() {
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = LagSuite.getInstance().getConfig();

        enabled = config.getBoolean("redstone-culler.enabled", true);
        threshold = config.getInt("redstone-culler.update-threshold", 20);
        alertOps = config.getBoolean("redstone-culler.alert-ops", true);
        this.tickInterval = config.getInt("redstone-culler.tick-interval", 20);
    }

    public void scheduleTickingTask() {
        if (!enabled) return;

        SchedulerAdapter.runSyncRepeating(
                LagSuite.getInstance(),
                RedstoneTracker::tick,
                tickInterval, // initial delay
                tickInterval  // repeat interval
        );
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getThreshold() {
        return threshold;
    }

    public boolean shouldAlertOps() {
        return alertOps;
    }

    public void setEnabled(boolean value) {
        this.enabled = value;
    }
}