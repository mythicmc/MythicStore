
package org.mythicmc.mythicstore.tempflyvouchers;

import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener
implements Listener {
    private final TempFlyVouchers tfvoucher;
    private final FileConfiguration data;

    public PlayerJoinListener(TempFlyVouchers tfvoucher) {
        this.tfvoucher = tfvoucher;
        this.data = tfvoucher.getDataYAML().getData();
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent e) {
        if (this.data.getConfigurationSection("Vouchers") == null) {
            return;
        }
        Set players = this.data.getConfigurationSection("Vouchers").getKeys(false);
        if (!players.contains(e.getPlayer().getName().toLowerCase())) {
            return;
        }
        if (this.tfvoucher.getPlugin().getConfig().getString("extras.tfcommand") == null) {
            this.tfvoucher.getPlugin().getLogger().log(Level.SEVERE, "Please fix voucher command in config.yml");
            return;
        }
        new BukkitRunnable(){
            public void run() {
                for (int amount = PlayerJoinListener.this.data.getInt("Vouchers." + e.getPlayer().getName().toLowerCase()); amount > 0; --amount) {
                    Bukkit.dispatchCommand((CommandSender)PlayerJoinListener.this.tfvoucher.getPlugin().getServer().getConsoleSender(), (String)PlayerJoinListener.this.tfvoucher.getPlugin().getConfig().getString("extras.tfcommand").replace("{Player}", e.getPlayer().getName()));
                    System.out.println("given an apple");
                }
                PlayerJoinListener.this.data.set("Vouchers." + e.getPlayer().getName().toLowerCase(), null);
                PlayerJoinListener.this.tfvoucher.getDataYAML().saveData();
                PlayerJoinListener.this.tfvoucher.getDataYAML().reloadData();
                e.getPlayer().sendMessage((Object)ChatColor.GREEN + "You have been given your vip TempFly");
            }
        }.runTaskLater((Plugin)this.tfvoucher.getPlugin(), 100L);
    }
}

