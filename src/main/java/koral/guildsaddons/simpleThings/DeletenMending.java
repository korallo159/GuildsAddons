package koral.guildsaddons.simpleThings;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class DeletenMending implements Listener {
    @EventHandler
    public void onFishing(PlayerFishEvent ev) {
        if (!ev.isCancelled() && ev.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            if (ev.getCaught() instanceof Item) {
                Item item = (Item) ev.getCaught();
                if (item.getItemStack().getType() == Material.ENCHANTED_BOOK) {
                    EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemStack().getItemMeta();
                    if (meta.hasStoredEnchant(Enchantment.MENDING))
                        ev.setCancelled(true);
                }
            }
        }
    }
}
