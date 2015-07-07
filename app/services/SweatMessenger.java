package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.Sweat;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.Comet;
import play.libs.F;
import play.libs.Json;
import play.mvc.WebSocket;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
// todo consider using actor here
public class SweatMessenger {

    private static final String SWEATS = "sweats";

    private final JedisPool jedisPool;

    private AtomicLong outCounter = new AtomicLong();

    static Set<Comet> comets = new ConcurrentHashMap<Comet, Boolean>().newKeySet();
    static Map<WebSocket.Out, Long> websocketOuts = new ConcurrentHashMap<>();

    ObjectMapper mapper = new ObjectMapper();

    @Inject
    public SweatMessenger(ApplicationLifecycle lifecycle) {
        Logger.trace("SweatMessenger constructor");
        jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");

        new Thread(() -> {
            Logger.trace("SweatMessenger subscribing..");
            jedisPool.getResource().subscribe(new MyListener(), SWEATS);
            Logger.trace("SweatMessenger subscribed");
        }).start();

        lifecycle.addStopHook(() -> {
            Logger.trace("SweatMessenger stopHook");
            jedisPool.destroy();
            return F.Promise.pure(null);
        });
        Logger.trace("SweatMessenger constructor end");
    }


    public void save(Sweat sweat) {
        Logger.trace("SweatMessenger save");
        try (Jedis jedis = jedisPool.getResource();) {
            jedis.publish(SWEATS, Json.stringify(Json.toJson(sweat)));
        }
    }

    public void subscribe(Comet out) {
        comets.add(out);
        out.onDisconnected(()->unsubscribe(out));
    }

    public void unsubscribe(Comet out) {
        comets.remove(out);
    }

    public void subscribe(WebSocket.Out out) {
        long id = outCounter.incrementAndGet();
        Logger.trace("SweatMessenger subscribe ws.out " + id);
        websocketOuts.put(out, id);
    }

    public void unsubscribe(WebSocket.Out out) {
        websocketOuts.remove(out);
    }

    class MyListener extends JedisPubSub {
        public MyListener() {
            Logger.trace("MyListener constructor");
        }

        public void onMessage(String channel, String message) {
            Logger.trace("SweatMessenger MyListener onMessage " + message);
            comets.forEach(c -> c.sendMessage(message));
            Logger.trace("Comets notified");
            websocketOuts.forEach((out, id) -> {
                Logger.trace("Writing message to out " + id);
                out.write(message);
            });
            Logger.trace("WS notified");
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
