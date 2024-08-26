package org.mythicmc.mythicstore.command;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.node.Node;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mythicmc.mythicstore.MythicStore;

public class CreativePlotCommand implements CommandExecutor {
    private final MythicStore plugin;

    public CreativePlotCommand(MythicStore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) return false;
        Player p = Bukkit.getPlayerExact(args[0]);
        if (p == null || !p.isOnline()) {
            sender.sendMessage("§4That player is not online");
            return true;
        }

        int maxPlots = plugin.getConfig().getInt("givecreativeplot.max-plots");
        int minPlots = plugin.getConfig().getInt("givecreativeplot.min-plots");

        var lp = LuckPermsProvider.get();
        var user = lp.getPlayerAdapter(Player.class).getUser(p);
        var context = ImmutableContextSet.builder()
                .add("server", "creative")
                .add("world", "creative")
                .build();
        // Find all nodes which match this context
        var nodes = user.resolveInheritedNodes(QueryOptions.contextual(context));

        for (int i = maxPlots - 1; i >= minPlots; i--) {
            int finalI = i;
            var node = nodes.stream()
                    .filter(n -> n.getKey().equalsIgnoreCase("plots.plot." + finalI))
                    .findFirst();
            if (node.isPresent()) {
                if (i > minPlots) {
                    user.data().remove(node.get());
                }
                user.data().add(Node.builder("plots.plot." + (i + 1)).context(context).build());
                lp.getUserManager().saveUser(user).thenAcceptAsync(success -> sender
                        .sendMessage("§aSuccessfully gave " + p.getName() + " a creative plot"));
                return true;
            }
        }

        sender.sendMessage("§4That player already has the maximum number of plots");
        return true;
    }
}
