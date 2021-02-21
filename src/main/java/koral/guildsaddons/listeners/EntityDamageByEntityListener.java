package koral.guildsaddons.listeners;

import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.guilds.Guild;
import org.bukkit.Bukkit;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Objects;

public class EntityDamageByEntityListener implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent ev) {
        if (!guildPvP(ev))
            breakingHear(ev);
    }

    boolean breakingHear(EntityDamageByEntityEvent ev) {
        if (ev.getEntity() instanceof EnderCrystal && ev.getEntity().getScoreboardTags().contains(Guild.scHeartTag)) {
            if (ev.getDamager() instanceof Player) {
                Player p = (Player) ev.getDamager();
                Guild guild = Guild.fromLocation(ev.getEntity().getLocation());
                Guild playerGuild;


                if (guild.equals(Guild.fromPlayer(p.getName()))) {
                    p.sendMessage("Nie możesz zniszczyć serca swojej gildi");
                    ev.setCancelled(true);
                } else if (guild.protect >= System.currentTimeMillis()) {
                    p.sendMessage("Ta gildia jest aktualnie pod chronioną");
                    ev.setCancelled(true);
                } else if ((playerGuild = Guild.fromPlayer(p.getName())) != null && playerGuild.alliances.contains(guild.name)) {
                    p.sendMessage("Nie możesz zniszczyć serca sojuszniczej gildi");
                    ev.setCancelled(true);
                } else {
                    Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.getPlugin(), () -> guild.breakHeart(p));
                }
            } else
                ev.setCancelled(true);

            return true;
        }

        return false;
    }
    boolean guildPvP(EntityDamageByEntityEvent ev) {
        final Player p1;
        final Player p2;

        if (ev.getEntity() instanceof Player)
            p1 = (Player) ev.getEntity();
        else
            return false;

        if (ev.getDamager() instanceof Player)
            p2 = (Player) ev.getDamager();
        else {
            if (ev.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) ev.getDamager();
                if (projectile.getShooter() != null && projectile.getShooter() instanceof Player)
                    p2 = (Player) projectile.getShooter();
                else
                    return false;
            } else
                return false;
        }

        Guild g1 = Guild.fromPlayer(p1.getName());
        Guild g2 = Guild.fromPlayer(p2.getName());


        if (Objects.equals(g1, g2) && g1 != null && !g1.pvp)
            ev.setCancelled(true);
        else if (g1 != null && g2 != null && g1.alliances.contains(g2.name))
            ev.setCancelled(true);

        return true;
    }
}
