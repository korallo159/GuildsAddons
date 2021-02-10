package koral.guildsaddons.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {
    /**
     * gracze z tym tagiem nie będą mogli wyciągać itemków z otwartego inventory
     * tag ten jest zapomiany z gracza gdy zamyka on inventory
     *
     * // gracz nie może nic wyjąć z tego inv
     * player.openInventory(inv);
     * player.addScoreboardTag(InventoryClickListener.scBlockTag);
     */
    public static final String scBlockTag = "guildAddonsBlockInvClick";


    @EventHandler
    public void onInventoryClick(InventoryClickEvent ev) {
        if (ev.getWhoClicked().getScoreboardTags().contains(scBlockTag) &&
                (
                     ev.getClick() == ClickType.DOUBLE_CLICK ||
                    (ev.getRawSlot() >= 0 && ev.getRawSlot() < ev.getInventory().getSize())
                ))
            ev.setCancelled(true);
    }
}
