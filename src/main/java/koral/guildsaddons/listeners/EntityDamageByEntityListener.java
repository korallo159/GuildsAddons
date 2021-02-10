package koral.guildsaddons.listeners;

import koral.guildsaddons.guilds.Guild;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntityListener implements Listener {
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent ev) {
        final Player p1;
        final Player p2;

        if (ev.getEntity() instanceof Player)
            p1 = (Player) ev.getEntity();
        else
            return;

        if (ev.getDamager() instanceof Player)
            p2 = (Player) ev.getDamager();
        else {
            if (ev.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) ev.getDamager();
                if (projectile.getShooter() != null && projectile.getShooter() instanceof Player)
                    p2 = (Player) projectile.getShooter();
                else
                    return;
            } else
                return;
        }

        Guild g1 = Guild.fromPlayer(p1.getName());
        Guild g2 = Guild.fromPlayer(p2.getName());

        if (g1.equals(g2) && !g1.pvp)
            ev.setCancelled(true);
    }
}
