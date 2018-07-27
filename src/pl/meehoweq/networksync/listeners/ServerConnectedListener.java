package pl.meehoweq.networksync.listeners;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import pl.meehoweq.networksync.NetworkSyncPlugin;

public class ServerConnectedListener implements Listener {

    NetworkSyncPlugin plugin;

    public ServerConnectedListener(NetworkSyncPlugin plugin) {
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerConnected(ServerConnectedEvent event) {
        ProxiedPlayer player = event.getPlayer();

        plugin.redisManager.connectServer(player, event.getServer().getInfo());
    }

}
