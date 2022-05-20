/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandSender
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerJoinEvent
 */
package org.mythicmc.mythicstore.delayedcommands;

import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnPlayerJoinListener
implements Listener {
    private DelayedCommands delayedCommands;

    public OnPlayerJoinListener(DelayedCommands delayedCommands) {
        this.delayedCommands = delayedCommands;
    }

    @EventHandler
    public void onEvent(PlayerJoinEvent e) throws IOException {
        if (this.delayedCommands.getDataYAML().getData().getStringList(e.getPlayer().getName().toLowerCase()).isEmpty()) {
            return;
        }
        for (String s : this.delayedCommands.getDataYAML().getData().getStringList(e.getPlayer().getName().toLowerCase())) {
            Bukkit.dispatchCommand((CommandSender)this.delayedCommands.getPlugin().getServer().getConsoleSender(), (String)s.replace("{player}", e.getPlayer().getName()));
        }
        this.delayedCommands.getDataYAML().getData().set(e.getPlayer().getName().toLowerCase(), (Object)"");
        this.delayedCommands.getDataYAML().saveData();
    }
}

