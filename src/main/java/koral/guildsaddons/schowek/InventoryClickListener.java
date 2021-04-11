package koral.guildsaddons.schowek;

import koral.guildsaddons.GuildsAddons;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent ev) {
        if (ev.getInventory().getHolder() != null && ev.getInventory().getHolder() instanceof Schowek.Holder) {
            if (ev.getClick() == ClickType.SWAP_OFFHAND || ev.getClick() == ClickType.NUMBER_KEY || ev.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
                ev.setCancelled(true);
                return;
            }
            if (Schowek.Holder.emptySlot.isSimilar(ev.getCurrentItem()))
                ev.setCancelled(true);
            else if (ev.getRawSlot() >= 0 && ev.getRawSlot() < ev.getInventory().getSize()) {
                Bukkit.getScheduler().runTask(GuildsAddons.plugin, () -> {
                   if (ev.getInventory().getItem(ev.getRawSlot()) == null)
                       ev.getInventory().setItem(ev.getRawSlot(), Schowek.Holder.emptySlot);
                });
            }
        }
    }
}
