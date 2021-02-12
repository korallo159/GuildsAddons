package koral.guildsaddons.guilds;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import koral.sectorserver.ForwardChannelListener;
import koral.sectorserver.SectorServer;
import org.bukkit.Bukkit;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public class GuildSocketForwardChannelListener implements ForwardChannelListener {
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

    static void updateAllyInviteMapRemove(DataInputStream in) throws IOException {
        GuildCommand.allyInviteMap.remove(in.readUTF(), in.readUTF());
    }
    static void updateAllyInviteMapPut(DataInputStream in) throws IOException {
        GuildCommand.allyInviteMap.put(in.readUTF(), in.readUTF());
    }
    static void updateInviteMapPut(DataInputStream in) throws IOException {
        String playerName = in.readUTF();
        String guildName = in.readUTF();

        SectorServer.doForNonNull(Guild.fromNameUnSafe(guildName), guild -> GuildCommand.inviteMap.put(playerName, guild));
    }
    static void updateInviteMapRemove1(DataInputStream in) throws IOException {
        GuildCommand.inviteMap.remove(in.readUTF());
    }
    static void updateInviteMapRemove2(DataInputStream in) throws IOException {
        String playerName = in.readUTF();
        String guildName = in.readUTF();

        SectorServer.doForNonNull(Guild.fromNameUnSafe(guildName), guild -> GuildCommand.inviteMap.remove(playerName, guild));
    }
}
