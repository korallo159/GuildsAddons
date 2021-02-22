package koral.guildsaddons.simpleThings;

import koral.guildsaddons.GuildsAddons;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class Cobblex implements Listener {
    static NamespacedKey key = new NamespacedKey(GuildsAddons.plugin, "cobblex");

    public Cobblex() {
        refreshRecipe();
    }

    private void refreshRecipe() {
        if (Bukkit.getRecipe(key) == null) {
            ShapedRecipe rec = new ShapedRecipe(key, itemCobblex);
            rec.shape("CCC", "CCC", "CCC");
            rec.setIngredient('C', Material.COBBLESTONE);
            Bukkit.addRecipe(rec);
        }

        Bukkit.getScheduler().runTaskLater(GuildsAddons.plugin, this::refreshRecipe, 20*30);
    }

    // Będzie craftowany lootbag z mimi

    public static ItemStack itemCobblex;

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent ev) {
        if (ev.getRecipe() instanceof  ShapedRecipe && ((ShapedRecipe) ev.getRecipe()).getKey().equals(key)) {
            for (ItemStack item : ev.getInventory().getMatrix())
                if (item == null || item.getType() != Material.COBBLESTONE || item.getAmount() != 64) {
                    ev.getInventory().setResult(null);
                    return;
                }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemCraft(CraftItemEvent ev) {
        if (ev.getRecipe() instanceof  ShapedRecipe && ((ShapedRecipe) ev.getRecipe()).getKey().equals(key)) {
            for (ItemStack item : ev.getInventory().getMatrix())
                if (item == null || item.getType() != Material.COBBLESTONE || item.getAmount() != 64) {
                    ev.getInventory().setResult(null);
                    ev.setCancelled(true);
                    return;
                }
            ev.setCancelled(true);
            if (ev.getWhoClicked().getItemOnCursor().getType() != Material.AIR)
                return;
            ev.getInventory().setMatrix(new ItemStack[9]);
            ev.getWhoClicked().setItemOnCursor(itemCobblex);
        }
    }
}
