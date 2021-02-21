package koral.guildsaddons.util;

import koral.sectorserver.SectorServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PanelYesNo implements Listener {
    private static class PanelTakNieHolder implements InventoryHolder {
        private static ItemStack emptySlot = createOption(Material.BLACK_STAINED_GLASS_PANE, " ");

        Runnable yes;
        Runnable no;

        Inventory inv;

        public PanelTakNieHolder(String title, Runnable yes, Runnable no) {
            inv = Bukkit.createInventory(this, 3*9, ChatColor.translateAlternateColorCodes('&', title));

            for (int i=0; i < inv.getSize(); i++)
                inv.setItem(i, emptySlot);

            this.yes = () -> {
                yes.run();
                this.no = null;
            };
            this.no = no;
        }

        @Override
        public Inventory getInventory() {
            return inv;
        }
    }
    private static ItemStack createOption(Material mat, String title) {
        ItemStack item = new ItemStack(mat);

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(title);
        meta.addItemFlags(ItemFlag.HIDE_DYE);

        item.setItemMeta(meta);

        return item;
    }
    public static void create(Player p, String tytuł, String Tak, String Nie, Runnable tak, Runnable nie) {
        Inventory inv = new PanelTakNieHolder(tytuł, tak, nie).getInventory();

        inv.setItem(12, createOption(Material.LIME_STAINED_GLASS_PANE, Tak));
        inv.setItem(14, createOption(Material.RED_STAINED_GLASS_PANE,  Nie));

        p.openInventory(inv);
    }
    @EventHandler
    public void klikanieEqtaknie(InventoryClickEvent ev) {
        SectorServer.doForNonNull(ev.getInventory().getHolder(), holder -> {
            if (holder instanceof PanelTakNieHolder) {
                if (ev.getRawSlot() < ev.getInventory().getSize() || ev.getRawSlot() >= 0)
                    ev.setCancelled(true);

                if (ev.getRawSlot() == 12)
                    SectorServer.doForNonNull(((PanelTakNieHolder) holder).yes, Runnable::run);
                else if (ev.getRawSlot() == 14)
                    SectorServer.doForNonNull(((PanelTakNieHolder) holder).no, Runnable::run);
                else
                    return;
                ev.getWhoClicked().closeInventory();
            }
        });
    }
    @EventHandler
    public void zamykanieEqtaknie(InventoryCloseEvent ev) {
        SectorServer.doForNonNull(ev.getInventory().getHolder(), holder -> {
            if (holder instanceof PanelTakNieHolder)
                SectorServer.doForNonNull(((PanelTakNieHolder) holder).no, Runnable::run);
        });
    }
}
