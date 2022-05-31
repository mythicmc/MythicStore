package org.mythicmc.mythicstore.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mythicmc.mythicstore.MythicStore;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

public class RunOnJoinCommand implements CommandExecutor {
    private final MythicStore plugin;

    public RunOnJoinCommand(MythicStore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            sender.sendMessage(ChatColor.RED + "This is only console command");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.GREEN + "Usage: " + ChatColor.WHITE + "/roj <player> <command>");
            return true;
        }
        if (!args[0].matches("^\\w{3,16}$")) {
            plugin.getLogger().log(Level.SEVERE, "Incorrect player name was given \"" + args[0] + "\"");
            return true;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int x = 1; x < args.length; ++x) {
            stringBuilder.append(args[x]).append(" ");
        }
        List<String> list = plugin.getDelayedCommandsData().getData().getStringList(args[0].toLowerCase());
        list.add(stringBuilder.toString());
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (!player.getName().equalsIgnoreCase(args[0])) continue;
            for (String s : list) {
                Bukkit.dispatchCommand(plugin.getServer().getConsoleSender(), s.replace("{player}", args[0]));
            }
            return true;
        }
        plugin.getDelayedCommandsData().getData().set(args[0].toLowerCase(), list);
        try {
            plugin.getDelayedCommandsData().saveData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}

