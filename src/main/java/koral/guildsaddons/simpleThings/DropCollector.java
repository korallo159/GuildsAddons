package koral.guildsaddons.simpleThings;

import koral.sectorserver.SectorServer;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class DropCollector implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent ev) {
        if (ev.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;
        if (!ev.isCancelled() && ev.isDropItems()) {
            ev.setDropItems(false);

            Consumer<ItemStack> cons = item ->
                    ev.getPlayer().getInventory().addItem(item).forEach((count, cancelledItem) ->
                            ev.getBlock().getWorld().dropItem(ev.getBlock().getLocation(), cancelledItem));

            ev.getBlock().getDrops(ev.getPlayer().getInventory().getItemInMainHand()).forEach(cons);
            if (ev.getBlock().getState() instanceof Container) {
                if (ev.getBlock().getState() instanceof ShulkerBox)
                    return;
                Container container = (Container) ev.getBlock().getState();
                container.getInventory().forEach(item -> SectorServer.doForNonNull(item, cons::accept));
                container.getInventory().clear();
            }
        }
    }
}
