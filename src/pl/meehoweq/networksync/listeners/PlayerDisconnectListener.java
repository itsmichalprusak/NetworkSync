package pl.meehoweq.networksync.listeners;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import pl.meehoweq.networksync.NetworkSyncPlugin;

public class PlayerDisconnectListener implements Listener {

    private NetworkSyncPlugin plugin;

    public PlayerDisconnectListener(NetworkSyncPlugin plugin) {
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();

        plugin.redisManager.removePlayer(player);
    }

}
