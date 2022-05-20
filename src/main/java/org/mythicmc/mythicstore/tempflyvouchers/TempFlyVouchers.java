
package org.mythicmc.mythicstore.tempflyvouchers;

import org.mythicmc.mythicstore.MythicStore;
import org.mythicmc.mythicstore.tempflyvouchers.util.DataYAML;
import java.util.Objects;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public final class TempFlyVouchers {

    private final MythicStore plugin;
    private DataYAML dataYAML;

    public TempFlyVouchers(MythicStore plugin) {
        this.plugin = plugin;
        this.dataYAML = new DataYAML(this);
        this.dataYAML.createDataYML();
    }

    public void register() {
        Objects.requireNonNull(plugin.getCommand("givetfvoucher")).setExecutor((CommandExecutor)new GiveVoucherCommand(this));
        plugin.getServer().getPluginManager().registerEvents((Listener)new PlayerJoinListener(this), (Plugin)plugin);
    }

    public DataYAML getDataYAML() {
        return this.dataYAML;
    }

    public MythicStore getPlugin() {
        return plugin;
    }
}

