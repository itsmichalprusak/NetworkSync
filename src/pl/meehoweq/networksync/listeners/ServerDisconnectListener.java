package pl.meehoweq.networksync.listeners;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import pl.meehoweq.networksync.NetworkSyncPlugin;

public class ServerDisconnectListener implements Listener {

    NetworkSyncPlugin plugin;

    public ServerDisconnectListener(NetworkSyncPlugin plugin) {
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onServerDisconnect(ServerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        ServerInfo info = event.getTarget();

        plugin.redisManager.disconnectServer(player, info);
    }

}
