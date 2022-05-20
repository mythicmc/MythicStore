
package org.mythicmc.mythicstore.delayedcommands;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Command
implements CommandExecutor {
    private DelayedCommands delayedCommands;

    public Command(DelayedCommands delayedCommands) {
        this.delayedCommands = delayedCommands;
    }

    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (sender instanceof Player) {
            sender.sendMessage((Object)ChatColor.RED + "This is only console command");
            return false;
        }
        if (args.length < 2) {
            sender.sendMessage((Object)ChatColor.GREEN + "Usage: " + (Object)ChatColor.WHITE + "/roj <player> <command>");
            return false;
        }
        if (!args[0].matches("^\\w{3,16}$")) {
            this.delayedCommands.getPlugin().getLogger().log(Level.SEVERE, "Incorrect player name was given \"" + args[0] + "\"");
            return false;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int x = 1; x < args.length; ++x) {
            stringBuilder.append(args[x]).append(" ");
        }
        List<String> list = this.delayedCommands.getDataYAML().getData().getStringList(args[0].toLowerCase());
        list.add(stringBuilder.toString());
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (!player.getName().equalsIgnoreCase(args[0])) continue;
            for (String s : list) {
                Bukkit.dispatchCommand((CommandSender)this.delayedCommands.getPlugin().getServer().getConsoleSender(), (String)s.replace("{player}", args[0]));
            }
            return false;
        }
        this.delayedCommands.getDataYAML().getData().set(args[0].toLowerCase(), (Object)list);
        try {
            this.delayedCommands.getDataYAML().saveData();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}

