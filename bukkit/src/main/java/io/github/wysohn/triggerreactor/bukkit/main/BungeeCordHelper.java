package io.github.wysohn.triggerreactor.bukkit.main;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class BungeeCordHelper implements PluginMessageListener, Runnable {
    private final Plugin plugin;
    private final String CHANNEL = "BungeeCord";

    private final String SUB_SERVERLIST = "ServerList";
    private final String SUB_USERCOUNT = "UserCount";

    private final Map<String, Integer> playerCounts = new ConcurrentHashMap<>();

    /**
     * constructor should only be called from onEnable()
     */
    @Inject
    private BungeeCordHelper(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL);
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(plugin, CHANNEL, this);
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

    public void sendToServer(Player player, String serverName) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(serverName);

        player.sendPluginMessage(plugin, CHANNEL, out.toByteArray());
    }

    public String[] getServerNames() {
        String[] servers = playerCounts.keySet().toArray(new String[playerCounts.size()]);
        return servers;
    }

    public int getPlayerCount(String serverName) {
        return playerCounts.getOrDefault(serverName, -1);
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
}
