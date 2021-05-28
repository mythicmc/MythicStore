package org.mythicmc.rediscommandqueue;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

public class RedisCommandQueueBungee extends Plugin {
    private JedisPool pool;
    private ScheduledTask task;
    private Configuration config;
    private boolean loggedOnce = false;
    private boolean isCancelled = true;
    private final String COMMAND_QUEUE = "command_queue";

    private void saveDefaultConfig() {
        if (!getDataFolder().exists() && !getDataFolder().mkdir()) {
            throw new RuntimeException("Failed to create data folder!");
        }
        var file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            try {
                Files.copy(getResourceAsStream("config.bungee.yml"), file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void reloadConfig() throws IOException {
        config = ConfigurationProvider.getProvider(YamlConfiguration.class)
                .load(new File(getDataFolder(), "config.yml"));
    }

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerCommand(this, new Command(
                "rediscommandqueuebungee", "rediscommandqueue.command", "rediscqbungee"
        ) {
            @Override
            public void execute(CommandSender sender, String[] args) {
                if (args.length != 1 || !args[0].equals("reload")) {
                    sender.sendMessage(new ComponentBuilder("Run /rediscommandqueue reload to reload the plugin.")
                            .color(ChatColor.RED).create());
                }
                try {
                    isCancelled = true;
                    if (task != null) task.cancel();
                    if (pool != null) pool.close();
                    saveDefaultConfig();
                    reloadConfig();
                    createPool();
                    createTask();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sender.sendMessage(new ComponentBuilder("Reloaded RedisCommandQueue. Check console for errors.")
                        .color(ChatColor.GREEN).create());
            }
        });

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
        var user = config.getString("user");
        var password = config.getString("password");
        pool = new JedisPool(
                new JedisPoolConfig(),
                config.getString("host", "localhost"),
                config.getInt("port", 6739),
                Protocol.DEFAULT_TIMEOUT,
                user != null && user.isBlank() ? null : user,
                password != null && password.isBlank() ? null : password,
                config.getInt("database", 0),
                config.getBoolean("ssl", false)
        );
    }

    private void createTask() {
        // Create repeating task to read the pool.
        isCancelled = false;
        task = getProxy().getScheduler().schedule(this, () -> {
            if (pool.isClosed() || isCancelled) return;

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
                    getProxy().getPluginManager().dispatchCommand(getProxy().getConsole(), command);
                }
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void onDisable() {
        isCancelled = true;
        task.cancel();
        pool.close();
    }
}
