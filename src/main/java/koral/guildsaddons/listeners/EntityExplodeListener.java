package koral.guildsaddons.listeners;

import koral.guildsaddons.guilds.Guild;
import koral.sectorserver.SectorServer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.lang.reflect.InvocationTargetException;

public class EntityExplodeListener implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityExplode(EntityExplodeEvent ev) {
        // wybuchanie obsydianu
        Block block;
        int a;
        try {
            a = (int) (float) ev.getEntity().getClass().getDeclaredMethod("getYield").invoke(ev.getEntity());
        } catch (NoSuchMethodException e) {
            a = (int) ev.getYield();
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return;
        }
        a -= 1;

        for (int x = -a; x <= a; x++)
            for (int z = -a; z <= a; z++)
                for (int y = -a; y <= a; y++)
                    if ((block = ev.getEntity().getLocation().clone().add(x, y, z).getBlock()).getType() == Material.OBSIDIAN && Math.random() <= .08)//TODO: wczytywać szanse na robicie obsydianu
                        ev.blockList().add(block);

        // wybuchanie tnt na terenie gildi
        if (ev.getEntity() instanceof TNTPrimed) {
            if (ev.getEntity().getLocation().getY() > Guild.tntHeight) {
                ev.setCancelled(true);
                return;
            }
            SectorServer.doForNonNull(Guild.fromLocation(ev.getEntity().getLocation()), guild -> {
                if (guild.protect >= System.currentTimeMillis()) {
                    ev.setCancelled(true);
                    SectorServer.doForNonNull(((TNTPrimed) ev.getEntity()).getSource(), source -> source.sendMessage("Gildia " + guild.name + " jest jeszcze pod ochroną!"));
                } else
                    guild.attack();
            });
        }
    }
}
