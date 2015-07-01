package services;

import com.typesafe.plugin.RedisPlugin;
import models.Sweat;
import play.api.Play;
import play.libs.Json;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;

@Singleton
public class SweatDao {

    private static final String SWEATS = "sweats";

    //@Inject just temporary hardcoded because injection is not working
    JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");

    public void save(Sweat sweat) {
        Jedis jedis = jedisPool.getResource();
        try {
            jedis.publish((SWEATS), Json.stringify(Json.toJson(sweat)));
        } finally {
            jedisPool.returnResource(jedis);
        }
    }
}
