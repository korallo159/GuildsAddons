package koral.guildsaddons.listeners;

import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.database.statements.PlayersStatements;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.getPlugin(), () -> {
            PlayersStatements.createPlayerQuery(e.getPlayer());
        });
        if(PluginChannelListener.tpaTeleport.containsKey(e.getPlayer().getName())){
            Player target = Bukkit.getPlayer(PluginChannelListener.tpaTeleport.get(e.getPlayer().getName()));
            Bukkit.getScheduler().runTask(GuildsAddons.getPlugin(), () -> e.getPlayer().teleport(target.getLocation()));
            System.out.println(PluginChannelListener.tpaTeleport.get(e.getPlayer().getName()));
            PluginChannelListener.tpaTeleport.remove(e.getPlayer().getName());
        }
    }
}
