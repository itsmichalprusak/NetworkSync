package pl.meehoweq.networksync.listeners;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;
import pl.meehoweq.networksync.NetworkSyncPlugin;
import pl.meehoweq.networksync.events.*;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

public class RedisListener extends JedisPubSub {

    private NetworkSyncPlugin plugin;

    public RedisListener(NetworkSyncPlugin plugin) {
        this.plugin = plugin;
        plugin.redisManager.subscribe(this,
                "NSBroadcastMessage",
                "NSSendMessage",
                "NSKickPlayer",
                "NSKickIP",
                "NSPlayerPreLogin",
                "NSPlayerLogin",
                "NSPlayerServerConnect",
                "NSPlayerServerDisconnect",
                "NSPlayerDisconnect",
                "NSExecuteCommand");
    }

    private Gson gson = new Gson();

    public void onMessage(String channel, String json) {
        JsonObject object = gson.fromJson(json, JsonObject.class);

        switch (channel) {
            case "NSBroadcastMessage": {
                String instance = object.get("instance").getAsString();
                BaseComponent[] message = ComponentSerializer.parse(object.get("message").getAsString());

                if (instance.equals(plugin.redisManager.getInstance())) {
                    return;
                }

                if (object.get("permission") != null) {
                    String permission = object.get("permission").getAsString();

                    for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                        if (player.hasPermission(permission)) {
                            player.sendMessage(message);
                        }
                    }
                } else {
                    ProxyServer.getInstance().broadcast(message);
                }
                break;
            }
            case "NSSendMessage": {
                UUID uuid = UUID.fromString(object.get("uuid").getAsString());
                String message = object.get("message").getAsString();

                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);

                if (player == null) {
                    return;
                }

                player.sendMessage(message);
                break;
            }
            case "NSKickPlayer": {
                UUID uuid = UUID.fromString(object.get("uuid").getAsString());
                String reason = object.get("reason").getAsString();

                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);

                if (player == null) {
                    return;
                }

                player.disconnect(reason);
                break;
            }
            case "NSKickIP": {
                String instance = object.get("instance").getAsString();
                String ip = object.get("ip").getAsString();
                String reason = object.get("reason").getAsString();

                if (instance.equals(plugin.redisManager.getInstance())) {
                    return;
                }

                for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                    if (player.getAddress().getAddress().getHostAddress().equals(ip)) {
                        player.disconnect(reason);
                    }
                }
                break;
            }
            case "NSPlayerPreLogin": {
                UUID uuid = UUID.fromString(object.get("uuid").getAsString());
                String nick = object.get("nick").getAsString();
                String ip = object.get("ip").getAsString();
                String instance = object.get("instance").getAsString();

                if (plugin.redisManager.getInstance().equals(instance)) {
                    return;
                }

                NSPlayerPreLoginEvent event = new NSPlayerPreLoginEvent(uuid, nick, ip);
                ProxyServer.getInstance().getPluginManager().callEvent(event);
                break;
            }
            case "NSPlayerLogin": {
                UUID uuid = UUID.fromString(object.get("uuid").getAsString());
                String nick = object.get("nick").getAsString();
                String ip = object.get("ip").getAsString();
                String instance = object.get("instance").getAsString();

                if (plugin.redisManager.getInstance().equals(instance)) {
                    return;
                }

                NSPlayerLoginEvent event = new NSPlayerLoginEvent(uuid, nick, ip);
                ProxyServer.getInstance().getPluginManager().callEvent(event);
                break;
            }
            case "NSPlayerServerNS": {
                UUID uuid = UUID.fromString(object.get("uuid").getAsString());
                String nick = object.get("nick").getAsString();
                String ip = object.get("ip").getAsString();
                ServerInfo info = ProxyServer.getInstance().getServerInfo(object.get("info").getAsString());
                String instance = object.get("instance").getAsString();

                if (plugin.redisManager.getInstance().equals(instance)) {
                    return;
                }

                NSPlayerDisconnectServerEvent event = new NSPlayerDisconnectServerEvent(uuid, nick, ip, info);
                ProxyServer.getInstance().getPluginManager().callEvent(event);
                break;
            }
            case "NSPlayerServerDisNS": {
                UUID uuid = UUID.fromString(object.get("uuid").getAsString());
                String nick = object.get("nick").getAsString();
                String ip = object.get("ip").getAsString();
                ServerInfo info = ProxyServer.getInstance().getServerInfo(object.get("info").getAsString());
                String instance = object.get("instance").getAsString();

                if (plugin.redisManager.getInstance().equals(instance)) {
                    return;
                }

                NSPlayerDisconnectServerEvent event = new NSPlayerDisconnectServerEvent(uuid, nick, ip, info);
                ProxyServer.getInstance().getPluginManager().callEvent(event);
                break;
            }
            case "NSPlayerDisNS": {
                UUID uuid = UUID.fromString(object.get("uuid").getAsString());
                String nick = object.get("nick").getAsString();
                String ip = object.get("ip").getAsString();
                String instance = object.get("instance").getAsString();

                if (plugin.redisManager.getInstance().equals(instance)) {
                    return;
                }

                NSPlayerDisconnectEvent event = new NSPlayerDisconnectEvent(uuid, nick, ip);
                ProxyServer.getInstance().getPluginManager().callEvent(event);
                break;
            }
            case "NSExecuteCommand": {
                String cmd = object.get("command").getAsString();
                String instance = object.get("instance").getAsString();

                if (plugin.redisManager.getInstance().equals(instance)) {
                    return;
                }

                ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), cmd);
                break;
            }
        }
    }

    public void onSubscribe(String channel, int subscribedChannels) { }

    public void onUnsubscribe(String channel, int subscribedChannels) { }

    public void onPSubscribe(String pattern, int subscribedChannels) { }

    public void onPUnsubscribe(String pattern, int subscribedChannels) { }

    public void onPMessage(String pattern, String channel, String message) { }

}
