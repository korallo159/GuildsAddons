package koral.guildsaddons.listeners;

import koral.guildsaddons.GuildsAddons;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class AutoFosa implements Listener {
    final int cooldown = 10;
    public static ItemStack itemAutoFosa;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent ev) {
        if (ev.getItemInHand().isSimilar(itemAutoFosa))
            Bukkit.getScheduler().runTask(GuildsAddons.plugin, () -> setBlock(ev.getBlock().getLocation().add(.5, .5, .5)));
    }

    private void setBlock(Location loc) {
        if (loc.getBlock().getType() == Material.BEDROCK || loc.getY() <= 0)
            return;
        loc.getBlock().setType(Material.AIR);
        loc.getWorld().spawnParticle(Particle.CLOUD, loc, 5, .1, .1, .1, .1);
        Bukkit.getScheduler().runTaskLater(GuildsAddons.plugin, () -> setBlock(loc.add(0, -1, 0)), cooldown);
    }
}
