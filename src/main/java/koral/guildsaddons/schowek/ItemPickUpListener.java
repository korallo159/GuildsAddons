package koral.guildsaddons.schowek;

import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.database.statements.PlayersStatements;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ItemPickUpListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onItemPickUp(EntityPickupItemEvent ev) {
        if (!ev.isCancelled() && ev.getEntity() instanceof Player) {
            Player p = (Player) ev.getEntity();
            Material mat = ev.getItem().getItemStack().getType();

            if (    p.getOpenInventory().getTopInventory().getHolder() != null &&
                    p.getOpenInventory().getTopInventory().getHolder() instanceof Schowek.Holder &&
                    Schowek.dataMap.containsKey(mat)) {
                ev.setCancelled(true);
                return;
            }

            int max = Schowek.getLimit(mat);
            if (max > 0) {
                int amount = 0;
                for (ItemStack item : p.getInventory())
                    if (item != null && item.getType() == mat)
                        amount += item.getAmount();

                if (amount + ev.getItem().getItemStack().getAmount() > max) {
                    int count = amount + ev.getItem().getItemStack().getAmount() - max; // do zabrania
                    ItemStack item = ev.getItem().getItemStack();
                    item.setAmount(item.getAmount() - count);
                    if (item.getAmount() <= 0) {
                        ev.setCancelled(true);
                        Bukkit.getScheduler().runTask(GuildsAddons.plugin, ev.getItem()::remove);
                    } else
                        ev.getItem().setItemStack(item);
                    Map<String, Integer> toSend = new HashMap<>();
                    toSend.put(mat.toString(), count);
                    Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.plugin, () -> PlayersStatements.updatePlayerData(p, toSend));
                }
            }
        }
    }
}
