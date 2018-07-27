package pl.meehoweq.networksync.redis;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import pl.meehoweq.networksync.NetworkSyncPlugin;
import pl.meehoweq.networksync.events.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The main Redis management class.
 * Dispatched events are prefixed with 'NS', which is an actual abbreviation of 'NetworkSync'.
 * Please have this naming convention in mind, as all the messaging made via Redis will strictly comply with it.
 *
 * @author Michał Prusak
 */
public class RedisManager {

    private NetworkSyncPlugin plugin;

    public RedisManager(NetworkSyncPlugin plugin) {
        this.plugin = plugin;

        pool = new JedisPool(plugin.configManager.redisHost, 6379);
        subscriber = pool.getResource();

        this.instance = plugin.generateRandomString();
    }

    private JedisPool pool;
    private Jedis subscriber;

    public JedisPool getPool() {
        return this.pool;
    }

    private String instance;

    /**
     * Run an asynchronous threaded channel subscription.
     * @param pubSub Jedis Pub/Sub.
     * @param channels channels to subscribe to.
     */
    public void subscribe(final JedisPubSub pubSub, final String... channels) {
        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> subscriber.subscribe(pubSub, channels));
    }

    public String getInstance() {
        return instance;
    }

    private Gson gson = new Gson();

    public void preLoginPlayer(UUID uuid, String nick, String ip) {
        JsonObject object = new JsonObject();
        object.addProperty("uuid", uuid.toString());
        object.addProperty("nick", nick);
        object.addProperty("ip", ip);
        object.addProperty("instance", instance);

        Jedis jedis = null;

        try {
            jedis = pool.getResource();

            jedis.publish("NSPlayerPreLogin", gson.toJson(object));
        } catch (JedisConnectionException ex) {
            if(jedis != null) {
                pool.returnBrokenResource(jedis);
            }
        } finally {
            pool.returnResource(jedis);
        }

        NSPlayerPreLoginEvent preLoginEvent = new NSPlayerPreLoginEvent(uuid, nick, ip);
        ProxyServer.getInstance().getPluginManager().callEvent(preLoginEvent);
    }

    public void addPlayer(ProxiedPlayer player) {
        UUID uuid = player.getUniqueId();
        String nick = player.getName();
        String ip = player.getAddress().getAddress().getHostAddress();

        JsonObject object = new JsonObject();
        object.addProperty("uuid", uuid.toString());
        object.addProperty("nick", nick);
        object.addProperty("ip", ip);
        object.addProperty("instance", instance);

        Jedis jedis = null;

        try {
            jedis = pool.getResource();

            jedis.sadd("ns_players", uuid.toString());
            jedis.sadd("ns_nicks", nick);
            jedis.hset("ns_player_uuids", nick.toLowerCase(), uuid.toString());
            jedis.hset("ns_player_nicks", nick.toLowerCase(), nick);
            jedis.hset("ns_player_ips", uuid.toString(), ip);

            jedis.publish("NSPlayerLogin", gson.toJson(object));
        } catch (JedisConnectionException ex) {
            if(jedis != null) {
                pool.returnBrokenResource(jedis);
            }
        } finally {
            pool.returnResource(jedis);
        }

        NSPlayerLoginEvent event = new NSPlayerLoginEvent(uuid, nick, ip);
        ProxyServer.getInstance().getPluginManager().callEvent(event);
    }

    public void connectServer(ProxiedPlayer player, ServerInfo info) {
        UUID uuid = player.getUniqueId();
        String nick = player.getName();
        String ip = player.getAddress().getAddress().getHostAddress();

        JsonObject object = new JsonObject();
        object.addProperty("uuid", uuid.toString());
        object.addProperty("nick", nick);
        object.addProperty("ip", ip);
        object.addProperty("info", info.getName());
        object.addProperty("instance", instance);

        Jedis jedis = null;

        try {
            jedis = pool.getResource();

            jedis.hset("ns_player_servers", uuid.toString(), info.getName());

            jedis.publish("NSPlayerServerConnect", gson.toJson(object));
        } catch (JedisConnectionException ex) {
            if(jedis != null) {
                pool.returnBrokenResource(jedis);
            }
        } finally {
            pool.returnResource(jedis);
        }

        NSPlayerConnectServerEvent event = new NSPlayerConnectServerEvent(uuid, nick, ip, info);
        ProxyServer.getInstance().getPluginManager().callEvent(event);
    }

    public void disconnectServer(ProxiedPlayer player, ServerInfo info) {
        UUID uuid = player.getUniqueId();
        String nick = player.getName();
        String ip = player.getAddress().getAddress().getHostAddress();

        JsonObject object = new JsonObject();
        object.addProperty("uuid", uuid.toString());
        object.addProperty("nick", nick);
        object.addProperty("ip", ip);
        object.addProperty("info", info.getName());
        object.addProperty("instance", instance);

        Jedis jedis = null;

        try {
            jedis = pool.getResource();

            String serverName = jedis.hget("ns_player_servers", uuid.toString());

            if(serverName != null && serverName.equals(info.getName())) {
                jedis.hdel("ns_player_servers", uuid.toString());
            }

            jedis.publish("NSPlayerServerDisconnect", gson.toJson(object));
        } catch (JedisConnectionException ex) {
            if(jedis != null) {
                pool.returnBrokenResource(jedis);
            }
        } finally {
            pool.returnResource(jedis);
        }

        NSPlayerDisconnectServerEvent event = new NSPlayerDisconnectServerEvent(uuid, nick, ip, info);
        ProxyServer.getInstance().getPluginManager().callEvent(event);
    }

    public void removePlayer(ProxiedPlayer player) {
        UUID uuid = player.getUniqueId();
        String nick = player.getName();
        String ip = player.getAddress().getAddress().getHostAddress();

        JsonObject object = new JsonObject();
        object.addProperty("uuid", uuid.toString());
        object.addProperty("nick", nick);
        object.addProperty("ip", ip);
        object.addProperty("instance", instance);

        Jedis jedis = null;

        try {
            jedis = pool.getResource();

            jedis.srem("ns_players", uuid.toString());
            jedis.srem("ns_nicks", nick);
            jedis.hdel("ns_player_uuids", nick.toLowerCase());
            jedis.hdel("ns_player_nicks", nick.toLowerCase());
            jedis.hdel("ns_player_ips", uuid.toString());
            jedis.hdel("ns_player_servers", uuid.toString());

            jedis.publish("NSPlayerDisconnect", gson.toJson(object));
        } catch (JedisConnectionException ex) {
            if(jedis != null) {
                pool.returnBrokenResource(jedis);
            }
        } finally {
            pool.returnResource(jedis);
        }

        NSPlayerDisconnectEvent event = new NSPlayerDisconnectEvent(uuid, nick, ip);
        ProxyServer.getInstance().getPluginManager().callEvent(event);
    }

    public List<String> getNicks() {
        List<String> nicks = new ArrayList<>();

        Jedis jedis = null;

        try {
            jedis = pool.getResource();

            nicks.addAll(jedis.smembers("ns_nicks"));
        } catch (JedisConnectionException ex) {
            if(jedis != null) {
                pool.returnBrokenResource(jedis);
            }
        } finally {
            pool.returnResource(jedis);
        }

        return nicks;
    }

    public boolean isOnline(UUID uuid) {
        if(uuid == null) {
            return false;
        }

        boolean online = false;

        Jedis jedis = null;

        try {
            jedis = pool.getResource();

            online = jedis.sismember("ns_players", uuid.toString());
        } catch (JedisConnectionException ex) {
            if(jedis != null) {
                pool.returnBrokenResource(jedis);
            }
        } finally {
            pool.returnResource(jedis);
        }

        return online;
    }

    public boolean isOnline(String nick) {
        return getUUID(nick) != null;
    }

    public String getExactNick(String nick) {
        if(nick == null) {
            return null;
        }

        String exactNick = null;

        Jedis jedis = null;

        try {
            jedis = pool.getResource();

            exactNick = jedis.hget("ns_player_nicks", nick.toLowerCase());
        } catch (JedisConnectionException ex) {
            if(jedis != null) {
                pool.returnBrokenResource(jedis);
            }
        } finally {
            pool.returnResource(jedis);
        }

        return exactNick;
    }

    public String getIP(UUID uuid) {
        if(uuid == null) {
            return null;
        }

        String ip = null;

        Jedis jedis = null;

        try {
            jedis = pool.getResource();

            ip = jedis.hget("ns_player_ips", uuid.toString());
        } catch (JedisConnectionException ex) {
            if(jedis != null) {
                pool.returnBrokenResource(jedis);
            }
        } finally {
            pool.returnResource(jedis);
        }

        return ip;
    }

    public String getIP(String nick) {
        return getIP(getUUID(nick));
    }

    public UUID getUUID(String nick) {
        if(nick == null) {
            return null;
        }

        UUID uuid = null;

        Jedis jedis = null;

        try {
            jedis = pool.getResource();

            String uuidString = jedis.hget("ns_player_uuids", nick.toLowerCase());

            if(uuidString != null) {
                uuid = UUID.fromString(uuidString);
            }
        } catch (JedisConnectionException ex) {
            if(jedis != null) {
                pool.returnBrokenResource(jedis);
            }
        } finally {
            pool.returnResource(jedis);
        }

        return uuid;
    }

    public ServerInfo getServer(UUID uuid) {
        if(uuid == null) {
            return null;
        }

        ServerInfo server = null;

        Jedis jedis = null;

        try {
            jedis = pool.getResource();

            String serverName = jedis.hget("ns_player_servers", uuid.toString());

            if(serverName != null) {
                server = ProxyServer.getInstance().getServerInfo(serverName);
            }
        } catch (JedisConnectionException ex) {
            if(jedis != null) {
                pool.returnBrokenResource(jedis);
            }
        } finally {
            pool.returnResource(jedis);
        }

        return server;
    }

    public ServerInfo getServer(String nick) {
        return getServer(getUUID(nick));
    }

    public void broadcastMessage(String message, String permission) {
        Jedis jedis = null;

        try {
            jedis = pool.getResource();

            JsonObject object = new JsonObject();
            object.addProperty("instance", instance);
            object.addProperty("message", message);

            if(permission != null) {
                object.addProperty("permission", permission);
            }

            jedis.publish("NSBroadcastMessage", gson.toJson(object));
        } catch (JedisConnectionException ex) {
            if(jedis != null) {
                pool.returnBrokenResource(jedis);
            }
        } finally {
            pool.returnResource(jedis);
        }
    }

    public void sendMessage(UUID uuid, String message) {
        if(uuid == null) {
            return;
        }

        Jedis jedis = null;

        try {
            jedis = pool.getResource();

            JsonObject object = new JsonObject();
            object.addProperty("uuid", uuid.toString());
            object.addProperty("message", message);

            jedis.publish("NSSendMessage", gson.toJson(object));
        } catch (JedisConnectionException ex) {
            if(jedis != null) {
                pool.returnBrokenResource(jedis);
            }
        } finally {
            pool.returnResource(jedis);
        }
    }

    public void sendMessage(String nick, String message) {
        sendMessage(getUUID(nick), message);
    }

    public void kickPlayer(UUID uuid, String reason) {
        Jedis jedis = null;

        try {
            jedis = pool.getResource();

            JsonObject object = new JsonObject();
            object.addProperty("uuid", uuid.toString());
            object.addProperty("reason", reason);

            jedis.publish("NSKickPlayer", gson.toJson(object));
        } catch (JedisConnectionException ex) {
            if(jedis != null) {
                pool.returnBrokenResource(jedis);
            }
        } finally {
            pool.returnResource(jedis);
        }
    }

    public void kickPlayer(String nick, String reason) {
        kickPlayer(getUUID(nick), reason);
    }

    public void kickIP(String ip, String reason) {
        Jedis jedis = null;

        try {
            jedis = pool.getResource();

            JsonObject object = new JsonObject();
            object.addProperty("instance", instance);
            object.addProperty("ip", ip);
            object.addProperty("reason", reason);

            jedis.publish("NSKickPlayer", gson.toJson(object));
        } catch (JedisConnectionException ex) {
            if(jedis != null) {
                pool.returnBrokenResource(jedis);
            }
        } finally {
            pool.returnResource(jedis);
        }
    }

}
