
package org.mythicmc.mythicstore.delayedcommands;

import org.mythicmc.mythicstore.MythicStore;
import org.mythicmc.mythicstore.delayedcommands.util.DataYAML;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public final class DelayedCommands {

    private final MythicStore plugin;
    private DataYAML dataYAML;

    public DelayedCommands(MythicStore plugin) {
        this.plugin = plugin;
        this.dataYAML = new DataYAML(this);
        this.dataYAML.createDataYML();
    }

    public void register() {
        Objects.requireNonNull(plugin.getCommand("runonjoin")).setExecutor((CommandExecutor)new Command(this));
        plugin.getServer().getPluginManager().registerEvents((Listener)new OnPlayerJoinListener(this), (Plugin)plugin);
    }

    public DataYAML getDataYAML() {
        return this.dataYAML;
    }

    public MythicStore getPlugin() {
        return plugin;
    }
}

