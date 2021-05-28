package org.mythicmc.rediscommandqueue;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

class RedisCommandQueue extends JavaPlugin {
    private JedisPool pool;
    private BukkitTask task;
    private boolean loggedOnce = false;
    private final String COMMAND_QUEUE = "command_queue";

    @Override
    public void onEnable() {
        var pluginCommand = getCommand("rediscommandqueue");
        if (pluginCommand != null) {
            pluginCommand.setExecutor((sender, command, label, args) -> {
                if (args.length != 1 || !args[0].equals("reload")) return false;
                if (task != null) task.cancel();
                if (pool != null) pool.close();
                createPool();
                createTask();
                sender.sendMessage("§aReloaded RedisCommandQueue. Check console for errors.");
                return true;
            });
        }

        try {
            saveDefaultConfig();
            reloadConfig();
            createPool();
            createTask();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createPool() {
        var user = getConfig().getString("user");
        var password = getConfig().getString("password");
        pool = new JedisPool(
                new JedisPoolConfig(),
                getConfig().getString("host", "localhost"),
                getConfig().getInt("port", 6739),
                Protocol.DEFAULT_TIMEOUT,
                user != null && user.isBlank() ? null : user,
                password != null && password.isBlank() ? null : password,
                getConfig().getInt("database", 0),
                getConfig().getBoolean("ssl", false)
        );
    }

    private void createTask() {
        // Create repeating task to read the pool.
        task = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (pool.isClosed() || task.isCancelled()) return;

            try (Jedis jedis = pool.getResource()) {
                // Check the type of command_queue.
                var type = jedis.type(COMMAND_QUEUE);
                if (type == null || type.isBlank()) return;
                else if (!type.equals("list")) {
                    if (!loggedOnce) {
                        getLogger().severe("command_queue exists in the Redis database and it is not a list!");
                        loggedOnce = true;
                    }
                    return;
                }
                if (loggedOnce) {
                    getLogger().info("command_queue is now a valid list. Reading the queue normally now.");
                    loggedOnce = false;
                }

                var commands = jedis.lrange(COMMAND_QUEUE, 0, -1);
                if (commands.size() == 0) return;
                for (var command : commands) {
                    jedis.lrem(COMMAND_QUEUE, 1, command);
                    getServer().getScheduler().runTask(
                            this, () -> getServer().dispatchCommand(getServer().getConsoleSender(), command)
                    );
                }
            }
        }, 20 * 60, 20 * 60);
    }

    @Override
    public void onDisable() {
        task.cancel();
        pool.close();
    }
}
