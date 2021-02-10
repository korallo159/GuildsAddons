package koral.guildsaddons.simpleThings;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class DropCollector implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent ev) {
        if (ev.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;
        if (!ev.isCancelled() && ev.isDropItems()) {
            ev.setDropItems(false);
            ev.getBlock().getDrops(ev.getPlayer().getInventory().getItemInMainHand()).forEach(item ->
                    ev.getPlayer().getInventory().addItem(item).forEach((count, cancelledItem) ->
                            ev.getBlock().getWorld().dropItem(ev.getBlock().getLocation(), cancelledItem)));
        }
    }
}
