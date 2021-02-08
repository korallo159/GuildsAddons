package koral.guildsaddons.listeners;
import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.commands.Is;
import koral.guildsaddons.database.statements.PlayersStatements;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;

import static koral.guildsaddons.commands.Sethome.homesCompleterGet;

public class PlayerJoin implements Listener {


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.getPlugin(), () -> {
            PlayersStatements.createPlayerQuery(e.getPlayer());
        });
        Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.getPlugin(), () -> homesCompleterGet(e.getPlayer()));

    }


}
