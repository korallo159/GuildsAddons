package koral.guildsaddons.listeners;

import koral.guildsaddons.commands.SetRtp;
import koral.guildsaddons.commands.rtp;
import koral.guildsaddons.simpleThings.Klatka;
import koral.guildsaddons.util.Cooldowns;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;


public class PlayerInteract implements Listener {
    Cooldowns cooldowns = new Cooldowns(new HashMap<>());
    Cooldowns bellCd = new Cooldowns(new HashMap<>());
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getClickedBlock().getType().equals(Material.STONE_BUTTON)) {

                for (String key : SetRtp.rtpConfig.getConfig().getConfigurationSection("rtps").getKeys(false)) {
                    if (SetRtp.rtpConfig.getConfig().getLocation("rtps." + key).equals(e.getClickedBlock().getLocation())) {
                        if (cooldowns.hasCooldown(e.getPlayer(), 30)) {
                            e.getPlayer().sendMessage("§4Musisz chwilę poczekać, zanim ponownie użyjesz RTP.");
                            return;
                        }
                        cooldowns.setSystemTime(e.getPlayer());
                        rtp.tp(e.getPlayer());
                    }
                }
            }
            if(e.getClickedBlock().getType().equals(Material.BELL)){
                if(Klatka.cageConfig.getConfig().getLocation("bell") == null) return;
               if(Klatka.cageConfig.getConfig().getLocation("bell").equals(e.getClickedBlock().getLocation())){
                   if(bellCd.hasCooldown(e.getPlayer(), 2, "§cOdczekaj chwile, zanim ponownie dołączysz/odejdziesz z kolejki.")) return;

                    bellCd.setSystemTime(e.getPlayer());
                    e.getPlayer().performCommand("klatka");
               }
            }
        }
    }

}
