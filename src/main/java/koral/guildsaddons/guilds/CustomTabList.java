package koral.guildsaddons.guilds;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.database.statements.GuildStatements;
import koral.guildsaddons.database.statements.PlayersStatements;
import koral.guildsaddons.util.Pair;
import koral.sectorserver.PluginChannelListener;
import koral.sectorserver.SectorServer;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

//TODO: /nick

public class CustomTabList {
    public enum Slots {
        nick(3),
        rank(4),
        kills(5),
        deaths(6),
        tps(9),
        onlineThere(12),
        onlineAll(13),
        rankPlayersHeader(21),
        firstRankPlayers(23),
        rankGuildsHeader(41),
        firstRankGuilds(43),
        cmdsHeader(61),
        cmdsFirst(63);

        final int index;
        Slots(int index) {
            this.index = index;
        }

        public void update(Player p, String msg) {
            inTab[index].listName = new ChatComponentText(ChatColor.translateAlternateColorCodes('&', msg));
            update(p, inTab[index]);
        }
        static void update(Player p, EntityPlayer... toUpdate) {
            nms(p).playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, toUpdate));
        }
        static void update(Player p, Iterable<EntityPlayer> toUpdate) {
            nms(p).playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, toUpdate));
        }
    }

    public static final int rankSlots = 15;

    static EntityPlayer nms(Player p) {
        return ((CraftPlayer) p).getHandle();
    }

    static EntityPlayer[] inTab = new EntityPlayer[80];


    public static void updateKills(Player p, int kills) {
        CustomTabList.Slots.kills.update(p, "§aZabójstwa: §e" + kills);
    }
    public static void updateDeaths(Player p, int deaths) {
        CustomTabList.Slots.deaths.update(p, "§aŚmierci: §e" + deaths);
    }
    public static void updatePoints(Player p, double points) {
        CustomTabList.Slots.rank.update(p, "§aPunkty: §e" + (int) points);
    }
    public static void updateNick(Player p) {
        CustomTabList.Slots.nick.update(p, "§aNick: §7" + p.getDisplayName());
    }
    public static void updateOnlineThere(Player p) {
        CustomTabList.Slots.onlineThere.update(p, "§aonline sektora: §e" + Bukkit.getOnlinePlayers().size());
    }
    public static void updateOnlineAll(Player p) {
        if(PluginChannelListener.playerCompleterList.size() != 0)
        CustomTabList.Slots.onlineAll.update(p, "§aonline global: §e" +  PluginChannelListener.playerCompleterList.size());
    }

    public static void updatePlayersRank() {
        SectorServer.sendToServer("updateTabPlayersRank", "ALL", out -> {});
    }
    static void _updatePlayersRank() {
        Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.getPlugin(), () -> {
            List<Pair<String, Integer>> rank = PlayersStatements.getTopPlayers();
            for (int i=Slots.firstRankPlayers.index; i < Slots.firstRankPlayers.index + rankSlots; i++) {
                Pair<String, Integer> pair = rank.isEmpty() ? new Pair<>("", null) : rank.remove(0);
                inTab[i].listName = new ChatComponentText("§e" + pair.t1 + "§8: §6" + (pair.t2 != null ? pair.t2 : ""));
            }
            Bukkit.getOnlinePlayers().forEach(CustomTabList::updatePlayersRank);
        });
    }
    public static void updatePlayersRank(Player p) {
        EntityPlayer[] toUpdate = new EntityPlayer[rankSlots];

        for (int i=Slots.firstRankPlayers.index; i < Slots.firstRankPlayers.index + rankSlots; i++)
            toUpdate[i - Slots.firstRankPlayers.index] = inTab[i];

        Slots.update(p, toUpdate);
    }
    public static void updateGuildsRank() {
        SectorServer.sendToServer("updateTabGuildsRank", "ALL", out -> {});
    }
    public static void _updateGuildsRank() {
        Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.getPlugin(), () -> {
            List<Pair<String, Integer>> rank = GuildStatements.getTopGuilds();
            for (int i=Slots.firstRankGuilds.index; i < Slots.firstRankGuilds.index + rankSlots; i++) {
                Pair<String, Integer> pair = rank.isEmpty() ? new Pair<>("", null) : rank.remove(0);
                inTab[i].listName = new ChatComponentText("§c" + pair.t1 + "§8: §4" + (pair.t2 != null ? pair.t2 : ""));
            }
            Bukkit.getOnlinePlayers().forEach(CustomTabList::updateGuildsRank);
        });
    }
    public static void updateGuildsRank(Player p) {
        EntityPlayer[] toUpdate = new EntityPlayer[rankSlots];

        for (int i=Slots.firstRankGuilds.index; i < Slots.firstRankGuilds.index + rankSlots; i++)
            toUpdate[i - Slots.firstRankGuilds.index] = inTab[i];

        Slots.update(p, toUpdate);
    }


    static {
        int num[] = new int[]{0};

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = server.worldServer.values().iterator().next();

        Consumer<String> pole = msg -> inTab[num[0]] = create(server, worldServer, msg, num[0]++);

        pole.accept("");
        pole.accept("§2§lInformacje o Tobie ");
        pole.accept(" ");
        pole.accept("~ nick");
        pole.accept("Ranking");
        pole.accept("Zabójstwa");
        pole.accept("Smierci");
        pole.accept(" ");
        pole.accept("Informacje");
        pole.accept("TPS 21.37");
        pole.accept(" ");
        pole.accept(" ");
        pole.accept(" ");
        pole.accept(" ");
        pole.accept(" ");
        pole.accept(" ");
        pole.accept(" ");
        pole.accept(" ");
        pole.accept(" ");
        pole.accept(" ");

        pole.accept(" ");
        pole.accept("§6§lTop rankingu");
        pole.accept(" ");

        pole.accept("#1");
        pole.accept("#2");
        pole.accept("#3");
        pole.accept("#4");
        pole.accept("#5");
        pole.accept("#6");
        pole.accept("#7");
        pole.accept("#8");
        pole.accept("#9");
        pole.accept("#10");
        pole.accept("#11");
        pole.accept("#12");
        pole.accept("#13");
        pole.accept("#14");
        pole.accept("#15");
        pole.accept(" ");
        pole.accept(" ");

        pole.accept(" ");
        pole.accept("§4§lTop gildie");
        pole.accept(" ");
        pole.accept("#1");
        pole.accept("#2");
        pole.accept("#3");
        pole.accept("#4");
        pole.accept("#5");
        pole.accept("#6");
        pole.accept("#7");
        pole.accept("#8");
        pole.accept("#9");
        pole.accept("#10");
        pole.accept("#11");
        pole.accept("#12");
        pole.accept("#13");
        pole.accept("#14");
        pole.accept("#15");
        pole.accept(" ");
        pole.accept(" ");

        pole.accept(" ");
        pole.accept("§9§lkomendy");
        pole.accept(" ");
        pole.accept("§9/itemshop");
        pole.accept("§9/schowek");
        pole.accept("§9/g");
        pole.accept("§9/spawn");
        pole.accept("§9/tpa");
        pole.accept("§9/ustawdom");
        pole.accept(" ");
        pole.accept(" ");
        pole.accept(" ");
        pole.accept(" ");
        pole.accept(" ");
        pole.accept(" ");
        pole.accept(" ");
        pole.accept(" ");
        pole.accept(" ");
        pole.accept(" ");
        pole.accept(" ");

        Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.getPlugin(), () -> {
            updatePlayersRank();
            updateGuildsRank();
        });
    }

    public static void apply(Player p) {
        p.setPlayerListHeader(ChatColor.GOLD + "" + ChatColor.BOLD + "\nJBWM\n\n§6sektor§8: §a" + SectorServer.serverName);
        p.setPlayerListFooter(ChatColor.translateAlternateColorCodes('&',
                "&9✪ &c&lsklep&8: &f&lsklep.jbwm.pl &9✪ &c&ldc&8: &f&ldiscord.jbwm.pl &9✪ &c&lfb&8: &f&lfb.com/jbwmpl &9✪"));

        nms(p).playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, inTab));

        updateNick(p);
        updatePlayersRank(p);
        updateGuildsRank(p);

        Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.getPlugin(), () -> {
           updateKills(p,  PlayersStatements.getKillsData(p.getName()));
           updateDeaths(p, PlayersStatements.getDeathsData(p.getName()));
           updatePoints(p, PlayersStatements.getPointsData(p.getName()));
        });
        Bukkit.getScheduler().runTask(GuildsAddons.getPlugin(),
                () -> GuildsAddons.pointsObjective.getScore(p.getName()).setScore((int) PlayersStatements.getPointsData(p.getName())));
    }

    private static EntityPlayer create(MinecraftServer server, WorldServer worldServer, String name, int num) {
        PlayerInteractManager playerinteractmanager = new PlayerInteractManager(worldServer);
        GameProfile profile = new GameProfile(UUID.randomUUID(), "!" + (num < 10 ? "0" : "") + num);
        profile.getProperties().put("textures", new Property("textures",
                "ewogICJ0aW1lc3RhbXAiIDogMTYxMzczNTk2NDgxMSwKICAicHJvZmlsZUlkIiA6ICI3MjM3MWFkZjJlOGQ0YzkyYTczNGE5M2M4Mjc5ZGViOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTdG9uZSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85NmRhNzM1Yzg4ZTA4YjgwYmRmODE0MGIwYWRkOGQxODY5MGI4NWI0ZjkwMjViYjg4MWQ3YTE5MTM0MGYzYjMxIgogICAgfQogIH0KfQ==",
                "mSgiOk3sj2eX3KNQydJebpx6sjn7X9XdXDSZqSA+Tbs66gt5sQnXrE4E2dNr5ddSP1B1ldosNVEUc1nzlWalqyzD+lekDHNo3YuNSFtoAFIVyKfNqfwiAlyFFYvaPFSwA/v/pyjoCaKYt/oh8pLDcuNDVRxft3uTFG9RspzaOm7w40znFWOLKesSK2l5q+ijfH1hwFoBGloxHR5U/mC8tzox1gSlHNCZJfRMiOEPQ5ZnlHCCQDwLoRTpblFFRv8+iesLCqelxPPDodMhalIqmVfHPqys+KzYrSAYiuTCmL6TMo5Mzf/sgDZZupCHGpgwA6juJOXqIPUDL9fqY5a8xnSnGJB4bqG+93Fd/oOgO9zJYsWbOX07aiumzezkyl9EOoDKHw5O6Y4lPJiKzyEXB7E1Ig9l5PW8Rc6fxu9Qltqia3EJcx/ApZb3mG3WOsq6aBs1SSs3lCij/bUDUplKxDePGHppAj5+i3ogq9PWgYhco1QAY2XYAwEGEjAcnwGbNKwxaQNXYIbwAz7CG192kN3FGQXTiH5tGN0t+EmhTtkyFFA4TL7XHRUJ3S1Z34lRkVxHNbQX1Y+NC6DBNkWy6CxDLX6jDA0e6vMBDyfYRkQBlNvJMFTQW2lVS8vQ5i16SUf6O5l/soQYlppqvp5vpWJV7Km+4dnkPA8xVcj9PxA="));
        EntityPlayer player = new EntityPlayer(server, worldServer, profile, playerinteractmanager);
        player.listName = new ChatComponentText(name);

        return player;
    }
}
