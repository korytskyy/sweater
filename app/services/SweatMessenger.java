package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.Sweat;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.Comet;
import play.libs.F;
import play.libs.Json;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class SweatMessenger {

    private static final String SWEATS = "sweats";

    private final JedisPool jedisPool;

    static Set<Comet> comets = new ConcurrentHashMap<Comet, Boolean>().newKeySet();

    ObjectMapper mapper = new ObjectMapper();

    @Inject
    public SweatMessenger(ApplicationLifecycle lifecycle) {
        Logger.debug("SweatMessenger construcotr");
        jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");

        Logger.debug("SweatMessenger subscribing..");


        new Thread(() -> jedisPool.getResource().subscribe(new MyListener(), SWEATS)).start();

        Logger.debug("SweatMessenger subscribed");

        lifecycle.addStopHook(() -> {
            Logger.debug("SweatMessenger stopHook");
            jedisPool.destroy();
            return F.Promise.pure(null);
        });
    }

    public void save(Sweat sweat) {
        Logger.debug("SweatMessenger save");
        try (Jedis jedis = jedisPool.getResource();) {
            jedis.publish(SWEATS, Json.stringify(Json.toJson(sweat)));
        }
    }

    public static void registerChunkOut(Comet out) {
        Logger.debug("SweatMessenger register");
        comets.add(out);
        out.onDisconnected(() -> {
            Logger.debug("SweatMessenger register onDisconnected");
            comets.remove(out);
        });
    }

    class MyListener extends JedisPubSub {
        public void onMessage(String channel, String message) {
            Logger.debug("SweatMessenger MyListener onMessage");
            comets.forEach(c -> c.sendMessage(message));
        }

        public void onSubscribe(String channel, int subscribedChannels) {
        }

        public void onUnsubscribe(String channel, int subscribedChannels) {
        }

        public void onPSubscribe(String pattern, int subscribedChannels) {
        }

        public void onPUnsubscribe(String pattern, int subscribedChannels) {
        }

        public void onPMessage(String pattern, String channel,
                               String message) {
        }
    }

}