package koral.guildsaddons.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import koral.guildsaddons.guilds.Guild;
import koral.guildsaddons.guilds.GuildCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class PlayerCommandPreprocessListener implements Listener {
    public static List<String> blockedCmds = new ArrayList<>();

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent ev) {
        for (String cmd : blockedCmds)
            if (ev.getMessage().toLowerCase().startsWith(cmd.toLowerCase())) {
                Set<ProtectedRegion> regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(ev.getPlayer().getWorld())).getApplicableRegions(GuildCommand.locToVec(ev.getPlayer().getLocation())).getRegions();
                Pattern pattern = Pattern.compile("guild_-?\\d+x-?\\d+z");
                for (ProtectedRegion region : regions) {
                    if (pattern.matcher(region.getId()).matches()) {
                        Guild guild = Guild.fromPlayerUnSafe(ev.getPlayer().getName());
                        if (guild == null || !guild.region.equals(region.getId())) {
                            ev.getPlayer().sendMessage("§cNie możesz tego użyć na terenie nie swojej gildi");
                            ev.setCancelled(true);
                        }
                    }
                }
            }
    }
}
