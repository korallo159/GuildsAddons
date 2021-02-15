package koral.guildsaddons.listeners;

import koral.guildsaddons.guilds.Guild;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent ev) {
        if (ev.getBlock().getType() == Material.TNT && ev.getBlock().getLocation().getY() > Guild.tntHeight) {
            ev.getPlayer().sendMessage("Nie możesz stawiać tnt tak wysoko");
            ev.setCancelled(true);
        }
    }
}
