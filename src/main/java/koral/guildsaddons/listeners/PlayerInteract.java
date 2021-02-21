package koral.guildsaddons.listeners;

import koral.guildsaddons.commands.SetRtp;
import koral.guildsaddons.commands.rtp;
import koral.guildsaddons.util.Cooldowns;
import koral.sectorserver.SectorServer;
import koral.sectorserver.util.Teleport;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Random;
import java.util.function.UnaryOperator;


public class PlayerInteract implements Listener {
    Cooldowns cooldowns = new Cooldowns(new HashMap<>());

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getClickedBlock().getType().equals(Material.STONE_BUTTON)) {
                if (cooldowns.hasCooldown(e.getPlayer(), 60)) {
                    e.getPlayer().sendMessage("Nie możesz tego jeszcze zrobić");
                    return;
                }
                cooldowns.setSystemTime(e.getPlayer());
                for (String key : SetRtp.rtpConfig.getConfig().getConfigurationSection("rtps").getKeys(false)) {
                    if (SetRtp.rtpConfig.getConfig().getLocation("rtps." + key).equals(e.getClickedBlock().getLocation())) {
                        rtp.tp(e.getPlayer());
                    }
                }
            }
        }
    }
}
