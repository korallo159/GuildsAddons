package koral.guildsaddons.schowek;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InventoryClickListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent ev) {
        if (ev.getInventory().getHolder() != null && ev.getInventory().getHolder() instanceof Schowek.Holder) {
            ev.setCancelled(true);
            if (ev.getRawSlot() < 0 || ev.getRawSlot() >= ev.getInventory().getSize() || Schowek.Holder.emptySlot.isSimilar(ev.getCurrentItem()))
                return;

            Material mat = ev.getCurrentItem().getType();

            AtomicInteger akt = new AtomicInteger(0);
            ev.getWhoClicked().getInventory().all(mat).forEach((slot, item) -> akt.set(akt.get() + item.getAmount()));

            int limit = Schowek.dataMap.get(mat).t1;


            Matcher matcher = Pattern.compile(".+ (-?\\d+)").matcher(ev.getCurrentItem().getItemMeta().getDisplayName());
            if (matcher.matches()) {
                int inSchowek = Integer.parseInt(matcher.group(1));
                int max = Math.min(limit - akt.get(), inSchowek);
                int posiadane = inSchowek - max;
                if (max > 0) {
                    ItemStack item = ev.getCurrentItem();
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName("§6Posiadane§8:§e " + posiadane);
                    item.setItemMeta(meta);
                    if (posiadane > 0 && posiadane <= 64)
                        item.setAmount(posiadane);
                    if (posiadane <= 0)
                        item.setAmount(1);
                    ev.setCurrentItem(item);

                    ev.getWhoClicked().getInventory().addItem(new ItemStack(mat, max));
                }
            }
        }
    }
}
