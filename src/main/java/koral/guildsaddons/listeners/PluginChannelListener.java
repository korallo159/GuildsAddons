package koral.guildsaddons.listeners;

import koral.guildsaddons.GuildsAddons;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.HashMap;

public class PluginChannelListener implements PluginMessageListener {
    public static HashMap<String, String> tpaMap = new HashMap<>();
    public static HashMap<String, String> tpaTeleport = new HashMap<>();

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            String subchannel = in.readUTF();
            if (subchannel.equals("ChatChannel") && GuildsAddons.getPlugin(GuildsAddons.class).getConfig().getBoolean("spawnServer")) {
                if (Bukkit.getOnlinePlayers().isEmpty()) return;
                short length = in.readShort();
                byte[] data = new byte[length];
                in.readFully(data);
                String s = new String(data);
                Bukkit.broadcastMessage(s);
            }
            if (subchannel.equals("TpaChannel")) {
                short length = in.readShort();
                byte[] data = new byte[length];
                in.readFully(data);
                String s = new String(data);
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(s);
                if (jsonObject.containsKey("accept")) {
                    boolean accept = (boolean) jsonObject.get("accept");
                    String server = (String) jsonObject.get("server");
                    if (Bukkit.getPlayer(jsonObject.get("target").toString()) != null)
                        if (accept) {
                            Bukkit.getPlayer(jsonObject.get("target").toString()).sendMessage(ChatColor.GREEN + "Prośba o teleportację zaakceptowana. Nie ruszaj się przez 10 sekund");
                            //TODO: 10 sekund oczekiwania, a jak sie ruszy to canceluje.
                            sendPlayerTpInfo("TpaChannel", server, player, jsonObject.get("player").toString());
                            connectAnotherServer(server, player);
                        } else
                            Bukkit.getPlayer(jsonObject.get("target").toString()).sendMessage("ChatColor.RED" + "Prośba o teleportacje odrzucona.");
                    return;
                }
                if(jsonObject.containsKey("accepted")){
                   String tpaAccepter = (String) jsonObject.get("player"); // greymen15 w przypadku wysylania
                   String tpaReceiver = (String) jsonObject.get("target"); // korallo
                tpaTeleport.put(tpaAccepter, tpaReceiver);
                Bukkit.getScheduler().runTaskLater(GuildsAddons.getPlugin(), () -> tpaTeleport.remove(tpaAccepter), 100); // gdyby jakimś cudem gracza nie przeniosło od razu
                }
                String tpaSender = (String) jsonObject.get("player");
                String tpaReceiver = (String) jsonObject.get("target");
                if (Bukkit.getPlayer(tpaReceiver) != null) {
                    tpaMap.put(tpaReceiver, tpaSender);
                    Bukkit.getScheduler().runTaskLater(GuildsAddons.getPlugin(), () -> tpaMap.remove(tpaReceiver), 20 * 25);
                    Bukkit.getPlayer(tpaReceiver).sendMessage(ChatColor.RED + "Dostałeś prośbę o teleportację od gracza " + tpaSender);
                }

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

    public static void sendTpaRequest(String subchannel, String server, Player player, String target) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteArrayOutputStream);
            out.writeUTF("Forward");
            out.writeUTF(server);
            out.writeUTF(subchannel);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("player", player.getName());
            jsonObject.put("target", target);
            String s = jsonObject.toJSONString();
            byte[] data = s.getBytes();
            out.writeShort(data.length);
            out.write(data);
            sendPluginMessage(player, byteArrayOutputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendTpaAcceptation(String subchannel, String server, Player player, String target, boolean accept) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteArrayOutputStream);
            out.writeUTF("Forward");
            out.writeUTF(server);
            out.writeUTF(subchannel);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("player", player.getName());
            jsonObject.put("target", target);
            jsonObject.put("accept", accept);
            jsonObject.put("server", GuildsAddons.getPlugin().getConfig().getString("name"));
            String s = jsonObject.toJSONString();
            byte[] data = s.getBytes();
            out.writeShort(data.length);
            out.write(data);
            sendPluginMessage(player, byteArrayOutputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendPlayerTpInfo(String subchannel, String server, Player player, String target ) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            out.writeUTF("Forward");
            out.writeUTF(server);
            out.writeUTF(subchannel); // "customchannel" for example

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("player", player.getName());
            jsonObject.put("target", target);
            jsonObject.put("accepted", true);
            String s = jsonObject.toJSONString();
            byte[] data = s.getBytes();
            out.writeShort(data.length);
            out.write(data);

            sendPluginMessage(player, b.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void askForPlayers(String server){

    }
    public static void getServers(Player player){
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("GetServers");
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        sendPluginMessage(player, b.toByteArray());
    }


}
