package koral.guildsaddons.listeners;
import koral.guildsaddons.commands.SetRtp;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class PlayerInteract implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(e.getClickedBlock().getType().equals(Material.STONE_BUTTON)){
                for(String key: SetRtp.rtpConfig.getConfig().getConfigurationSection("rtps").getKeys(false)){
                   if(SetRtp.rtpConfig.getConfig().getLocation("rtps." + key).equals(e.getClickedBlock().getLocation())) {
                       List<String> lista = new ArrayList<>();
                       lista.addAll(SetRtp.rtpConfig.getConfig().getStringList("rtp"));
                       int randint = new Random().nextInt(lista.size());
                       PluginChannelListener.sendRtpMessage("RtpChannel", lista.get(randint), e.getPlayer());
                       PluginChannelListener.connectAnotherServer(lista.get(randint), e.getPlayer());
                     }
                  }
                }
            }
        }
    }
