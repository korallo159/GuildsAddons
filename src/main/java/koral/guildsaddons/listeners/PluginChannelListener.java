package koral.guildsaddons.listeners;

import koral.guildsaddons.GuildsAddons;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public class PluginChannelListener implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        try{
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            String subchannel = in.readUTF();
            if(subchannel.equals("ChatChannel") && GuildsAddons.getPlugin(GuildsAddons.class).getConfig().getBoolean("spawnServer")){
                if(Bukkit.getOnlinePlayers().isEmpty()) return;
                short length = in.readShort();
                byte[] data = new byte[length];
                in.readFully(data);
                String s = new String(data);
                Bukkit.broadcastMessage(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
