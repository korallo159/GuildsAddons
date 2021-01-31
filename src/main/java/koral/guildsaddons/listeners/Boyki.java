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

import java.util.function.Consumer;

public class Boyki implements Listener {
    final int cooldown = 10;
    public static ItemStack itemAutoFosa;
    public static ItemStack itemBoyFarmer;
    public static ItemStack itemSandFarmer;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent ev) {
        Consumer<Location> consumer;

        if      (ev.getItemInHand().isSimilar(itemAutoFosa))    consumer = this::removeBlock;
        else if (ev.getItemInHand().isSimilar(itemBoyFarmer))   consumer = loc -> setBlock(loc, Material.OBSIDIAN);
        else if (ev.getItemInHand().isSimilar(itemSandFarmer))  consumer = loc -> setBlock(loc, Material.SAND);
        else return;

        Bukkit.getScheduler().runTask(GuildsAddons.plugin, () -> {
            ev.getBlock().setType(Material.AIR, false);
            consumer.accept(ev.getBlock().getLocation().add(.5, .5, .5));
        });
    }


    private void setBlock(Location loc, Material mat) {
        if (!loc.getBlock().getType().isAir() || loc.getY() <= 0)
            return;
        loc.getBlock().setType(mat, false);
        loc.getWorld().spawnParticle(Particle.CLOUD, loc, 5, .1, .1, .1, .1);
        Bukkit.getScheduler().runTaskLater(GuildsAddons.plugin, () -> setBlock(loc.add(0, -1, 0), mat), cooldown);
    }
    private void removeBlock(Location loc) {
        if (loc.getBlock().getType() == Material.BEDROCK || loc.getY() <= 0)
            return;
        loc.getBlock().setType(Material.AIR);
        loc.getWorld().spawnParticle(Particle.CLOUD, loc, 5, .1, .1, .1, .1);
        Bukkit.getScheduler().runTaskLater(GuildsAddons.plugin, () -> removeBlock(loc.add(0, -1, 0)), cooldown);
    }
}
