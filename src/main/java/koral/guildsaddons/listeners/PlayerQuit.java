package koral.guildsaddons.listeners;

import koral.guildsaddons.commands.Sethome;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        Sethome.homeMap.remove(e.getPlayer());
    }
}
