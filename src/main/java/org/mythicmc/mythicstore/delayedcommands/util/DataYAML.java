/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Charsets
 *  com.google.common.io.Files
 *  org.apache.commons.lang.Validate
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.configuration.InvalidConfigurationException
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.plugin.Plugin
 *  org.yaml.snakeyaml.error.YAMLException
 */
package org.mythicmc.mythicstore.delayedcommands.util;

import org.mythicmc.mythicstore.delayedcommands.DelayedCommands;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.error.YAMLException;

public class DataYAML {
    private final File file;
    private final DelayedCommands delayedCommands;
    private FileConfiguration configuration;
    private boolean save;

    public DataYAML(DelayedCommands delayedCommands) {
        this.delayedCommands = delayedCommands;
        this.file = new File(delayedCommands.getPlugin().getDataFolder() + System.getProperty("file.separator") + "DelayedCommands" + System.getProperty("file.separator") + "DelayedCommands.yml");
        this.configuration = YamlConfiguration.loadConfiguration((File)this.file);
    }

    public void createDataYML() {
        try {
            String path = delayedCommands.getPlugin().getDataFolder() + System.getProperty("file.separator") + "DelayedCommands";
            if (new File(path).mkdir()) {
                this.delayedCommands.getPlugin().getLogger().info((Object)ChatColor.GREEN + "Successfully created DelayedCommands directory");
            }
            if (!this.file.exists() && this.file.createNewFile()) {
                this.delayedCommands.getPlugin().getLogger().info((Object)ChatColor.GREEN + "Successfully created DelayedCommands.yml");
            }
        }
        catch (IOException e) {
            this.delayedCommands.getPlugin().getLogger().severe((Object)ChatColor.RED + "Couldn't create DelayedCommands.yml");
        }
    }

    public FileConfiguration getData() {
        return this.configuration;
    }

    public void saveData() throws IOException {
        this.save(this.file, this.configuration.saveToString());
    }

    public void save(File file, String data) throws IOException {
        Validate.notNull((Object)file, (String)"File cannot be null");
        Files.createParentDirs((File)file);
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.delayedCommands.getPlugin(), () -> {
            Writer writer = null;
            try {
                writer = new OutputStreamWriter((OutputStream)new FileOutputStream(file), Charsets.UTF_8);
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

