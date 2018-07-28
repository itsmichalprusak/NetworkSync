package pl.meehoweq.networksync.listeners;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import pl.meehoweq.networksync.NetworkSyncPlugin;

public class PreLoginListener implements Listener {

    private NetworkSyncPlugin plugin;

    public PreLoginListener(NetworkSyncPlugin plugin) {
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onPreLogin(final PreLoginEvent event) {
        event.registerIntent(plugin);

        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
            String nick = event.getConnection().getName();

            if(plugin.redisManager.getUUID(nick) != null) {
                event.setCancelled(true);
                event.setCancelReason("§cGracz o tym nicku jest juz online!");
            }

            event.completeIntent(plugin);
        });
    }

}
