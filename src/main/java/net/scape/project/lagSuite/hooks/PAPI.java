package net.scape.project.lagSuite.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.scape.project.lagSuite.LagSuite;
import org.bukkit.entity.Player;

public class PAPI extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "lagsuite";
    }

    @Override
    public String getAuthor() {
        return "Scape";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier.equals("timer")) {
            int seconds = LagSuite.getInstance().getClearManager().getSecondsUntilClear();

            if (seconds <= 0) {
                return "Clearing...";
            }

            int minutes = seconds / 60;
            int secs = seconds % 60;

            if (minutes > 0) {
                return minutes + "m " + secs + "s";
            } else {
                return secs + "s";
            }
        }

        return null;
    }
}