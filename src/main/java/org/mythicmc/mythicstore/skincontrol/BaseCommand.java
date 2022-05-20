
package org.mythicmc.mythicstore.skincontrol;

import org.bukkit.plugin.Plugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

public class BaseCommand implements CommandExecutor {
    private final SkinControl skincontrol;

    public BaseCommand(SkinControl skinControl) {
        this.skincontrol = skinControl;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Invalid usage.");
            return true;
        }
        String[] slicedArgs = Arrays.copyOfRange(args, 1, args.length);
        if (args[0].equalsIgnoreCase("upgrade")) {
            this.handleUpgrade(slicedArgs, sender);
        } else if (args[0].equalsIgnoreCase("expire")) {
            this.handleExpire(slicedArgs, sender);
        } else {
            sender.sendMessage("Invalid command. Valid commands: 'upgrade', 'expire'.");
        }
        return true;
    }

    private void handleUpgrade(String[] args, CommandSender sender) {
        if (args.length <= 1) {
            sender.sendMessage("Invalid usage for 'upgrade'.");
            return;
        }
        this.checkUserPermission(args[0]).thenAcceptAsync(hasPerm -> {
            if (hasPerm) {
                return;
            }
            this.skincontrol.getPlugin().getServer().getScheduler().runTask((Plugin)this.skincontrol.getPlugin(), () -> {
                int daysLeft = this.skincontrol.getPlayerData().getInt(args[0]);
                if (daysLeft + Integer.parseInt(args[1]) < 30) {
                    this.skincontrol.getPlayerData().set(args[0], (Object)(daysLeft + Integer.parseInt(args[1])));
                } else {
                    this.skincontrol.getPlayerData().set(args[0], null);
                    this.skincontrol.getPlugin().getServer().dispatchCommand(sender, "lp user " + args[0] + " parent add ltvip");
                }
                String data = this.skincontrol.getPlayerData().saveToString();
                this.skincontrol.getPlugin().getServer().getScheduler().runTaskAsynchronously((Plugin)this.skincontrol.getPlugin(), () -> {
                    try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(this.skincontrol.getPlayerDataFile()));){
                        writer.write(data);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            });
        });
    }

    private void handleExpire(String[] args, CommandSender sender) {
        if (args.length == 0) {
            sender.sendMessage("Invalid usage for 'expire'.");
            return;
        }
        this.checkUserPermission(args[0]).thenAcceptAsync(res -> {
            if (!res) {
                this.skincontrol.getPlugin().getServer().getScheduler().runTask((Plugin)this.skincontrol.getPlugin(), () -> this.skincontrol.getPlugin().getServer().dispatchCommand(sender, "rc bungee skin clear " + args[0]));
                File skinFile = new File("../bungeecord/plugins/SkinsRestorer/Players", args[0].toLowerCase() + ".player");
                if (!skinFile.delete()) {
                    this.skincontrol.getPlugin().getLogger().warning("Failed to delete " + skinFile.getAbsolutePath() + ".");
                }
                this.skincontrol.getPlugin().getLogger().info("Expired " + args[0] + "'s skin.");
            } else {
                this.skincontrol.getPlugin().getLogger().info("Skipped expiring " + args[0] + "'s skin as they have permission.");
            }
        });
    }

    private CompletableFuture<Boolean> checkUserPermission(String name) {
        User data = LuckPermsProvider.get().getUserManager().getUser(name);
        if (data == null) {
            return LuckPermsProvider.get()
                    .getUserManager()
                    .lookupUniqueId(name)
                    .thenComposeAsync((uuid) -> LuckPermsProvider.get().getUserManager().loadUser(uuid))
                    .thenApplyAsync((user) -> user.getCachedData()
                            .getPermissionData()
                            .checkPermission("skincontrol.purchased")
                            .asBoolean());
        } else {
            return CompletableFuture.completedFuture(data.getCachedData()
                    .getPermissionData()
                    .checkPermission("skincontrol.purchased")
                    .asBoolean());
        }
    }

}
