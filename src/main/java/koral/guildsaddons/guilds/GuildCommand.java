package koral.guildsaddons.guilds;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.database.statements.GuildStatements;
import koral.guildsaddons.database.statements.PlayersStatements;
import koral.guildsaddons.listeners.InventoryClickListener;
import koral.guildsaddons.managers.ConfigManager;
import koral.guildsaddons.simpleThings.StoneDrop;
import koral.guildsaddons.util.Pair;
import koral.guildsaddons.util.SerializableLocation;
import koral.sectorserver.PluginChannelListener;
import koral.sectorserver.SectorServer;
import koral.sectorserver.util.Teleport;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class GuildCommand implements TabExecutor {
    public static void reloadGuildItems() {
        ConfigManager config = new ConfigManager("guild items.yml");

        Guild.upgradeCosts.clear();
        config.config.getMapList("upgrades").forEach(map -> {
            Map<Material, Integer> upgradeMap = new HashMap<>();
            ((Map<String, Integer>) map.get("items")).forEach(
                    (mat, amount) -> upgradeMap.put(Material.valueOf(mat.toUpperCase().replace(' ', '_')), amount));
            Guild.upgradeCosts.add(new Pair<>((int) map.get("size"), upgradeMap));
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> list = new ArrayList<>();

        if (args.length <= 1 && sender instanceof Player) {

            Guild guild = Guild.fromPlayer(sender.getName());

            list.add("info");

            if (guild != null) {
                list.add("opuść");
                if (guild.home != null)
                    list.add("dom");
                boolean leader = sender.getName().equals(guild.leader);
                if (leader || sender.getName().equals(guild.subLeader)) {
                    list.add("pvp");
                    list.add("zaproś");
                    list.add("wyrzuć");
                    list.add("powiększ");
                    list.add("ustawhome");
                    list.add("sojusz");
                    if (guild.level < Guild.upgradeCosts.size())
                        list.add("itemy");
                    if (leader) {
                        list.add("usuń");
                        list.add("zastępca");
                    }
                }
            } else {
                list.add("załóż");
                list.add("itemy");
                if (inviteMap.containsKey(sender.getName()))
                    list.add("dołącz");
            }


        } else
            list = PluginChannelListener.playerCompleterList;


        List<String> result = new ArrayList<>();
        for (String str : list)
            if (args.length == 0 || str.startsWith(args[args.length - 1].toLowerCase()))
                result.add(str);

        return result;
    }

    public static boolean msg(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.getPlugin(), () -> ((Predicate<Player>) p -> {
            if (args.length < 1)
                return pomoc(sender);

            switch (args[0].toLowerCase()) {
                case "załóż":
                case "zaloz":
                    return załóż(p, args);
                case "usuń":
                case "usun"://TODO: potwierdzenie
                    return usuń(p);
                case "opuść":
                case "opusc"://TODO: tepać gdzieś jeśli jest na terenie gildi
                    return opuść(p);
                case "wyrzuć":
                case "wyrzuc":
                    return wyrzuć(p, args);
                case "zaproś":
                case "zapros":
                case "dodaj":
                case "invite":
                    return zaproś(p, args);
                case "dołącz":
                case "dolacz":
                case "join":
                    return dołącz(p);
                case "zastępca":
                case "zastepca":
                    return zastępca(p, args);
                case "ustawhome":
                case "ustawdom":
                case "sethome":
                    return ustawdom(p);
                case "dom":
                case "home":
                    return dom(p);
                case "pvp":
                    return pvp(p);
                case "itemy":
                    return itemy(p);
                case "sojusz":
                    return sojusz(p, args);
                case "usuńsojusz":
                case "usunsojusz":
                    return usuńsojusz(p, args);
                case "powiększ":
                case "powieksz":
                    return powiększ(p);
                case "info":
                    return info(sender, args);
                default:
                    return pomoc(sender);
            }
        }).test(sender instanceof Player ? (Player) sender : null));
        return true;
    }


    boolean playerOnline(String playerName) {
        for (String name : PluginChannelListener.playerCompleterList)
            if (name.equalsIgnoreCase(playerName))
                return true;
        return false;
    }

    public static void setGuildOnEveryServers(String playerName, Guild guild) {
        setGuild(playerName, guild, true);
        GuildsAddons.sendPluginMessageForward("ALL", "setGuild", out -> {
            out.writeUTF(playerName);
            out.writeUTF(guild == null ? null : guild.name);
        });
    }
    public static boolean setGuild(String playerName, Guild guild) {
        return setGuild(playerName, guild, true);
    }
    public static boolean setGuild(String playerName, Guild guild, boolean updateDatebase) {
        if (updateDatebase)
            PlayersStatements.setGuild(playerName, guild);

        playerName = playerName.toLowerCase();
        if (Guild.fromPlayer.containsKey(playerName)) {
            Guild.fromPlayer.put(playerName, guild);
            return true;
        }
        return false;
    }

    static BlockVector3 getMinRegionPoint(Location loc, int dxz) {
        return BlockVector3.at((int) (loc.getBlockX() - dxz / 2d), 0,   (int) (loc.getBlockZ() - dxz / 2d));
    }
    static BlockVector3 getMaxRegionPoint(Location loc, int dxz) {
        return BlockVector3.at((int) (loc.getBlockX() + dxz / 2d), 256, (int) (loc.getBlockZ() + dxz / 2d));
    }


    boolean załóż(Player p, String args[]) {
        Guild guild = Guild.fromPlayer(p.getName());
        if (guild != null) return msg(p, "Posiadasz już gildię");
        if (args.length < 3) return msg(p, "/g załóż <nazwa> <tag>");

        String name = args[1];
        String tag = args[2];

        if (name.length() > 20) return msg(p, "Nazwa gildi nie może być dłuższa niż 30 znaków!");
        if (tag.length()  > 4)  return msg(p, "Tag nie może być dłuższy niż 4 znaki!");

        for (Map.Entry<Material, Integer> entry : Guild.upgradeCosts.get(0).t2.entrySet())
            if (!p.getInventory().contains(entry.getKey(), entry.getValue()))
                return msg(p, "Nie posiadasz wymaganych itemków aby założyć gildię. Wpisz /g itemy aby zobaczyć pełną liste potrzebnych itemów");

        if (Guild.fromName(name) != null) return msg(p, "Ta nazwa jest już zajęta");
        if (Guild.fromTag(tag)   != null) return msg(p, "Ten tag jest już zajęty");


        Location loc = p.getLocation().getBlock().getLocation().add(.5, 0, .5);

        RegionManager regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(loc.getWorld()));

        int max_dxz = Guild.upgradeCosts.get(Guild.upgradeCosts.size() - 1).t1;
        ProtectedCuboidRegion region_max = new ProtectedCuboidRegion(
                String.format("guild_%sx%sz_max", loc.getBlockX(), loc.getBlockZ()),
                getMinRegionPoint(loc, max_dxz),
                getMaxRegionPoint(loc, max_dxz)
        );

        if (!regions.getApplicableRegions(region_max).testState(null, GuildsAddons.flagGuildCreate))
            return msg(p, "Nie możesz założyć gildi w tym miejscu");

        region_max.setFlag(GuildsAddons.flagGuildCreate, StateFlag.State.DENY);
        region_max.setPriority(Guild.region_priority - 1);

        int start_dxz = Guild.upgradeCosts.get(Guild.upgradeCosts.size() - 1).t1;
        ProtectedCuboidRegion region = new ProtectedCuboidRegion(
                String.format("guild_%sx%sz", loc.getBlockX(), loc.getBlockZ()),
                getMinRegionPoint(loc, start_dxz),
                getMaxRegionPoint(loc, start_dxz)
        );


        long now = System.currentTimeMillis();
        guild = new Guild(name, tag, p.getName(), null, new ArrayList<>(), new ArrayList<>(),
                SerializableLocation.fromLocation(loc), region.getId(), loc.getWorld().getName(),
                false, 3, 1, now + 24*60*60*1000 /* 24h ochrony startowej*/, now);


        region.setFlag(Flags.GREET_MESSAGE, Guild.greetPrefix + guild.name);
        region.setFlag(Flags.FAREWELL_MESSAGE, "Opuszczasz teren gildi " + guild.name);

        region.setPriority(Guild.region_priority);

        DefaultDomain owners = new DefaultDomain();
        owners.addPlayer(p.getName());
        region.setOwners(owners);

        regions.addRegion(region);
        regions.addRegion(region_max);


        final Guild fguild = guild;
        Bukkit.getScheduler().runTask(GuildsAddons.getPlugin(), () -> {
            loc.setY(25);
            BiConsumer<Integer, Integer> wall = (x, z) -> {
                for (int ix=-1; ix <= 1; ix++) {
                    Location subLoc = loc.clone().add(ix * x, 0, ix * z);
                    for (int iy=0; iy < 4; iy++)
                        subLoc.add(0, 1, 0).getBlock().setType(Material.OBSIDIAN);
                }
            };


            Location subLoc = loc.clone();
            for (int x = -1; x <= 1; x++)
                for (int z = -1; z <= 1; z++)
                    for (int y = -1; y < 4; y++)
                        subLoc.clone().add(x, y, z).getBlock().setType(Material.AIR, false);

            loc.add(0, -1, 0).getBlock().setType(Material.OBSIDIAN);
            loc.add(0, -1, 0).getBlock().setType(Material.OBSIDIAN);
            loc.add(0, 0, 1).getBlock().setType(Material.OBSIDIAN);
            loc.add(0, 0, 1); wall.accept(1, 0);
            loc.add(1, 0, -1).getBlock().setType(Material.OBSIDIAN);
            loc.add(0, 0, -1).getBlock().setType(Material.OBSIDIAN);
            loc.add(1, 0, 0); wall.accept(0, 1);
            loc.add(-1, 0, -1).getBlock().setType(Material.OBSIDIAN);
            loc.add(-1, 0, 0).getBlock().setType(Material.OBSIDIAN);
            loc.add(0, 0, -1); wall.accept(1, 0);
            loc.add(-1, 0, 1).getBlock().setType(Material.OBSIDIAN);
            loc.add(0, 0, 1).getBlock().setType(Material.OBSIDIAN);
            loc.add(-1, 0, 0); wall.accept(0, 1);
            loc.add(0, 1, 0).getBlock().setType(Material.AIR);
            loc.add(0, 1, 0).getBlock().setType(Material.AIR);
            loc.add(0, -2, 0).getBlock().setType(Material.OBSIDIAN);
            loc.add(1, 0, 1).getBlock().setType(Material.OBSIDIAN);

            loc.add(0, 5, 0).getBlock().setType(Material.OBSIDIAN);
            loc.add(0, 0, -1).getBlock().setType(Material.OBSIDIAN);
            loc.add(0, 0, -1).getBlock().setType(Material.OBSIDIAN);
            loc.add(1, 0, 0).getBlock().setType(Material.OBSIDIAN);
            loc.add(0, 0, 1).getBlock().setType(Material.OBSIDIAN);
            loc.add(0, 0, 1).getBlock().setType(Material.OBSIDIAN);
            loc.add(1, 0, 0).getBlock().setType(Material.OBSIDIAN);
            loc.add(0, 0, -1).getBlock().setType(Material.OBSIDIAN);
            loc.add(0, 0, -1).getBlock().setType(Material.OBSIDIAN);

            fguild.respawnHeart();
        });

        Guild.upgradeCosts.get(0).t2.forEach((mat, amount) -> {
            PlayerInventory inv = p.getInventory();
            Iterator<? extends Map.Entry<Integer, ? extends ItemStack>> it = inv.all(mat).entrySet().iterator();

            while (amount > 0) {
                Map.Entry<Integer, ? extends ItemStack> entry = it.next();
                ItemStack item = entry.getValue();
                amount -= item.getAmount();
                if (amount < 0) {
                    item.setAmount(-amount);
                    inv.setItem(entry.getKey(), item);
                } else
                    inv.setItem(entry.getKey(), null);
            }
        });

        GuildStatements.createGuildQuery(guild);

        setGuild(p.getName(), guild);

        return guild.sendToMembers("Gildia utworzona na mocy %s", p.getDisplayName());
    }
    boolean usuń(Player p) {
        Guild guild = Guild.fromPlayer(p.getName());

        if (guild == null)                     return msg(p, "Nie posiadasz gildii");
        if (!p.getName().equals(guild.leader)) return msg(p, "Musisz być liderem gildi aby to zrobić");

        guild.delete();

        guild.sendToMembers("%s usunął gildię", p.getDisplayName());
        return true;
    }
    boolean opuść(Player p) {
        Guild guild = Guild.fromPlayer(p.getName());

        if (guild == null) return msg(p, "Nie posiadasz gildii");

        if (!guild.members.remove(p.getName()))
            if (p.getName().equals(guild.subLeader))
                guild.subLeader = null;
            else
                return msg(p, "Nie możesz opuścić własnej gildi");

        guild.sendToMembers("%s opuścił gildię", p.getDisplayName());

        setGuild(p.getName(), null);


        SectorServer.sendToServer("removeMember", "ALL", out -> {
            out.writeUTF(guild.region_world);
            out.writeUTF(guild.region);
            out.writeUTF(p.getName());
        });

        guild.save();
        return true;
    }
    boolean wyrzuć(Player p, String[] args) {
        if (args.length < 2)                       return msg(p, "/g wyrzuć <nick>");
        if (p.getName().equalsIgnoreCase(args[1])) return msg(p, "Nie możesz wyrzucić samego siebie");

        Guild guild = Guild.fromPlayer(p.getName());

        if (guild == null) return msg(p, "Nie posiadasz gildii");

        if (p.getName().equals(guild.leader) || p.getName().equals(guild.subLeader)) {
            if (args[1].equalsIgnoreCase(guild.subLeader)) {
                PlayersStatements.setGuild(guild.subLeader, null);
                guild.subLeader = null;
            } else {
                boolean deleted = false;
                for (int i=0; i < guild.members.size(); i++)
                    if (guild.members.get(i).equalsIgnoreCase(args[1])) {
                        args[1] = guild.members.get(1);

                        guild.sendToMembers("%s wyrzucił %s z gildi!", p.getDisplayName(), args[1]);
                        setGuildOnEveryServers(guild.members.remove(i), null);

                        deleted = true;

                        SectorServer.sendToServer("removeMember", "ALL", out -> {
                            out.writeUTF(guild.region_world);
                            out.writeUTF(guild.region);
                            out.writeUTF(args[1]);
                        });

                        break;
                    }
                if (!deleted)
                    return msg(p, "Niepoprawna nazwa gracza");
            }
            guild.save();
        } else
            return msg(p, "Nie możesz tego zrobić");
        return true;
    }

    // zapraszany: gildia
    static Map<String, Guild> inviteMap = new HashMap<>();
    boolean zaproś(Player p, String args[]) {
        if (args.length < 2) return msg(p, "/g zaproś <nick>");

        Guild guild = Guild.fromPlayer(p.getName());

        if (guild == null) return msg(p, "Nie posiadasz gildii");

        if (p.getName().equals(guild.leader) || p.getName().equals(guild.subLeader)) {
            int membersCount = guild.members.size() + 1 + (guild.subLeader == null ? 0 : 1);
            if (membersCount >= Guild.membersLimit)
                return msg(p, "Osiągnięto już limit członków gildi (" + Guild.membersLimit + ")");

            Player toInvite = Bukkit.getPlayer(args[1]);

            if (toInvite == null) return msg(p, "Niepoprawny gracz");
            if (guild.members.contains(toInvite.getName()) || toInvite.getName().equals(guild.leader) || toInvite.getName().equals(guild.subLeader))
                return msg(p, "Gracz należy już do twojej gildi");
            if (guild.equals(inviteMap.get(toInvite.getName())))            return msg(p, "Gracz już otrzymał zaproszenie do twojej gildi");
            if (PlayersStatements.getGuildName(toInvite.getName()) != null) return msg(p, "Gracz posiada już gildię");

            inviteMap.put(toInvite.getName(), guild);
            SectorServer.sendToServer("updateInviteMapPut", "ALL", out -> {
                out.writeUTF(toInvite.getName());
                out.writeUTF(guild.name);
            });

            final Guild fGuild = guild;
            Bukkit.getScheduler().runTaskLater(GuildsAddons.getPlugin(), () -> {
                if (inviteMap.remove(toInvite.getName(), fGuild)) {
                    SectorServer.sendToServer("updateInviteMapRemove2", "ALL", out -> {
                        out.writeUTF(toInvite.getName());
                        out.writeUTF(fGuild.name);
                    });
                    fGuild.sendToMembers("Zaproszenie do gildi dla gracza %s od %s wygasło", toInvite.getDisplayName(), p.getDisplayName());
                    toInvite.sendMessage(String.format("Zaproszenie do gildi %s od %s wygasło", fGuild.name, p.getDisplayName()));
                }
            }, 20 * 60 * 2);

            toInvite.sendMessage(String.format("Otrzymałeś zaproszenie od gildi %s od %s które wygaśnie za 2 minuty. Wpisz /g dołącz aby dołaczyć",
                    guild.name, p.getDisplayName()));
            return guild.sendToMembers("%s zaprosił %s do gildi, zaprosenie wygasnie za 2 minuty", p.getDisplayName(), toInvite.getDisplayName());
        } else
            return msg(p, "Nie możesz tego zrobić");
    }
    boolean dołącz(Player p) {
        Guild guild = Guild.fromPlayer(p.getName());

        if (guild != null)
            return msg(p, "Posiadasz już do gildię");

        guild = inviteMap.remove(p.getName());
        if (guild == null) return msg(p, "Nie masz żadnego zaproszenia do gildi");

        SectorServer.sendToServer("updateInviteMapRemove1", "ALL", out -> {
            out.writeUTF(p.getName());
        });

        int membersCount = guild.members.size() + 1 + (guild.subLeader == null ? 0 : 1);
        if (membersCount >= Guild.membersLimit)
            return msg(p, "Gildia " + guild.name + " osiągnieła już limit członków gildi (" + Guild.membersLimit + ")");

        guild.members.add(p.getName());
        setGuildOnEveryServers(p.getName(), guild);
        guild.save();

        Guild fguild = guild;
        SectorServer.sendToServer("addMember", "ALL", out -> {
            out.writeUTF(fguild.region_world);
            out.writeUTF(fguild.region);
            out.writeUTF(p.getName());
        });

        return guild.sendToMembers("%s dołączył do gildi", p.getDisplayName());
    }
    boolean zastępca(Player p, String[] args) {
        Guild guild = Guild.fromPlayer(p.getName());

        if (guild == null)                     return msg(p, "Nie posiadasz gildii");
        if (!guild.leader.equals(p.getName())) return msg(p, "Nie możesz tego zrobić");

        OfflinePlayer subleader = Bukkit.getOfflinePlayerIfCached(args[1]);

        if (subleader == null)                           return msg(p, "Gracz nigdy nie był online");
        if (subleader.getName().equals(guild.subLeader)) return msg(p, "Ten gracz jest już zastępcą");
        if (!guild.members.remove(subleader.getName()))  return msg(p, "Ten gracz nie należy do twojej gildi");
        if (p.getName().equals(subleader.getName()))     return msg(p, "Nie możesz być jednocześnie liderem i zastępcą gildi");

        if (guild.subLeader != null)
            guild.members.add(guild.subLeader);

        guild.subLeader = subleader.getName();
        guild.save();

        return guild.sendToMembers("%s wyznaczył %s na zastępce gildi", p.getDisplayName(), subleader.getName());
    }
    boolean ustawdom(Player p) {
        Guild guild = Guild.fromPlayer(p.getName());

        if (guild == null) return msg(p, "Nie posiadasz gildii");

        if (p.getName().equals(guild.leader) || p.getName().equals(guild.subLeader)) {
            guild.home = SerializableLocation.fromLocation(p.getLocation()); // TODO: sprawdzać czy stoi na terenie gildi
            guild.save();
        } else
            return msg(p, "Nie możesz tego zrobić");

        return guild.sendToMembers("%s wyznaczył nowy home gildi", p.getDisplayName());
    }
    boolean dom(Player p) {
        Guild guild = Guild.fromPlayer(p.getName());

        if (guild == null)      return msg(p, "Nie posiadasz gildi");
        if (guild.home == null) return msg(p, "Twoja gildia nie ma domu");

        final Guild fGuild = guild;
        Bukkit.getScheduler().runTask(GuildsAddons.getPlugin(), () -> Teleport.teleport(p, fGuild.home.toLocation()));
        return true;
    }
    boolean pvp(Player p) {
        Guild guild = Guild.fromPlayer(p.getName());

        if (guild == null) return msg(p, "Nie posiadasz gildii");

        if (p.getName().equals(guild.leader) || p.getName().equals(guild.subLeader)) {
            guild.pvp = !guild.pvp;
            guild.save();
            guild.sendToMembers("%s ustawił status pvp między członkami gildi na %s", p.getDisplayName(), guild.pvp ? "Włączony" : "Wyłączony");
        } else
            return msg(p, "Nie możesz tego zrobić");
        return true;
    }
    boolean itemy(Player p) {
        Guild guild = Guild.fromPlayer(p.getName());

        int lvl = guild == null ? 0 : guild.level;

        if (lvl >= Guild.upgradeCosts.size())
            return msg(p, "Osiągnięto już maksymalny poziom gildi");


        List<Pair<Material, Integer>> list = new ArrayList<>();
        Guild.upgradeCosts.get(lvl).t2.forEach((mat, amount) -> {
            while (amount > 0) {
                int count = Math.min(amount, 64);
                list.add(new Pair<>(mat, count));
                amount -= count;
            }
        });


        Inventory inv = Bukkit.createInventory(null, ((list.size() - 1) / 9 + 1) * 9, "Itemy na " + (guild == null ? "gildię" : "powiększenie gildi"));

        for (int i=0; i < inv.getSize(); i++)
            inv.setItem(i, StoneDrop.Holder.emptySlot);

        Iterator<Pair<Material, Integer>> it = list.iterator();
        for (int slot : GuildsAddons.slots(list.size(), inv.getSize() / 9)) {
            Pair<Material, Integer> pair = it.next();

            ItemStack item = new ItemStack(pair.t1);

            item.setAmount(pair.t2);

            ItemMeta meta = item.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
            item.setItemMeta(meta);

            inv.setItem(slot, item);
        }

        Bukkit.getScheduler().runTask(GuildsAddons.getPlugin(), () -> {
            p.openInventory(inv);
            p.addScoreboardTag(InventoryClickListener.scBlockTag);
        });
        return true;
    }
    // guild1name: guild2name
    static Map<String, String> allyInviteMap = new HashMap<>();
    boolean sojusz(Player p, String[] args) {
        if (args.length < 2) return msg(p, "/g sojusz [nazwa | tag | nick]");

        Guild guild = Guild.fromPlayer(p.getName());

        if (guild == null)                                                              return msg(p, "Nie posiadasz gildi");
        if (!(p.getName().equals(guild.leader) || p.getName().equals(guild.subLeader))) return msg(p, "Nie możesz tego zrobić");

        Guild guild2;

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(args[1]);
        if (offlinePlayer != null)
            guild2 = Guild.fromPlayer(offlinePlayer.getName());
        else if ((guild2 = Guild.fromTag(args[1])) == null)
            guild2 = Guild.fromName(args[1]);

        if (guild2 == null)                        return msg(p, "Niepoprawna gildia: " + args[0]);
        if (guild.alliances.contains(guild2.name)) return msg(p, "Już macie nawiązany sojusz z gildią: " + guild2.name);

        Guild fguild2 = guild2;

        if (allyInviteMap.remove(guild2.name, guild.name)) {
            SectorServer.sendToServer("updateAllyInviteMapRemove", "ALL", out -> {
                out.writeUTF(fguild2.name);
                out.writeUTF(guild.name);
            });

            guild .alliances.add(guild2.name);
            guild2.alliances.add(guild .name);
            guild.sendToMembers("%s przyjął zaproszenie do sojuszu z gildią %s", p.getDisplayName(), guild2.name);
            guild2.sendToMembers("Gildia %s przyjała propozycję sojuszu", guild.name);

            guild.save();
            guild2.save();
            return true;
        }

        allyInviteMap.put(guild.name, guild2.name);
        SectorServer.sendToServer("updateAllyInviteMapPut", "ALL", out -> {
            out.writeUTF(guild.name);
            out.writeUTF(fguild2.name);
        });

        guild.sendToMembers("%s wysłał propozycje sojuszu do gildi %s, która wygaśnie za 2 minuty", p.getDisplayName(), guild2.name);
        guild2.sendToMembers("Gildia %s wysłała do was propozycję sojuszu, która wygaśnie za 2 minuty", p.getDisplayName(), guild2.name);
        Bukkit.getScheduler().runTaskLater(GuildsAddons.getPlugin(), () -> {
            if (allyInviteMap.remove(guild.name, fguild2.name) && !guild.alliances.contains(fguild2.name)) {
                guild .sendToMembers("Zaproszenie do sojuszu dla gildi %s wygasło", fguild2.name);
                fguild2.sendToMembers("Zaproszenie do sojuszu od gildi %s wygasło", guild.name);
            }
        }, 20 * 60 * 2);

        return true;
    }
    boolean usuńsojusz(Player p, String[] args) {
        if (args.length < 2) return msg(p, "/g sojusz [nazwa | tag | nick]");

        Guild guild = Guild.fromPlayer(p.getName());

        if (guild == null)                                                              return msg(p, "Nie posiadasz gildi");
        if (!(p.getName().equals(guild.leader) || p.getName().equals(guild.subLeader))) return msg(p, "Nie możesz tego zrobić");

        Guild guild2;

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(args[1]);
        if (offlinePlayer != null)
            guild2 = Guild.fromPlayer(offlinePlayer.getName());
        else if ((guild2 = Guild.fromTag(args[1])) == null)
            guild2 = Guild.fromName(args[1]);

        if (guild2 == null)                         return msg(p, "Niepoprawna gildia: " + args[0]);
        if (!guild.alliances.contains(guild2.name)) return msg(p, "Nie macie nawiązanego sojuszu z gildią: " + guild2.name);

        guild .alliances.remove(guild2.name);
        guild2.alliances.remove(guild .name);
        guild.sendToMembers("%s zerwał sojusz z gildią %s", p.getDisplayName(), guild2.name);
        guild2.sendToMembers("Gildia %s zerwała sojuszu z wami!", guild.name);

        guild.save();
        guild2.save();

        return true;
    }
    boolean powiększ(Player p) {
        Guild guild = Guild.fromPlayer(p.getName());

        if (guild == null)                                                              return msg(p, "Nie posiadasz gildi");
        if (guild.level >= Guild.upgradeCosts.size())                                   return msg(p, "Osiągnięto już maksymalny poziom gildi");
        if (!(p.getName().equals(guild.leader) || p.getName().equals(guild.subLeader))) return msg(p, "Nie możesz tego zrobić");

        for (Map.Entry<Material, Integer> entry : Guild.upgradeCosts.get(guild.level).t2.entrySet())
            if (!p.getInventory().contains(entry.getKey(), entry.getValue()))
                return msg(p, "Nie posiadasz wymaganych itemków aby ulepszyć gildię. Wpisz /g itemy aby zobaczyć pełną liste potrzebnych itemów");

        Guild.upgradeCosts.get(guild.level).t2.forEach((mat, amount) -> {
            PlayerInventory inv = p.getInventory();
            Iterator<? extends Map.Entry<Integer, ? extends ItemStack>> it = inv.all(mat).entrySet().iterator();

            while (amount > 0) {
                Map.Entry<Integer, ? extends ItemStack> entry = it.next();
                ItemStack item = entry.getValue();
                amount -= item.getAmount();
                if (amount < 0) {
                    item.setAmount(-amount);
                    inv.setItem(entry.getKey(), item);
                } else
                    inv.setItem(entry.getKey(), null);
            }
        });

        guild.level += 1;

        guild.save();

        int dxz = Guild.upgradeCosts.get(guild.level - 1).t1;
        SectorServer.sendToServer("expandRegion", "ALL", out -> {
            out.writeUTF(guild.name);
            out.writeInt(dxz);
        });

        guild.sendToMembers("%s powiększył teren gildi", p.getDisplayName());

        return true;
    }
    boolean info(CommandSender sender, String[] args) {
        if (args.length < 2)
            return msg(sender, "/g info [nazwa | tag | nick]");

        Guild guild = Guild.fromName(args[1]);
        if (guild == null) guild = Guild.fromTag(args[1]);
        if (guild == null) guild = Guild.fromPlayer(args[1]);
        if (guild == null) return msg(sender, "Niepoprawna nazwa/tag gildi");

        sender.sendMessage(" ");
        sender.sendMessage(" ");
        sender.sendMessage("Gildia " + guild.name);
        sender.sendMessage("tag: " + guild.tag);
        sender.sendMessage(" ");
        sender.sendMessage("lider: " + guild.leader);
        if (guild.subLeader != null)
            sender.sendMessage("zastępca: " + guild.subLeader);
        if (!guild.members.isEmpty())
            sender.sendMessage("członkowie: " + guild.members);
        sender.sendMessage(" ");
        sender.sendMessage("życia: " + guild.hearts);
        sender.sendMessage("level: " + guild.level);
        sender.sendMessage("ochrona do: " + new SimpleDateFormat("MMM dd,yyyy HH:mm").format(new Date(guild.protect)));
        sender.sendMessage("data stworzenia: " + new SimpleDateFormat("MMM dd,yyyy HH:mm").format(new Date(guild.creation_date)));
        sender.sendMessage(" ");

        return true;
    }

    boolean pomoc(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Tej komendy może użyć tylko gracz");
            return true;
        }

        Guild guild = Guild.fromPlayer(sender.getName());

        if (guild != null) {
            sender.sendMessage("/g opuść - opuszcza gildię");
            if (guild.home != null)
                sender.sendMessage("/g dom - teleportuje na dom gildi");
            boolean leader = sender.getName().equals(guild.leader);
            if (leader || sender.getName().equals(guild.subLeader)) {
                sender.sendMessage("/g pvp - włącza/wyłącza pvp między członkami gildi");
                sender.sendMessage("/g zaproś <nick> - zaprasza gracza do gildi");
                sender.sendMessage("/g wyrzuć <nick> - wyrzuca gracza z gildi");
                sender.sendMessage("/g powiększ - powiększa teren gildi");
                sender.sendMessage("/g ustawhome - ustawia dom gildi");
                if (leader) {
                    sender.sendMessage("/g usuń - usuwa gildię");
                    sender.sendMessage("/g zastępca <nick> - wyznacza zastępce lidera");
                }
            }
        } else {
            sender.sendMessage("/g załóż <nazwa> <tag> - zakłada gildię");
            sender.sendMessage("/g itemy - wykaz potrzbnych itemów do założenia gildi");
            if (inviteMap.containsKey(sender.getName()))
                sender.sendMessage("/g dołącz - dołącza do gildi z zaproszenia");
        }
        return true;
    }
}
