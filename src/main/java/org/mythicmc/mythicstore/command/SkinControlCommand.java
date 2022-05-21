
package org.mythicmc.mythicstore.command;

import org.mythicmc.mythicstore.MythicStore;

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

public class SkinControlCommand implements CommandExecutor {
    private final MythicStore plugin;

    public SkinControlCommand(MythicStore plugin) {
        this.plugin = plugin;
    }

    @Override
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
            this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                int daysLeft = this.plugin.getSkinControlConfiguration().getInt(args[0]);
                if (daysLeft + Integer.parseInt(args[1]) < 30) {
                    this.plugin.getSkinControlConfiguration().set(args[0], daysLeft + Integer.parseInt(args[1]));
                } else {
                    this.plugin.getSkinControlConfiguration().set(args[0], null);
                    this.plugin.getServer().dispatchCommand(sender, "lp user " + args[0] + " parent add ltvip");
                }
                String data = this.plugin.getSkinControlConfiguration().saveToString();
                this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                    try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(this.plugin.getSkinControlFile()))){
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
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.plugin.getServer().dispatchCommand(sender, "rc bungee skin clear " + args[0]));
                File skinFile = new File("../bungeecord/plugins/SkinsRestorer/Players", args[0].toLowerCase() + ".player");
                if (!skinFile.delete()) {
                    this.plugin.getLogger().warning("Failed to delete " + skinFile.getAbsolutePath() + ".");
                }
                this.plugin.getLogger().info("Expired " + args[0] + "'s skin.");
            } else {
                this.plugin.getLogger().info("Skipped expiring " + args[0] + "'s skin as they have permission.");
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
