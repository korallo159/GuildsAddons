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
import koral.sectorserver.util.Teleport;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
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
import java.util.function.Consumer;
import java.util.function.Predicate;

public class GuildCommand implements TabExecutor {
    static Map<Material, Integer> guildItems = new HashMap<>();

    public static void reloadGuildItems() {
        ConfigManager config = new ConfigManager("guild items.yml");

        guildItems.clear();
        config.config.getValues(false).forEach(
                (mat, amount) -> guildItems.put(Material.valueOf(mat.toUpperCase().replace(' ', '_')), (int) amount));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length <= 1 && sender instanceof Player) {
            List<String> list = new ArrayList<>();

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


            List<String> result = new ArrayList<>();
            for (String str : list)
                if (args.length == 0 || str.startsWith(args[args.length - 1].toLowerCase()))
                    result.add(str);

            return result;
        }
        return null;
    }

    private Map<String, Guild> inviteMap = new HashMap<>();
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1)
            return info(sender);

        Predicate<String> msg = message -> {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        };
        BiConsumer<String, Guild> setGuild = (playerName, guild) -> {
          PlayersStatements.setGuild(playerName, guild);
          playerName = playerName.toLowerCase();
          if (Guild.fromPlayer.containsKey(playerName))
              Guild.fromPlayer.put(playerName, guild);
        };

        Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.getPlugin(), () -> ((Predicate<Player>) p -> {
            RegionManager regions;
            DefaultDomain members;
            Guild guild;

            switch (args[0].toLowerCase()) {
                case "załóż":
                case "zaloz":
                    guild = Guild.fromPlayer(p.getName());
                    if (guild != null) return msg.test("Posiadasz już gildię");
                    if (args.length < 3) return msg.test("/g załóż <nazwa> <tag>");

                    String name = args[1];
                    String tag = args[2];

                    if (name.length() > 20) return msg.test("Nazwa gildi nie może być dłuższa niż 30 znaków!");
                    if (tag.length()  > 4)  return msg.test("Tag nie może być dłuższy niż 4 znaki!");

                    for (Map.Entry<Material, Integer> entry : guildItems.entrySet())
                        if (!p.getInventory().contains(entry.getKey(), entry.getValue()))
                            return msg.test("Nie posiadasz wymaganych itemków aby założyć gildię. Wpisz /g itemy aby zobaczyć pełną liste potrzebnych itemów");

                    if (Guild.fromName(name) != null) return msg.test("Ta nazwa jest już zajęta");
                    if (Guild.fromTag(tag)   != null) return msg.test("Ten tag jest już zajęty");


                    Location loc = p.getLocation();

                    regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(loc.getWorld()));

                    ProtectedCuboidRegion region_max = new ProtectedCuboidRegion(
                            String.format("guild_%sx%sz_max", loc.getBlockX(), loc.getBlockZ()),
                            BlockVector3.at((int) (loc.getBlockX() - Guild.max_region_dxz / 2d), 0,   (int) (loc.getBlockZ() - Guild.max_region_dxz / 2d)),
                            BlockVector3.at((int) (loc.getBlockX() + Guild.max_region_dxz / 2d), 256, (int) (loc.getBlockZ() + Guild.max_region_dxz / 2d))
                    );

                    if (!regions.getApplicableRegions(region_max).testState(null, GuildsAddons.flagGuildCreate))
                        return msg.test("Nie możesz założyć gildi w tym miejscu");


                    region_max.setFlag(GuildsAddons.flagGuildCreate, StateFlag.State.DENY);
                    region_max.setPriority(Guild.region_priority - 1);

                    ProtectedCuboidRegion region = new ProtectedCuboidRegion(
                            String.format("guild_%sx%sz", loc.getBlockX(), loc.getBlockZ()),
                            BlockVector3.at((int) (loc.getBlockX() - Guild.start_region_dxz / 2d), 0,   (int) (loc.getBlockZ() - Guild.start_region_dxz / 2d)),
                            BlockVector3.at((int) (loc.getBlockX() + Guild.start_region_dxz / 2d), 256, (int) (loc.getBlockZ() + Guild.start_region_dxz / 2d))
                    );

                    region.setFlag(Flags.GREET_MESSAGE, "Wszedłeś na teren gildi " + guild.name);
                    region.setFlag(Flags.FAREWELL_MESSAGE, "Opuszczasz teren gildi " + guild.name);

                    region.setPriority(Guild.region_priority);

                    DefaultDomain owners = new DefaultDomain();
                    owners.addPlayer(p.getName());
                    region.setOwners(owners);

                    regions.addRegion(region);
                    regions.addRegion(region_max);


                    guildItems.forEach((mat, amount) -> {
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

                    long now = System.currentTimeMillis();
                    guild = new Guild(name, tag, p.getName(), null, new ArrayList<>(), SerializableLocation.fromLocation(loc),
                            region.getId(), loc.getWorld().getName(), false, 3, 0,
                            now + 24*60*60*1000 /* 24h ochrony startowej*/, now);

                    GuildStatements.createGuildQuery(guild);

                    setGuild.accept(p.getName(), guild);

                    return guild.sendToMembers("Gildia utworzona na mocy %s", p.getDisplayName());
                case "usuń":
                case "usun"://TODO: potwierdzenie
                    guild = Guild.fromPlayer(p.getName());

                    if (guild == null)                     return msg.test("Nie posiadasz gildii");
                    if (!p.getName().equals(guild.leader)) return msg.test("Musisz być liderem gildi aby to zrobić");

                    guild.sendToMembers("%s usunął gildię", p.getDisplayName());

                    Consumer<String> forget = nick -> setGuild.accept(nick, null);

                    forget.accept(guild.leader);
                    if (guild.subLeader != null) forget.accept(guild.subLeader);
                    guild.members.forEach(forget::accept);

                    final String finalGuildName = guild.name;
                    GuildsAddons.sendPluginMessageForward("ALL", "deleteGuild", out -> out.writeUTF(finalGuildName));

                    Guild.fromName.remove(guild.name);
                    Guild.fromTag.remove(guild.tag);

                    GuildStatements.delete(guild);

                    regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(Bukkit.getWorld(guild.region_world)));
                    regions.removeRegion(guild.region + "_max");
                    regions.removeRegion(guild.region);

                    break;
                case "opuść":
                case "opusc":
                    guild = Guild.fromPlayer(p.getName());

                    if (guild == null) return msg.test("Nie posiadasz gildii");

                    if (!guild.members.remove(p.getName()))
                        if (p.getName().equals(guild.subLeader))
                            guild.subLeader = null;
                        else
                            return msg.test("Nie możesz opuścić własnej gildi");

                    guild.sendToMembers("%s opuścił gildię", p.getDisplayName());

                    setGuild.accept(p.getName(), null);

                    region = guild.getRegion();
                    members = region.getMembers();
                    members.removePlayer(p.getName());
                    region.setMembers(members);

                    guild.save();
                    break;
                case "wyrzuć":
                case "wyrzuc":
                    if (args.length < 2)                       return msg.test("/g wyrzuć <nick>");
                    if (p.getName().equalsIgnoreCase(args[1])) return msg.test("Nie możesz wyrzucić samego siebie");

                    guild = Guild.fromPlayer(p.getName());

                    if (guild == null) return msg.test("Nie posiadasz gildii");

                    if (p.getName().equals(guild.leader) || p.getName().equals(guild.subLeader)) {
                        if (args[1].equalsIgnoreCase(guild.subLeader)) {
                            PlayersStatements.setGuild(guild.subLeader, null);
                            guild.subLeader = null;
                        } else {
                            boolean deleted = false;
                            for (int i=0; i < guild.members.size(); i++)
                                if (guild.members.get(i).equalsIgnoreCase(args[1])) {
                                    guild.sendToMembers("%s wyrzucił %s z gildi!", p.getDisplayName(), args[1]);
                                    setGuild.accept(guild.members.remove(i), null);
                                    deleted = true;

                                    region = guild.getRegion();
                                    members = region.getMembers();
                                    members.removePlayer(args[i]);
                                    region.setMembers(members);
                                    break;
                                }
                            if (!deleted)
                                return msg.test("Niepoprawna nazwa gracza");
                        }
                        guild.save();
                    } else
                        return msg.test("Nie możesz tego zrobić");
                    break;
                case "zaproś":
                case "zapros":
                case "dodaj":
                case "invite":
                    if (args.length < 2) return msg.test("/g zaproś <nick>");

                    guild = Guild.fromPlayer(p.getName());

                    if (guild == null) return msg.test("Nie posiadasz gildii");

                    if (p.getName().equals(guild.leader) || p.getName().equals(guild.subLeader)) {
                        int membersCount = guild.members.size() + 1 + (guild.subLeader == null ? 0 : 1);
                        if (membersCount >= Guild.membersLimit)
                            return msg.test("Osiągnięto już limit członków gildi (" + Guild.membersLimit + ")");

                        Player toInvite = Bukkit.getPlayer(args[1]);
                        if (toInvite == null) return msg.test("Niepoprawny gracz");
                        if (guild.members.contains(toInvite.getName()) || toInvite.getName().equals(guild.leader) || toInvite.getName().equals(guild.subLeader))
                            return msg.test("Gracz należy już do twojej gildi");
                        if (guild.equals(inviteMap.get(toInvite.getName())))            return msg.test("Gracz już otrzymał zaproszenie do twojej gildi");
                        if (PlayersStatements.getGuildName(toInvite.getName()) != null) return msg.test("Gracz posiada już gildię");

                        inviteMap.put(toInvite.getName(), guild);

                        final Guild fGuild = guild;
                        Bukkit.getScheduler().runTaskLater(GuildsAddons.getPlugin(), () -> {
                            if (inviteMap.remove(toInvite.getName(), fGuild)) {
                                fGuild.sendToMembers("Zaproszenie do gildi dla gracza %s od %s wygasło", toInvite.getDisplayName(), p.getDisplayName());
                                toInvite.sendMessage(String.format("Zaproszenie do gildi %s od %s wygasło", fGuild.name, p.getDisplayName()));
                            }
                         }, 20 * 60 * 2);

                        toInvite.sendMessage(String.format("Otrzymałeś zaproszenie od gildi %s od %s które wygaśnie za 2 minuty. Wpisz /g dołącz aby dołaczyć",
                                guild.name, p.getDisplayName()));
                        return guild.sendToMembers("%s zaprosił %s do gildi, zaprosenie wygasnie za 2 minuty", p.getDisplayName(), toInvite.getDisplayName());
                    } else
                        return msg.test("Nie możesz tego zrobić");
                case "dołącz":
                case "dolacz":
                case "join":
                    guild = inviteMap.remove(p.getName());
                    if (guild == null) return msg.test("Nie masz żadnego zaproszenia do gildi");

                    int membersCount = guild.members.size() + 1 + (guild.subLeader == null ? 0 : 1);
                    if (membersCount >= Guild.membersLimit)
                        return msg.test("Gildia " + guild.name + " osiągnieła już limit członków gildi (" + Guild.membersLimit + ")");

                    guild.members.add(p.getName());
                    setGuild.accept(p.getName(), guild);
                    guild.save();

                    region = guild.getRegion();
                    members = region.getMembers();
                    members.removePlayer(p.getName());
                    region.setMembers(members);

                    return guild.sendToMembers("%s dołączył do gildi", p.getDisplayName());
                case "zastępca":
                case "zastepca":
                    guild = Guild.fromPlayer(p.getName());

                    if (guild == null)                     return msg.test("Nie posiadasz gildii");
                    if (!guild.leader.equals(p.getName())) return msg.test("Nie możesz tego zrobić");

                    Player subleader = Bukkit.getPlayer(args[1]);

                    if (subleader == null)                           return msg.test("Gracz nie jest online");
                    if (subleader.getName().equals(guild.subLeader)) return msg.test("Ten gracz jest już zastępcą");
                    if (!guild.members.remove(subleader.getName()))  return msg.test("Ten gracz nie należy do twojej gildi");
                    if (p.getName().equals(subleader.getName()))     return msg.test("Nie możesz być jednocześnie liderem i zastępcą gildi");

                    if (guild.subLeader != null)
                        guild.members.add(guild.subLeader);

                    guild.subLeader = subleader.getName();
                    guild.save();

                    return guild.sendToMembers("%s wyznaczył %s na zastępce gildi", p.getDisplayName(), subleader.getDisplayName());
                case "ustawhome":
                case "ustawdom":
                case "sethome":
                    guild = Guild.fromPlayer(p.getName());

                    if (guild == null) return msg.test("Nie posiadasz gildii");

                    if (p.getName().equals(guild.leader) || p.getName().equals(guild.subLeader)) {
                        guild.home = SerializableLocation.fromLocation(p.getLocation()); // TODO: sprawdzać czy stoi na terenie gildi
                        guild.save();
                    } else
                        return msg.test("Nie możesz tego zrobić");

                    return guild.sendToMembers("%s wyznaczył nowy home gildi", p.getDisplayName());
                case "dom":
                case "home":
                    guild = Guild.fromPlayer(p.getName());

                    if (guild == null)      return msg.test("Nie posiadasz gildi");
                    if (guild.home == null) return msg.test("Twoja gildia nie ma domu");

                    final Guild fGuild = guild;
                    Bukkit.getScheduler().runTask(GuildsAddons.getPlugin(), () -> Teleport.teleport(p, fGuild.home.toLocation()));
                    break;
                case "pvp":
                    guild = Guild.fromPlayer(p.getName());

                    if (guild == null) return msg.test("Nie posiadasz gildii");

                    if (p.getName().equals(guild.leader) || p.getName().equals(guild.subLeader)) {
                        guild.pvp = !guild.pvp;
                        guild.save();
                        guild.sendToMembers("%s ustawił status pvp między członkami gildi na %s", p.getDisplayName(), guild.pvp ? "Włączony" : "Wyłączony");
                    } else
                        return msg.test("Nie możesz tego zrobić");

                    break;
                case "itemy":
                    List<Pair<Material, Integer>> list = new ArrayList<>();
                    guildItems.forEach((mat, amount) -> {
                        while (amount > 0) {
                            int count = Math.min(amount, 64);
                            list.add(new Pair<>(mat, count));
                            amount -= count;
                        }
                    });


                    Inventory inv = Bukkit.createInventory(null, ((list.size() - 1) / 9 + 1) * 9, "Itemy na gildię");

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
                    break;
                case "powiększ":
                case "powieksz":
                    break;//TODO: powiększ gildie
                case "info":
                    if (args.length < 2)
                        return msg.test("/g info [nazwa | tag | nick]");

                    guild = Guild.fromName(args[1]);
                    if (guild == null) guild = Guild.fromTag(args[1]);
                    if (guild == null) guild = Guild.fromPlayer(args[1]);
                    if (guild == null) return msg.test("Niepoprawna nazwa/tag gildi");

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

                    break;
                default:
                    return info(sender);
            }

            return true;
        }).test(sender instanceof Player ? (Player) sender : null));

        return true;
    }

    boolean info(CommandSender sender) {
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
