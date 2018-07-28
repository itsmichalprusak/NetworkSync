package pl.meehoweq.networksync.commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import pl.meehoweq.networksync.NetworkSyncPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class NsCmdCommand extends Command {

    private NetworkSyncPlugin plugin;

    public NsCmdCommand(NetworkSyncPlugin plugin) {
        super("nscommand", "networksync.command", "nscmd", "globalcmd", "globalcommand");
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerCommand(plugin, this);
    }

    private Gson gson = new Gson();

    @Override
    public void execute(final CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage("§cPoprawne uzycie: §7/nscommand <§6komenda bez slasha§7>");
            return;
        }

        StringBuilder command = new StringBuilder(args[0]);
        for (int i = 1; i < args.length; i++) {
            command.append(" ").append(args[i]);
        }

        final JsonObject object = new JsonObject();
        object.addProperty("command", command.toString());
        object.addProperty("instance", plugin.redisManager.getInstance());

        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
            Jedis jedis = null;

            try {
                jedis = plugin.redisManager.getPool().getResource();

                jedis.publish("NSExecuteCommand", gson.toJson(object));

                sender.sendMessage("§aKomenda §7/" + object.get("command").getAsString() + " §azostala wywolana na wszystkich instancjach proxy.");
            } catch (JedisConnectionException exception) {
                if(jedis != null) {
                    plugin.redisManager.getPool().returnBrokenResource(jedis);
                }
            } finally {
                plugin.redisManager.getPool().returnResource(jedis);
            }
        });

        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command.toString());
    }

}
