package org.mythicmc.mythicstore.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.mythicmc.mythicstore.MythicStore;

public class StoreCommand implements CommandExecutor {
    private final MythicStore plugin;

    public StoreCommand(MythicStore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1 || !args[0].equals("reload")) return false;
        plugin.reloadPlugin();
        sender.sendMessage("§aReloaded MythicStore. Check console for errors.");
        return true;
    }
}
