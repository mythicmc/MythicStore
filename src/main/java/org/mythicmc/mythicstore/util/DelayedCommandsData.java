
package org.mythicmc.mythicstore.util;

import org.mythicmc.mythicstore.MythicStore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.lang.Validate;

public class DelayedCommandsData {

    private final MythicStore plugin;

    private final File file;
    private final FileConfiguration configuration;

    public DelayedCommandsData(MythicStore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "DelayedCommands.yml");
        this.configuration = YamlConfiguration.loadConfiguration(this.file);
    }

    public void createDataYML() {
        try {
            if (!this.file.exists() && this.file.createNewFile()) {
                this.plugin.getLogger().info(ChatColor.GREEN + "Successfully created DelayedCommands.yml");
            }
        }
        catch (IOException e) {
            this.plugin.getLogger().severe(ChatColor.RED + "Couldn't create DelayedCommands.yml");
        }
    }

    public FileConfiguration getData() {
        return this.configuration;
    }

    public void saveData() throws IOException {
        this.save(this.file, this.configuration.saveToString());
    }

    public void save(File file, String data) throws IOException {
        Validate.notNull(file, "File cannot be null");
        Files.createParentDirs(file);
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            Writer writer = null;
            try {
                writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                writer.write(data);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    writer.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            this.reloadData();
        });
    }

    public void reloadData() {
        try {
            this.configuration.load(this.file);
        }
        catch (IOException | InvalidConfigurationException | YAMLException e) {
            e.printStackTrace();
        }
    }
}

