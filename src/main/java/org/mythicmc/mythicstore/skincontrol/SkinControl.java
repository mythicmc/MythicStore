package org.mythicmc.mythicstore.skincontrol;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.command.CommandExecutor;
import java.util.Objects;
import org.bukkit.configuration.file.FileConfiguration;
import java.io.File;
import org.mythicmc.mythicstore.MythicStore;

public class SkinControl {

    private final MythicStore plugin;

    private final File playerDataFile;
    private FileConfiguration playerData;

    public SkinControl(MythicStore plugin) {
        this.plugin = plugin;
        playerDataFile = new File(this.plugin.getDataFolder() + System.getProperty("file.separator") + "SkinControl", "skincontroldata.yml");
        this.reload();
    }

    public void register() {
        Objects.requireNonNull(plugin.getCommand("skincontrol")).setExecutor((CommandExecutor)new BaseCommand(this));
    }

    private void reload() {
        if (!this.playerDataFile.exists()) {
            plugin.saveResource("SkinControl" + System.getProperty("file.separator") + "skincontroldata.yml", true);
        }
        this.playerData = YamlConfiguration.loadConfiguration((File)this.playerDataFile);
        this.plugin.getLogger().info("Successfully reloaded SkinControl.");
    }

    public File getPlayerDataFile() {
        return this.playerDataFile;
    }

    public FileConfiguration getPlayerData() {
        return this.playerData;
    }

    public MythicStore getPlugin() {
        return this.plugin;
    }
}