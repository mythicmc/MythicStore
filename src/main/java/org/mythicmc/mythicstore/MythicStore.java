package org.mythicmc.mythicstore;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.mythicmc.mythicstore.command.StoreCommand;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

public class MythicStore extends JavaPlugin {
    private JedisPool pool;
    private BukkitTask task;
    private boolean loggedOnce = false;
    private final String COMMAND_QUEUE = "command-queue";

    @Override
    public void onEnable() {
        var pluginCommand = getCommand("mythicstore");
        if (pluginCommand != null)
            pluginCommand.setExecutor(new StoreCommand(this));

        try {
            saveDefaultConfig();
            reloadConfig();
            if (getConfig().getBoolean("redis.enabled")) {
                createPool();
                createTask();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createPool() {
        var user = getConfig().getString("user");
        var password = getConfig().getString("password");
        pool = new JedisPool(
                new JedisPoolConfig(),
                getConfig().getString("host", "localhost"),
                getConfig().getInt("port", 6379),
                Protocol.DEFAULT_TIMEOUT,
                user != null && user.isBlank() ? null : user,
                password != null && password.isBlank() ? null : password,
                getConfig().getInt("database", 0),
                getConfig().getBoolean("ssl", false)
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
                    if (!loggedOnce) {
                        getLogger().severe(COMMAND_QUEUE + " exists in the Redis database and it is not a list!");
                        loggedOnce = true;
                    }
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
            }
        }, 20 * 60, 20 * 60);
    }

    public void reloadPlugin() {
        reloadConfig();
        if (getConfig().getBoolean("redis.enabled")) {
            if (task != null) task.cancel();
            if (pool != null) pool.close();
            createPool();
            createTask();
        }
    }

    @Override
    public void onDisable() {
        task.cancel();
        pool.close();
    }

    public JedisPool getPool() {
        return pool;
    }

    public BukkitTask getTask() {
        return task;
    }
}
