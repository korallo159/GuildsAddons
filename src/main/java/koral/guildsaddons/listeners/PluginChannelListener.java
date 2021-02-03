package koral.guildsaddons.listeners;

import koral.guildsaddons.GuildsAddons;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
                            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 11*20, 0, false, false, false));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 11*20, 2, false, false, false));
                            tpaTimer(server, player, jsonObject, player.getLocation(), 10);
                        } else
                            Bukkit.getPlayer(jsonObject.get("target").toString()).sendMessage("§2[§aTPA§2] §c" + "Prośba o teleportacje odrzucona.");
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
                    tpaMap.put(tpaReceiver.toLowerCase(), tpaSender);
                    Bukkit.getScheduler().runTaskLater(GuildsAddons.getPlugin(), () -> tpaMap.remove(tpaReceiver), 20 * 25);
                    Bukkit.getPlayer(tpaReceiver).sendMessage("§2[§aTPA§2] " + "§cDostałeś prośbę o teleportację od gracza " + tpaSender +
                            " aby zaakceptować wpisz /tpaccept");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tpaTimer(String server, Player player, JSONObject jsonObject, Location lastLocation, int secondsLeft) {
        if (player.getLocation().distance(lastLocation) > 1) {
            player.sendMessage("§cPoruszyłeś się! Teleportacja anulowana.");
            player.removePotionEffect(PotionEffectType.CONFUSION);
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        } else if (secondsLeft <= 0) {
            sendPlayerTpInfo("TpaChannel", server, player, jsonObject.get("player").toString());
            connectAnotherServer(server, player);
        } else {
            player.sendTitle("§5§lTeleportacja", "§9Nie ruszaj się, za §a " + secondsLeft + "s §9 zostaniesz przeteleportowany", 0, 22, 5);
            Bukkit.getScheduler().runTaskLater(GuildsAddons.plugin, () -> tpaTimer(server, player, jsonObject, lastLocation,secondsLeft - 1), 20);
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
