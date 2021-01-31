package koral.guildsaddons.listeners;

import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class Stoniarki implements Listener {
    final int cooldown = 20; // w tickach

    static ConfigManager config = new ConfigManager("stoniarki.yml");

    static final Set<Location> stone = new HashSet<>();
    static Set<Location> stoniarki = new HashSet<>();

    public static ItemStack itemStoniark;

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent ev) {
        for (Block blok : ev.getBlocks()) {
            if (stoniarki.contains(blok.getLocation())) {
                ev.setCancelled(true);
                return;
            }
        }
    }
    @EventHandler
    public void onPistonExtend(BlockPistonRetractEvent ev) {
        for (Block blok : ev.getBlocks()) {
            if (stoniarki.contains(blok.getLocation())) {
                ev.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority =  EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent ev) {
        if (ev.isCancelled()) return;
        if (stone.contains(ev.getBlock().getLocation()))
            Bukkit.getScheduler().runTaskLater(GuildsAddons.plugin, () -> ev.getBlock().setType(Material.STONE), cooldown);
        else if (stoniarki.contains(ev.getBlock().getLocation())) {
            if (!ev.getPlayer().getInventory().addItem(itemStoniark).isEmpty())
                ev.getPlayer().getWorld().dropItem(ev.getPlayer().getLocation(), itemStoniark);
            stoniarki.remove(ev.getBlock().getLocation());
            stone.remove(ev.getBlock().getLocation().add(0, 1, 0));
            config.config.set("stoniarki", stoniarki);
            config.save();
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent ev) {
        if (ev.isCancelled()) return;
        if (ev.getItemInHand().isSimilar(itemStoniark)) {
            stoniarki.add(ev.getBlock().getLocation());

            Location locStone = ev.getBlock().getLocation().add(0, 1, 0);
            stone.add(locStone);
            locStone.getBlock().setType(Material.STONE);

            config.config.set("stoniarki", stoniarki);
            config.save();
        }
    }

    public static void reload() {
        config.reloadCustomConfig();
        stoniarki = (Set<Location>) config.config.get("stoniarki", new HashSet<>());
        stone.clear();
        stoniarki.forEach(loc -> stone.add(loc.clone().add(0, 1, 0)));
    }
}
