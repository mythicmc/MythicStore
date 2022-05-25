package org.mythicmc.mythicstore;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.mythicmc.mythicstore.command.CreativePlotCommand;
import org.mythicmc.mythicstore.command.DelayedCommands;
import org.mythicmc.mythicstore.command.SkinControlCommand;
import org.mythicmc.mythicstore.command.StoreCommand;
import org.mythicmc.mythicstore.listener.PlayerJoinListener;
import org.mythicmc.mythicstore.util.DelayedCommandsData;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.io.File;

public class MythicStore extends JavaPlugin {
    private JedisPool pool;
    private BukkitTask task;
    private boolean loggedOnce = false;
    private final String COMMAND_QUEUE = "command-queue";

    private File skinControlFile;
    private FileConfiguration skinControlData;
    private DelayedCommandsData delayedCommandsData;

    @Override
    public void onEnable() {
        var pluginCommand = getCommand("mythicstore");
        if (pluginCommand != null)
            pluginCommand.setExecutor(new StoreCommand(this));
        pluginCommand = getCommand("givecreativeplot");
        if (pluginCommand != null)
            pluginCommand.setExecutor(new CreativePlotCommand(this));

        try {
            saveDefaultConfig();
            reloadConfig();
            if (getConfig().getBoolean("redis.enabled")) {
                createPool();
                createTask();
            }

            if (getConfig().getBoolean("skincontrol")) {
                loadSkinControl();
                pluginCommand = getCommand("skincontrol");
                if (pluginCommand != null)
                    pluginCommand.setExecutor(new SkinControlCommand(this));
            }

            if (getConfig().getBoolean("delayedcommands")) {
                loadDelayedCommands();
                pluginCommand = getCommand("runonjoin");
                if (pluginCommand != null)
                    pluginCommand.setExecutor(new DelayedCommands(this));
                getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createPool() {
        var user = getConfig().getString("redis.user");
        var password = getConfig().getString("redis.password");
        pool = new JedisPool(
                new JedisPoolConfig(),
                getConfig().getString("redis.host", "localhost"),
                getConfig().getInt("redis.port", 6379),
                Protocol.DEFAULT_TIMEOUT,
                user != null && user.isBlank() ? null : user,
                password != null && password.isBlank() ? null : password,
                getConfig().getInt("redis.database", 0),
                getConfig().getBoolean("redis.ssl", false)
        );
    }

    public void createTask() {
        // Create repeating task to read the pool.
        task = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (pool.isClosed() || task.isCancelled()) return;

            try (Jedis jedis = pool.getResource()) {
                // Check the type of command_queue.
                var type = jedis.type(COMMAND_QUEUE);
                if (type == null || type.isBlank() || type.equals("none")) return;
                else if (!type.equals("list")) {
                    getLogger().severe(COMMAND_QUEUE + " exists in the Redis database and it is not a list!");
                    loggedOnce = true;
                    return;
                }
                if (loggedOnce) {
                    getLogger().info(COMMAND_QUEUE + " is now a valid list. Reading the queue normally now.");
                    loggedOnce = false;
                }

                var command = jedis.lpop(COMMAND_QUEUE);
                while (command != null && !command.isBlank()) {
                    String finalCommand = command;
                    getServer().getScheduler().runTask(
                            this, () -> getServer().dispatchCommand(getServer().getConsoleSender(), finalCommand)
                    );
                    command = jedis.lpop(COMMAND_QUEUE);
                }
            } catch (Exception e) {
                getLogger().severe("Failed to make request to Redis.");
                e.printStackTrace();
            }
        }, 20 * 30, 20 * 30);
    }

    private void loadSkinControl() {
        skinControlFile = new File(getDataFolder(), "skincontrol.yml");
        if (!skinControlFile.exists())
            this.saveResource("skincontrol.yml", true);
        skinControlData = YamlConfiguration.loadConfiguration(skinControlFile);
    }

    private void loadDelayedCommands() {
        delayedCommandsData = new DelayedCommandsData(this);
        delayedCommandsData.createDataYML();
        delayedCommandsData.reloadData();
    }

    public void reloadPlugin() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (pool != null) {
            pool.close();
            pool = null;
        }
        reloadConfig();
        if (getConfig().getBoolean("redis.enabled")) {
            createPool();
            createTask();
        }
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
        }
        if (pool != null) {
            pool.close();
        }
    }

    public JedisPool getPool() {
        return pool;
    }

    public BukkitTask getTask() {
        return task;
    }

    public File getSkinControlFile() {
        return skinControlFile;
    }

    public FileConfiguration getSkinControlData() {
        return skinControlData;
    }

    public DelayedCommandsData getDelayedCommandsData() {
        return delayedCommandsData;
    }
}
