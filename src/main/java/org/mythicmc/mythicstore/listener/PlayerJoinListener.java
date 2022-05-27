package org.mythicmc.mythicstore.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.mythicmc.mythicstore.MythicStore;

import java.io.IOException;

public class PlayerJoinListener implements Listener {
    private final MythicStore plugin;

    public PlayerJoinListener(MythicStore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEvent(PlayerJoinEvent e) throws IOException {
        if (plugin.getDelayedCommandsData().getData().getStringList(e.getPlayer().getName().toLowerCase()).isEmpty()) {
            return;
        }
        for (String s : plugin.getDelayedCommandsData().getData().getStringList(e.getPlayer().getName().toLowerCase())) {
            Bukkit.dispatchCommand(plugin.getServer().getConsoleSender(), s.replace("{player}", e.getPlayer().getName()));
        }
        plugin.getDelayedCommandsData().getData().set(e.getPlayer().getName().toLowerCase(), null);
        plugin.getDelayedCommandsData().saveData();
    }
}