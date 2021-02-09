package koral.guildsaddons.guilds;

import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.database.statements.GuildStatements;
import koral.guildsaddons.database.statements.PlayersStatements;
import koral.guildsaddons.util.SerializableLocation;
import koral.sectorserver.SectorServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Guild  {
    public String name;
    public String tag;

    public String leader; // lider
    public String subLeader; // zastępca
    public List<String> members = new ArrayList<>(); // reszta graczy

    public String region;

    public SerializableLocation home;

    public boolean pvp; // gracze w gildi mogą/nie mogą sie bić

    public int hearts;

    public int level;

    public long protect; // milisekundy końca ochrony
    public long creation_date;

    public Guild(String name, String tag, String leader, String subLeader, List<String> members, SerializableLocation home,
                 String region, boolean pvp, int hearts, int level, long protect, long creation_date) {
        this.name = name;
        this.tag = tag;
        this.leader = leader;
        this.subLeader = subLeader;
        this.members = members;
        this.home = home;
        this.region = region;
        this.pvp = pvp;
        this.hearts = hearts;
        this.level = level;
        this.protect = protect;
        this.creation_date = creation_date;
    }

    public void save() {
        GuildStatements.updatadeData(this);
    }


    public boolean sendToMembers(String format, String... args) {
        String msg = String.format(format, args);
        Consumer<String> send = nick -> SectorServer.doForNonNull(Bukkit.getPlayer(nick), p -> p.sendMessage(msg));

        send.accept(leader);
        if (subLeader != null) send.accept(subLeader);
        members.forEach(send::accept);

        System.out.println(String.format("Guild %s[%s]: %s", name, tag, msg));

        return true;
    }



    static Map<String, WeakReference<Guild>> fromName = new HashMap<>();
    static Map<String, WeakReference<Guild>> fromTag = new HashMap<>();
    static Map<String, Guild> fromPlayer = new HashMap<>();
    public static Guild fromPlayer(String nick) {
        nick = nick.toLowerCase();

        Guild guild = fromPlayer.get(nick);

        if (guild == null && !fromPlayer.containsKey(nick))
            guild = PlayersStatements.getGuild(nick);

        return guild;
    }
    public static Guild fromName(String name) {
        return getFrom(fromName, name, "NAME");
    }
    public static Guild fromTag(String tag) {
        return getFrom(fromName, tag, "TAG");
    }
    private static Guild getFrom(Map<String, WeakReference<Guild>> map, String key, String primaryKey) {
        WeakReference<Guild> reference = map.get(key);
        Guild guild = reference == null ? null : reference.get();
        if (guild == null) {
            guild = GuildStatements.getFrom(key, primaryKey);
            if (guild != null) {
                reference = new WeakReference<>(guild);
                fromName.put(guild.name, reference);
                fromTag.put(guild.tag, reference);
            }
        }
        return guild;
    }

    public static void playerJoinEvent(PlayerJoinEvent ev) {
        Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.getPlugin(),
                () -> fromPlayer.put(ev.getPlayer().getName().toLowerCase(), PlayersStatements.getGuild(ev.getPlayer().getName())));
    }
    public static void playerQuitEvent(PlayerQuitEvent ev) {
        fromPlayer.remove(ev.getPlayer().getName().toLowerCase());
    }
}
