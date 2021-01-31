package koral.guildsaddons.listeners;

import koral.guildsaddons.GuildsAddons;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;

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
            if(subchannel.equals("TpaChannel")){

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendPluginMessage(Player player, byte[] data) {
        player.sendPluginMessage(GuildsAddons.getPlugin(GuildsAddons.class), "BungeeCord", data);
    }

    public static void sendRtpMessage(String subchannel, String target, Player player) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            out.writeUTF("Forward");
            out.writeUTF(target);
            out.writeUTF(subchannel); // "customchannel" for example

            String s = player.getName();
            byte[] data = s.getBytes();
            out.writeShort(data.length);
            out.write(data);

            sendPluginMessage(player, b.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void connectAnotherServer(String server, Player player) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteArrayOutputStream);

        try {
            out.writeUTF("Connect");
            out.writeUTF(server);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendPluginMessage(player, byteArrayOutputStream.toByteArray());
    }
    public static void sendTpaRequest(){

    }
}
