package koral.guildsaddons.schowek;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractListener implements Listener {
    @EventHandler
    public void interact(PlayerInteractEvent ev) {
        if (ev.getItem() != null && Schowek.dataMap.containsKey(ev.getItem().getType()))
            InventoryCloseListener.checkEq(ev.getPlayer(), null, true);
    }
}
