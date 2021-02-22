package koral.guildsaddons.listeners;

import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.database.statements.PlayersStatements;
import koral.guildsaddons.guilds.CustomTabList;
import koral.guildsaddons.guilds.Guild;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static koral.guildsaddons.commands.Sethome.homesCompleterGet;

public class PlayerJoin implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.getPlugin(), () -> {
            PlayersStatements.createPlayerQuery(e.getPlayer());
            CustomTabList.apply(e.getPlayer());
        });
        Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.getPlugin(), () -> homesCompleterGet(e.getPlayer()));
        Guild.playerJoinEvent(e);


        Bukkit.getOnlinePlayers().forEach(CustomTabList::updateOnlineThere);
    }
}
