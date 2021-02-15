package koral.guildsaddons.listeners;
import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.commands.SetRtp;
import koral.guildsaddons.util.Cooldowns;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


public class PlayerInteract implements Listener {

    Cooldowns cooldowns = new Cooldowns(new HashMap<>());
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(e.getClickedBlock().getType().equals(Material.STONE_BUTTON)){
                if (cooldowns.hasCooldown(e.getPlayer(), 3)) {
                    e.getPlayer().sendMessage("Nie możesz tego jeszcze zrobić");
                    return;
                }
                cooldowns.setSystemTime(e.getPlayer());
                for(String key: SetRtp.rtpConfig.getConfig().getConfigurationSection("rtps").getKeys(false)){
                   if(SetRtp.rtpConfig.getConfig().getLocation("rtps." + key).equals(e.getClickedBlock().getLocation())) {
                       List<String> lista = new ArrayList<>();

                       lista.addAll(SetRtp.rtpConfig.getConfig().getStringList("rtp"));
                       int randint = new Random().nextInt(lista.size());
                       sendRtpMessage("RtpChannel", lista.get(randint), e.getPlayer());
                       connectAnotherServer(lista.get(randint), e.getPlayer());
                     }
                  }
                }
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

    }
