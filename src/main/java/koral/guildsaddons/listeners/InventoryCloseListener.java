package koral.guildsaddons.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryCloseListener implements Listener {
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent ev) {
        ev.getPlayer().removeScoreboardTag(InventoryClickListener.scBlockTag);
    }
}
