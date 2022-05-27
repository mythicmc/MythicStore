package org.mythicmc.mythicstore.util;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.mythicmc.mythicstore.MythicStore;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.*;

public class DelayedCommandsData {

    private final MythicStore plugin;

    private final File file;
    private final FileConfiguration configuration;

    public DelayedCommandsData(MythicStore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "delayedcommands.yml");
        this.configuration = YamlConfiguration.loadConfiguration(this.file);
    }

    public void createDataYML() {
        try {
            if (!file.exists() && file.createNewFile()) {
                plugin.getLogger().info(ChatColor.GREEN + "Successfully created delayedcommands.yml");
            }
        } catch (IOException e) {
            plugin.getLogger().severe(ChatColor.RED + "Couldn't create delayedcommands.yml");
        }
    }

    public FileConfiguration getData() {
        return configuration;
    }

    public void saveData() throws IOException {
        save(this.file, this.configuration.saveToString());
    }

    public void save(File file, String data) throws IOException {
        Validate.notNull(file, "File cannot be null");
        Files.createParentDirs(file);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Writer writer = null;
            try {
                writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                writer.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            this.reloadData();
        });
    }

    public void reloadData() {
        try {
            configuration.load(file);
        } catch (IOException | InvalidConfigurationException | YAMLException e) {
            e.printStackTrace();
        }
    }
}

