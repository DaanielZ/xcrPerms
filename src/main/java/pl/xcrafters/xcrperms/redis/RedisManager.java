package pl.xcrafters.xcrperms.redis;

import org.bukkit.Bukkit;
import pl.xcrafters.xcrperms.PermsPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisManager {

    PermsPlugin plugin;

    JedisPool pool;

    public RedisManager(PermsPlugin plugin) {
        this.plugin = plugin;
        this.pool = new JedisPool(new JedisPoolConfig(), plugin.configManager.redisHost, 6379, 10000);
    }

    public void subscribe(final JedisPubSub pubSub, final String... channels) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Jedis jedis = pool.getResource();
                try {
                    jedis.select(0);
                    jedis.subscribe(pubSub, channels);
                } catch (JedisConnectionException ex) {
                    pool.returnBrokenResource(jedis);
                } finally {
                    pool.returnResource(jedis);
                }
            }
        });
    }

}
