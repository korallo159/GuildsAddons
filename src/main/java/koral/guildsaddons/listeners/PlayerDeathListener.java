package koral.guildsaddons.listeners;

import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.database.statements.PlayersStatements;
import koral.guildsaddons.guilds.CustomTabList;
import koral.guildsaddons.guilds.Guild;
import koral.guildsaddons.util.Pair;
import koral.sectorserver.SectorServer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;

public class PlayerDeathListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent ev) {
        if (ev.isCancelled()) return;
        Player killed = ev.getEntity();
        SectorServer.doForNonNull(killed.getKiller(), killer -> {
            Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.getPlugin(), () -> {
                int kills = PlayersStatements.getKillsData(killer.getName()) + 1;
                int deaths = PlayersStatements.getDeathsData(killed.getName()) + 1;

                PlayersStatements.setKillsData(killer.getName(), kills);
                PlayersStatements.setDeathsData(killed.getName(), deaths);

                CustomTabList.updateKills(killer, kills);
                CustomTabList.updateDeaths(killed, deaths);


                double k = PlayersStatements.getPointsData(killer.getName());
                double d = PlayersStatements.getPointsData(killed.getName());

                Pair<Integer, Integer> pair = getEloValues((int) d, (int) k);

                k += pair.t1;
                d -= pair.t2;

                killer.sendTitle(" ", "§a+" + pair.t1 + " §6pkt", 30, 60, 30);
                killed.sendTitle(" ", "§c-" + pair.t2 + " §6pkt", 30, 60, 30);

                SectorServer.sendToServer("broadcast", "ALL", out -> out.writeUTF("§7" + killer.getDisplayName() + "§a(+" + pair.t1 + ") §6 zabił §7" + killed.getDisplayName() + "§c(-" + pair.t2 + ")"));

                PlayersStatements.setPointsData(killer.getName(), k);
                PlayersStatements.setPointsData(killed.getName(), d);

                CustomTabList.updatePoints(killer, (int) k);
                CustomTabList.updatePoints(killed, (int) d);

                SectorServer.doForNonNull(Guild.fromPlayer(killer.getName()), guild -> guild.addPoints(+pair.t1));
                SectorServer.doForNonNull(Guild.fromPlayer(killed.getName()), guild -> guild.addPoints(-pair.t2));

                CustomTabList.updatePlayersRank();
                CustomTabList.updateGuildsRank();
            });
        });
    }


    // a-b c
    // {(a, b): c}
    public static final Map<Pair<Integer, Integer>, Integer> elo_constants = new HashMap<>();
    public static double elo_exponent = 10;
    public static double elo_divider = 400;
    static Pair<Integer, Integer> getEloValues(int victimPoints, int attackerPoints) {
        int attackerElo = inRange(attackerPoints);
        int victimElo = inRange(victimPoints);

        int attacker = (int) Math.round(attackerElo * (1 - (1.0D / (1.0D + Math.pow(elo_exponent, (victimPoints - attackerPoints) / elo_divider)))));
        int victim   = (int) Math.round(victimElo * (0 - (1.0D / (1.0D + Math.pow(elo_exponent, (attackerPoints - victimPoints) / elo_divider)))) * - 1);

        return new Pair<>(attacker, victim);
    }
    static int inRange(int value) {
        for (Map.Entry<Pair<Integer, Integer>, Integer> entry : elo_constants.entrySet()) {
            Pair<Integer, Integer> range = entry.getKey();

            if (value >= range.t1 && value <= range.t2) {
                return entry.getValue();
            }
        }

        return 0;
    }
}
