package org.mythicmc.mythicstore.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mythicmc.mythicstore.MythicStore;

public class CreativePlotUtils {
    public static void runConsoleCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public static void increasePlotAmount(Player p, MythicStore plugin) {
        int maxPlots = plugin.getConfig().getInt("givecreativeplot.max-plots");
        int minPlots = plugin.getConfig().getInt("givecreativeplot.min-plots");

        for (int i = maxPlots - 1; i >= minPlots; i--) {
            if (p.hasPermission("plots.plot." + i)) {
                if (i > minPlots)
                    unsetPermission(p, "plots.plot." + i);
                setPermission(p, "plots.plot." + (i + 1));
                break;
            }
        }
    }

    public static void unsetPermission(Player p, String permission) {
        runConsoleCommand("lp user " + p.getName() + " permission unset " + permission);
    }

    public static void setPermission(Player p, String permission) {
        runConsoleCommand("lp user " + p.getName() + " permission set " + permission + " " + true);
    }
}
