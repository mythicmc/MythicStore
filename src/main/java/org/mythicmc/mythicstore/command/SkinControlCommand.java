package org.mythicmc.mythicstore.command;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.mythicmc.mythicstore.MythicStore;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

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

        if (args[0].equalsIgnoreCase("upgrade")) handleUpgrade(slicedArgs, sender);
        else if (args[0].equalsIgnoreCase("expire")) handleExpire(slicedArgs, sender);
        else sender.sendMessage("Invalid command. Valid commands: 'upgrade', 'expire'.");

        return true;
    }

    private void handleUpgrade(String[] args, CommandSender sender) {
        if (args.length <= 1) {
            sender.sendMessage("Invalid usage for 'upgrade'.");
            return;
        }

        checkUserPermission(args[0]).thenAcceptAsync(hasPerm -> {
            if (hasPerm) return;

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                int daysLeft = plugin.getSkinControlData().getInt(args[0]);

                // If it's 0, then it doesn't exist.
                if (daysLeft + Integer.parseInt(args[1]) < 30) {
                    plugin.getSkinControlData().set(args[0], daysLeft + Integer.parseInt(args[1]));
                } else {
                    plugin.getSkinControlData().set(args[0], null);
                    plugin.getServer().dispatchCommand(sender, "lp user " + args[0] + " parent add ltvip");
                }
                String data = plugin.getSkinControlData().saveToString();
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    try (Writer writer = new OutputStreamWriter(new FileOutputStream(plugin.getSkinControlFile()))) {
                        writer.write(data);
                    } catch (IOException e) {
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

        checkUserPermission(args[0]).thenAcceptAsync(res -> {
            if (!res) {
                plugin.getServer().getScheduler().runTask(plugin, () ->
                        plugin.getServer().dispatchCommand(sender, "rc bungee skin clear " + args[0]));

                File skinFile = new File("../bungeecord/plugins/SkinsRestorer/Players",
                        args[0].toLowerCase() + ".player");

                if (!skinFile.delete())
                    plugin.getLogger().warning("Failed to delete " + skinFile.getAbsolutePath() + ".");

                plugin.getLogger().info("Expired " + args[0] + "'s skin.");
            } else plugin.getLogger().info("Skipped expiring " + args[0] + "'s skin as they have permission.");
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
                            .checkPermission("mythicstore.purchasedskin")
                            .asBoolean());
        } else {
            return CompletableFuture.completedFuture(data.getCachedData()
                    .getPermissionData()
                    .checkPermission("mythicstore.purchasedskin")
                    .asBoolean());
        }
    }

}
