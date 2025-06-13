package net.scape.project.lagSuite;

import net.scape.project.lagSuite.commands.LagSuiteCommand;
import net.scape.project.lagSuite.commands.LagSuiteTabCompleter;
import net.scape.project.lagSuite.hooks.PAPI;
import net.scape.project.lagSuite.listeners.ChunkLimiterListener;
import net.scape.project.lagSuite.listeners.HaltListener;
import net.scape.project.lagSuite.managers.ClearManager;
import net.scape.project.lagSuite.managers.HaltManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class LagSuite extends JavaPlugin {

    private static LagSuite instance;
    private ClearManager clearManager;
    private HaltManager haltManager;

    private boolean haltEnabled = false;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();

        clearManager = new ClearManager();
        clearManager.scheduleItemClearTask();

        haltManager = new HaltManager();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PAPI().register();
        }

        getCommand("lagsuite").setExecutor(new LagSuiteCommand(this));
        getCommand("lagsuite").setTabCompleter(new LagSuiteTabCompleter());
        getServer().getPluginManager().registerEvents(new HaltListener(), this);
        getServer().getPluginManager().registerEvents(new ChunkLimiterListener(), this);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }

    public static LagSuite getInstance() {
        return instance;
    }

    public void reload() {
        /// reloading the config.yml
        super.reloadConfig();

        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        clearManager.clearTask();
        clearManager.scheduleItemClearTask();
    }

    public ClearManager getClearManager() {
        return clearManager;
    }

    public boolean isHaltEnabled() {
        return haltEnabled;
    }

    public void setHaltEnabled(boolean enabled) {
        this.haltEnabled = enabled;
    }

    public HaltManager getHaltManager() {
        return haltManager;
    }
}