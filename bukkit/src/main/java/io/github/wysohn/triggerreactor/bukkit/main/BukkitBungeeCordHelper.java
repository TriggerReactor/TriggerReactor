package io.github.wysohn.triggerreactor.bukkit.main;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.wysohn.triggerreactor.bukkit.scope.JavaPluginLifetime;
import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@JavaPluginLifetime
public class BukkitBungeeCordHelper extends Manager implements PluginMessageListener, Runnable {
    @Inject
    Server server;
    @Inject
    Plugin plugin;

    private final String CHANNEL = "BungeeCord";

    private final String SUB_SERVERLIST = "ServerList";
    private final String SUB_USERCOUNT = "UserCount";

    private final Map<String, Integer> playerCounts = new ConcurrentHashMap<>();

    private Thread bungeeConnectionThread;

    @Inject
    BukkitBungeeCordHelper() {
        server.getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL);
        server.getMessenger()
                .registerIncomingPluginChannel(plugin, CHANNEL, this);
    }

    private void initBungeeHelper() {
        bungeeConnectionThread = new Thread(this);
        bungeeConnectionThread.setPriority(Thread.MIN_PRIORITY);
        bungeeConnectionThread.start();
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals(CHANNEL)) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (subchannel.equals(SUB_SERVERLIST)) {
            String[] serverList = in.readUTF().split(", ");
            Set<String> serverListSet = Sets.newHashSet(serverList);

            for (String server : serverListSet) {
                if (!playerCounts.containsKey(server))
                    playerCounts.put(server, -1);
            }

            Set<String> deleteServer = new HashSet<>();
            for (Map.Entry<String, Integer> entry : playerCounts.entrySet()) {
                if (!serverListSet.contains(entry.getKey()))
                    deleteServer.add(entry.getKey());
            }

            for (String delete : deleteServer) {
                playerCounts.remove(delete);
            }
        } else if (subchannel.equals(SUB_USERCOUNT)) {
            String server = in.readUTF(); // Name of server, as given in the arguments
            int playercount = in.readInt();

            playerCounts.put(server, playercount);
        }
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            Player player = Iterables.getFirst(BukkitUtil.getOnlinePlayers(), null);
            if (player == null)
                return;

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(SUB_SERVERLIST);
            out.writeUTF("GetServers");
            player.sendPluginMessage(plugin, SUB_SERVERLIST, out.toByteArray());

            if (!playerCounts.isEmpty()) {
                for (Map.Entry<String, Integer> entry : playerCounts.entrySet()) {
                    ByteArrayDataOutput out2 = ByteStreams.newDataOutput();
                    out2.writeUTF(SUB_USERCOUNT);
                    out2.writeUTF("PlayerCount");
                    out2.writeUTF(entry.getKey());
                    player.sendPluginMessage(plugin, SUB_USERCOUNT, out2.toByteArray());
                }
            }

            try {
                Thread.sleep(5 * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public int getPlayerCount(String serverName) {
        return playerCounts.getOrDefault(serverName, -1);
    }

    public String[] getServerNames() {
        String[] servers = playerCounts.keySet().toArray(new String[playerCounts.size()]);
        return servers;
    }

    public void sendToServer(Player player, String serverName) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(serverName);

        player.sendPluginMessage(plugin, CHANNEL, out.toByteArray());
    }

    @Override
    public void onDisable() {
        bungeeConnectionThread.interrupt();
    }

    @Override
    public void onEnable() throws Exception {
        initBungeeHelper();
    }

    @Override
    public void onReload() throws RuntimeException {

    }
}
