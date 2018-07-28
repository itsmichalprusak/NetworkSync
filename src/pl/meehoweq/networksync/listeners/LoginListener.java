package pl.meehoweq.networksync.listeners;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import pl.meehoweq.networksync.NetworkSyncPlugin;

import java.util.UUID;

public class LoginListener implements Listener {

    private NetworkSyncPlugin plugin;

    public LoginListener(NetworkSyncPlugin plugin) {
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(final LoginEvent event) {
        if(event.isCancelled()) {
            return;
        }

        final UUID uuid = event.getConnection().getUniqueId();
        final String nick = event.getConnection().getName();
        final String ip = event.getConnection().getAddress().getAddress().getHostAddress();

        event.registerIntent(plugin);

        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
            plugin.redisManager.preLoginPlayer(uuid, nick, ip);

            event.completeIntent(plugin);
        });
    }

}
