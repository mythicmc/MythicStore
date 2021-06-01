package org.mythicmc.mythicstore.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mythicmc.mythicstore.MythicStore;
import org.mythicmc.mythicstore.util.CreativePlotUtils;

public class CreativePlotCommand implements CommandExecutor {
    private final MythicStore plugin;

    public CreativePlotCommand(MythicStore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) return false;
        Player p = Bukkit.getPlayer(args[0]);
        if (p == null || !p.isOnline()) {
            sender.sendMessage("§4That player is not online");
            return true;
        }
        CreativePlotUtils.increasePlotAmount(p, plugin);
        sender.sendMessage("§aSuccessfully gave " + p.getName() + " a creative plot");
        return true;
    }
}
