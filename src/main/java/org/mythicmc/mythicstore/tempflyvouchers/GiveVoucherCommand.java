
package org.mythicmc.mythicstore.tempflyvouchers;

import org.mythicmc.mythicstore.tempflyvouchers.util.DataYAML;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveVoucherCommand
implements CommandExecutor {
    private final TempFlyVouchers tfvoucher;
    private final DataYAML data;

    public GiveVoucherCommand(TempFlyVouchers tfvoucher) {
        this.tfvoucher = tfvoucher;
        this.data = tfvoucher.getDataYAML();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int amount;
        if (!sender.hasPermission("tf.voucher.give")) {
            sender.sendMessage((Object)ChatColor.RED + "You don't have permission to execute this command");
            return false;
        }
        if (args.length < 2) {
            sender.sendMessage((Object)ChatColor.GREEN + "Usage: " + (Object)ChatColor.WHITE + "/GiveTfVoucher <player> <amount>");
            return false;
        }
        try {
            amount = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e) {
            sender.sendMessage((Object)ChatColor.RED + args[1] + " is not an integer");
            return false;
        }
        this.data.getData().set("Vouchers." + args[0].toLowerCase(), (Object)amount);
        this.data.saveData();
        this.data.reloadData();
        if (sender instanceof Player) {
            sender.sendMessage((Object)ChatColor.GREEN + "Successfully added " + args[1] + " vouchers to " + (Object)ChatColor.GOLD + args[0]);
        }
        this.tfvoucher.getPlugin().getLogger().log(Level.INFO, "Successfully added " + args[1] + " vouchers to " + args[0]);
        return false;
    }
}

