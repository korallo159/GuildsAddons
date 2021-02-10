package koral.guildsaddons.guilds;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.database.statements.GuildStatements;
import koral.guildsaddons.database.statements.PlayersStatements;
import koral.guildsaddons.util.SerializableLocation;
import koral.sectorserver.SectorServer;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;

public class Guild  {
    public static int membersLimit = 30;

    public static int max_region_dxz = 150;
    public static int start_region_dxz = 150;
    public static int region_priority = 5;


    public String name;
    public String tag;

    public String leader; // lider
    public String subLeader; // zastępca
    public List<String> members; // reszta graczy

    public String region;
    public String region_world;

    public SerializableLocation home;

    public boolean pvp; // gracze w gildi mogą/nie mogą sie bić

    public int hearts;

    public int level;

    public long protect; // milisekundy końca ochrony
    public long creation_date;

    public Guild(String name, String tag, String leader, String subLeader, List<String> members, SerializableLocation home,
                 String region, String region_world, boolean pvp, int hearts, int level, long protect, long creation_date) {
        this.name = name;
        this.tag = tag;
        this.leader = leader;
        this.subLeader = subLeader;
        this.members = members;
        this.home = home;
        this.region = region;
        this.region_world = region_world;
        this.pvp = pvp;
        this.hearts = hearts;
        this.level = level;
        this.protect = protect;
        this.creation_date = creation_date;
    }

    public void save() {
        GuildStatements.updatadeData(this);
        GuildsAddons.sendPluginMessageForward("ALL", "updateGuild", out -> {
            out.writeUTF(name);
            out.writeUTF(GuildStatements.serialize(this));
        });
    }


    public boolean sendToMembers(String format, String... args) {
        String msg = String.format(format, args);

        System.out.println(String.format("Guild %s[%s]: %s", name, tag, msg));

        sendToMembersOnlyThere(msg);

        GuildsAddons.sendPluginMessageForward("ALL", "guildMsg", out -> {
            out.writeUTF(name);
            out.writeUTF(msg);
        });

        return true;
    }
    public void sendToMembersOnlyThere(String msg) {
        Consumer<String> send = nick -> SectorServer.doForNonNull(Bukkit.getPlayer(nick), p -> p.sendMessage(msg));

        send.accept(leader);
        if (subLeader != null) send.accept(subLeader);
        members.forEach(send::accept);

        System.out.println(String.format("Guild %s[%s]: %s", name, tag, msg));
    }

    public ProtectedCuboidRegion getRegion() {
        return (ProtectedCuboidRegion) WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(Bukkit.getWorld(region_world))).getRegion(region);
    }


    static Map<String, WeakReference<Guild>> fromName = new HashMap<>();
    static Map<String, WeakReference<Guild>> fromTag = new HashMap<>();
    static Map<String, Guild> fromPlayer = new HashMap<>();
    public static Guild fromPlayer(String nick) {
        nick = nick.toLowerCase();

        Guild guild = fromPlayer.get(nick);

        if (guild == null && !fromPlayer.containsKey(nick))
            guild = fromName(PlayersStatements.getGuildName(nick));

        return guild;
    }
    public static Guild fromNameUnSafe(String name) {
        WeakReference<Guild> reference = fromName.get(name);
        return reference == null ? null : reference.get();
    }
    public static Guild fromName(String name) {
        return getFrom(fromName, name, "NAME");
    }
    public static Guild fromTag(String tag) {
        return getFrom(fromName, tag, "TAG");
    }
    private static Guild getFrom(Map<String, WeakReference<Guild>> map, String key, String primaryKey) {
        if (key == null) return null;
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


    public void deleteUnSafe() {
        fromName.remove(name);
        fromTag.remove(tag);

        Consumer<String> forget = playerName -> {
            playerName = playerName.toLowerCase();
            if (fromPlayer.containsKey(playerName))
                fromPlayer.put(playerName, null);
        };

        forget.accept(leader);
        if (subLeader != null) forget.accept(subLeader);
        members.forEach(forget::accept);
    }
    public static void playerJoinEvent(PlayerJoinEvent ev) {
        Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.getPlugin(), () -> {
            String guildName = PlayersStatements.getGuildName(ev.getPlayer().getName());
            if (guildName == null)
                fromPlayer.put(ev.getPlayer().getName().toLowerCase(), guildName == null ? null : fromName(guildName));
        });
    }
    public static void playerQuitEvent(PlayerQuitEvent ev) {
        fromPlayer.remove(ev.getPlayer().getName().toLowerCase());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Guild guild = (Guild) o;
        return this.name.equals(guild.name);
    }
    @Override
    public int hashCode() {
        return Objects.hash(name, tag, creation_date);
    }

    @Override
    public String toString() {
        return "Guild{" +
                "name='" + name + '\'' +
                ", tag='" + tag + '\'' +
                ", leader='" + leader + '\'' +
                ", subLeader='" + subLeader + '\'' +
                ", members=" + members +
                ", region='" + region + '\'' +
                ", home=" + home +
                ", pvp=" + pvp +
                ", hearts=" + hearts +
                ", level=" + level +
                ", protect=" + protect +
                ", creation_date=" + creation_date +
                '}';
    }
}
