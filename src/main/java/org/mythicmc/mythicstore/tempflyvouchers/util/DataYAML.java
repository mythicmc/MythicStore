
package org.mythicmc.mythicstore.tempflyvouchers.util;

import org.mythicmc.mythicstore.tempflyvouchers.TempFlyVouchers;
import java.io.File;
import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.error.YAMLException;

public class DataYAML {
    private final File file;
    private final TempFlyVouchers tfvoucher;
    private final FileConfiguration configuration;

    public DataYAML(TempFlyVouchers tfvoucher) {
        this.tfvoucher = tfvoucher;
        this.file = new File(tfvoucher.getPlugin().getDataFolder() + System.getProperty("file.separator") + "TempFlyVouchers" + System.getProperty("file.separator") + "vouchers.yml");
        this.configuration = YamlConfiguration.loadConfiguration((File)this.file);
    }

    public void createDataYML() {
        try {
            String path = tfvoucher.getPlugin().getDataFolder() + System.getProperty("file.separator") + "TempFlyVouchers";
            if (new File(path).mkdir()) {
                this.tfvoucher.getPlugin().getLogger().info((Object)ChatColor.GREEN + "Successfully created TempFlyVouchers directory");
            }
            if (!this.file.exists() && this.file.createNewFile()) {
                this.tfvoucher.getPlugin().getLogger().info((Object)ChatColor.GREEN + "Successfully created data.yml");
            }
        }
        catch (IOException e) {
            this.tfvoucher.getPlugin().getLogger().severe((Object)ChatColor.RED + "Couldn't create data.yml");
        }
    }

    public FileConfiguration getData() {
        try {
            this.configuration.load(this.file);
        }
        catch (IOException | InvalidConfigurationException | YAMLException e) {
            e.printStackTrace();
        }
        return this.configuration;
    }

    public void saveData() {
        try {
            this.configuration.save(this.file);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
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

