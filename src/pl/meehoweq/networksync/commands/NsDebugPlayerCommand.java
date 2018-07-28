package pl.meehoweq.networksync.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import pl.meehoweq.networksync.NetworkSyncPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class NsDebugPlayerCommand extends Command {

    private NetworkSyncPlugin plugin;

    public NsDebugPlayerCommand(NetworkSyncPlugin plugin) {
        super("nsdebugplayer", "networksync.debug", "nsdebug", "debugplayer");
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerCommand(plugin, this);
    }

    public void execute(final CommandSender sender, String[] args) {
        if(args.length != 1) {
            sender.sendMessage("§cPoprawne uzycie: §7/nsdebugplayer <§6nick§7>");
            return;
        }

        final String nick = args[0];

        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
            Jedis jedis = null;

            try {
                jedis = plugin.redisManager.getPool().getResource();

                jedis.srem("ns_nicks", nick);
                jedis.hdel("ns_player_uuids", nick.toLowerCase());
                jedis.hdel("ns_player_nicks", nick.toLowerCase());

                sender.sendMessage("§aPomyslnie wyczyszczono powiazania sieciowe dla §7" + nick + "§a. Gracz powinien moc sie juz polaczyc.");
            } catch (JedisConnectionException exception) {
                if(jedis != null) {
                    plugin.redisManager.getPool().returnBrokenResource(jedis);
                }
            } finally {
                plugin.redisManager.getPool().returnResource(jedis);
            }
        });
    }

}
