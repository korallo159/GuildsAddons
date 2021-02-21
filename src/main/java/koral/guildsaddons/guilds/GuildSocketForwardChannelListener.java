package koral.guildsaddons.guilds;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import koral.guildsaddons.database.statements.GuildStatements;
import koral.sectorserver.ForwardChannelListener;
import koral.sectorserver.SectorServer;
import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public class GuildSocketForwardChannelListener implements ForwardChannelListener {
    // regiony
    static RegionManager getRegions(String worldName) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(Bukkit.getWorld(worldName)));
    }

    static void guildRemoveRegion(DataInputStream in) throws IOException {
        String region_world = in.readUTF();
        String region = in.readUTF();

        try {
            RegionManager regions = getRegions(region_world);

            regions.removeRegion(region);
            regions.removeRegion(region + "_max");
        } catch (Throwable e) {
        }
    }
    static void removeMember(DataInputStream in) throws IOException {
        String region_world = in.readUTF();
        String regionId = in.readUTF();
        String playerName = in.readUTF();

        memberOperation(region_world, regionId, playerName, DefaultDomain::removePlayer);
    }
    static void addMember(DataInputStream in) throws IOException {
        String region_world = in.readUTF();
        String regionId = in.readUTF();
        String playerName = in.readUTF();

        memberOperation(region_world, regionId, playerName, DefaultDomain::addPlayer);
    }
    private static void memberOperation(String region_world, String regionId, String playerName, BiConsumer<DefaultDomain, String> func) {
        try {
            ProtectedCuboidRegion region = (ProtectedCuboidRegion) getRegions(region_world).getRegion(regionId);

            DefaultDomain members = region.getMembers();
            func.accept(members, playerName);
            region.setMembers(members);
        } catch (Throwable e) {
        }

    }
    static void expandRegion(DataInputStream in) throws IOException {
        String guildName = in.readUTF();
        int dxz = in.readInt();

        Guild guild = Guild.fromName(guildName);

        SectorServer.doForNonNull(guild.getRegion(), region -> {
            region.setMinimumPoint(GuildCommand.getMinRegionPoint(guild.getHeartLoc(), dxz));
            region.setMaximumPoint(GuildCommand.getMaxRegionPoint(guild.getHeartLoc(), dxz));
        });
    }


    // mapy
    static void updateAllyInviteMapRemove(DataInputStream in) throws IOException {
        GuildCommand.allyInviteMap.remove(in.readUTF(), in.readUTF());
    }
    static void updateAllyInviteMapPut(DataInputStream in) throws IOException {
        GuildCommand.allyInviteMap.put(in.readUTF(), in.readUTF());
    }

    static void updateInvitesAdd(DataInputStream in) throws IOException {
        GuildCommand.invites.add(in.readUTF());
    }
    static void updateInvitesRemove(DataInputStream in) throws IOException {
        GuildCommand.invites.remove(in.readUTF());
    }



    // gildie
    static void guild_set(DataInputStream in) throws IOException {
        String playerName = in.readUTF();
        String guildName = in.readUTF();

        GuildCommand.setGuild(playerName, guildName.equals("") ? null : guildName);
    }

    static void guild_msg(DataInputStream in) throws IOException {
        String guildName = in.readUTF();
        String msg = in.readUTF();

        SectorServer.doForNonNull(Guild.fromNameUnSafe(guildName), guild -> guild.sendToMembersOnlyThere(msg));
    }

    static void guild_save(DataInputStream in) throws IOException {
        String guildName = in.readUTF();
        String data = in.readUTF();

        SectorServer.doForNonNull(Guild.fromNameUnSafe(guildName), guild -> {
            Guild newGuild;
            try {
                newGuild = GuildStatements.deserialize((JSONObject) new JSONParser().parse(data));
                for (Field field : Guild.class.getDeclaredFields())
                    if (!Modifier.isStatic(field.getModifiers()) && !Objects.equals(field.get(guild), field.get(newGuild))) {
                        field.set(guild, field.get(newGuild));
                        System.out.println(String.format("Ustawiono gildi %s wartość %s na %s", guild.name, field.getName(), field.get(guild)));//TODO: usunąć tego loga
                    }
            } catch (ParseException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    static void guild_delete(DataInputStream in) throws IOException {
        String guildName = in.readUTF();

        SectorServer.doForNonNull(Guild.fromNameUnSafe(guildName), Guild::deleteUnSafe);
    }


    static void updateTabPlayersRank(DataInputStream in) {
        CustomTabList._updatePlayersRank();
    }
    static void updateTabGuildsRank(DataInputStream in) {
        CustomTabList._updateGuildsRank();
    }

}
