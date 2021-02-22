package koral.guildsaddons.listeners;

import koral.guildsaddons.guilds.Guild;
import koral.guildsaddons.guilds.GuildCommand;
import koral.sectorserver.SectorServer;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent ev) {
        if (ev.getBlock().getType() == Material.TNT && ev.getBlock().getLocation().getY() > Guild.tntHeight) {
            ev.getPlayer().sendMessage("Nie możesz stawiać tnt tak wysoko");
            ev.setCancelled(true);
        }

        SectorServer.doForNonNull(Guild.fromPlayer(ev.getPlayer().getName()), guild -> {
            if (guild.isAttacking() && guild.getRegion().contains(GuildCommand.locToVec(ev.getBlock().getLocation()))) {
                ev.getPlayer().sendMessage("§cNie ma czasu na budowanie! Chroń swojej gildi przed najeźdźami!");
                ev.setCancelled(true);
            }
        });
    }
}
