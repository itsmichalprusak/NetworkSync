package pl.meehoweq.networksync;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import pl.meehoweq.networksync.commands.NsCmdCommand;
import pl.meehoweq.networksync.commands.NsDebugPlayerCommand;
import pl.meehoweq.networksync.listeners.*;
import pl.meehoweq.networksync.redis.RedisManager;

import java.util.Random;

public class NetworkSyncPlugin extends Plugin {

    private static NetworkSyncPlugin instance;
    private static final String CHAR_LIST = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    public ConfigManager configManager;
    public RedisManager redisManager;

    private PreLoginListener preLoginListener;
    private LoginListener loginListener;
    private PostLoginListener postLoginListener;
    private PlayerDisconnectListener playerDisconnectListener;
    private ServerConnectedListener serverConnectedListener;
    private RedisListener redisListener;
    private ProxyStopListener proxyStopListener;
    private ServerDisconnectListener serverDisconnectListener;

    private NsDebugPlayerCommand cDebugCommand;
    private NsCmdCommand cCmdCommand;

    @Override
    public void onEnable() {
        getLogger().info("NetworkSync booting up on this node...");

        this.configManager = new ConfigManager(this);
        this.redisManager = new RedisManager(this);

        this.preLoginListener = new PreLoginListener(this);
        this.loginListener = new LoginListener(this);
        this.postLoginListener = new PostLoginListener(this);
        this.playerDisconnectListener = new PlayerDisconnectListener(this);
        this.serverConnectedListener = new ServerConnectedListener(this);
        this.redisListener = new RedisListener(this);
        this.proxyStopListener = new ProxyStopListener(this);
        this.serverDisconnectListener = new ServerDisconnectListener(this);

        this.cDebugCommand = new NsDebugPlayerCommand(this);
        this.cCmdCommand = new NsCmdCommand(this);

        instance = this;
        getLogger().info("Done.");
    }

    @Override
    public void onDisable() {
        getLogger().info("NetworkSync is shutting down. The whole cache will be cleaned prior to it.");

        for(ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            redisManager.removePlayer(player);
        }
    }

    public static NetworkSyncPlugin getInstance() {
        return instance;
    }

    public String generateRandomString() {
        StringBuilder randomString = new StringBuilder();

        for(int i=0; i<8; i++) {
            int number = getRandomNumber();
            char randomChar = CHAR_LIST.charAt(number);
            randomString.append(randomChar);
        }

        return randomString.toString();
    }

    private int getRandomNumber() {
        int randomInt;

        Random randomGenerator = new Random();
        randomInt = randomGenerator.nextInt(CHAR_LIST.length());
        if (randomInt - 1 == -1) {
            return randomInt;
        } else {
            return randomInt - 1;
        }
    }

}
