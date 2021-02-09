package koral.guildsaddons.guilds;

import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.database.statements.GuildStatements;
import koral.guildsaddons.database.statements.PlayersStatements;
import koral.guildsaddons.util.SerializableLocation;
import koral.sectorserver.util.Teleport;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class GuildCommand implements TabExecutor {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return null;
    }
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
            Guild guild;

            switch (args[0].toLowerCase()) {
                case "załóż":
                case "zaloz":
                    guild = PlayersStatements.getGuild(p.getName());
                    if (guild != null)
                        return msg.test("Posiadasz już gildię");

                    if (args.length < 3)
                        return msg.test("/g załóż <nazwa> <tag>");

                    String name = args[1];
                    String tag = args[2];

                    if (name.length() > 20) return msg.test("Nazwa gildi nie może być dłuższa niż 30 znaków!");
                    if (tag.length()  > 4)  return msg.test("Tag nie może być dłuższy niż 4 znaki!");

                    if (Guild.fromName(name) != null) return msg.test("Ta nazwa jest już zajęta");
                    if (Guild.fromTag(tag)   != null) return msg.test("Ten tag jest już zajęty");

                    long now = System.currentTimeMillis();
                    guild = new Guild(name, tag, p.getName(), null, new ArrayList<>(), null, null, false, 3, 0,
                            now + 24*60*60*1000 /* 24h ochrony startowej*/, now);

                    GuildStatements.createGuildQuery(guild);

                    setGuild.accept(p.getName(), guild);

                    return guild.sendToMembers("Gildia utworzona na mocy %s", p.getDisplayName());
                case "usuń":
                case "usun":
                    guild = PlayersStatements.getGuild(p.getName());

                    if (guild == null)
                        return msg.test("Nie posiadasz gildii");

                    if (!p.getName().equals(guild.leader))
                        return msg.test("Musisz być liderem gildi aby to zrobić");

                    guild.sendToMembers("%s usunął gildię", p.getDisplayName());

                    Consumer<String> forget = nick -> setGuild.accept(nick, null);

                    forget.accept(guild.leader);
                    if (guild.subLeader != null) forget.accept(guild.subLeader);
                    guild.members.forEach(forget::accept);

                    GuildStatements.delete(guild);
                    break;
                case "opuść":
                case "opusc":
                    guild = PlayersStatements.getGuild(p.getName());

                    if (guild == null)
                        return msg.test("Nie posiadasz gildii");

                    if (!guild.members.remove(p.getName()))
                        if (p.getName().equals(guild.subLeader))
                            guild.subLeader = null;
                        else
                            return msg.test("Nie możesz opuścić własnej gildi");

                    guild.sendToMembers("%s opuścił gildię", p.getDisplayName());

                    setGuild.accept(p.getName(), null);

                    GuildStatements.updatadeData(guild);
                    break;
                case "wyrzuć":
                case "wyrzuc":
                    if (args.length < 2)
                        return msg.test("/g wyrzuć <nick>");

                    if (p.getName().equalsIgnoreCase(args[1]))
                        return msg.test("Nie możesz wyrzucić samego siebie");

                    guild = PlayersStatements.getGuild(p.getName());

                    if (guild == null)
                        return msg.test("Nie posiadasz gildii");

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
                                    break;
                                }
                            if (!deleted)
                                return msg.test("Niepoprawna nazwa gracza");
                        }
                        GuildStatements.updatadeData(guild);
                    } else
                        return msg.test("Nie możesz tego zrobić");
                    break;
                case "zastępca":
                case "zastepca":
                    guild = PlayersStatements.getGuild(p.getName());

                    if (guild == null)
                        return msg.test("Nie posiadasz gildii");

                    if (!guild.leader.equals(p.getName()))
                        return msg.test("Nie możesz tego zrobić");

                    if (guild.leader.equalsIgnoreCase(p.getName()))
                        return msg.test("Nie możesz być jednocześnie liderem i zastępcą gildi");

                    Player subleader = Bukkit.getPlayer(args[1]);

                    if (subleader == null)
                        return msg.test("Gracz nie jest online");

                    if (subleader.getName().equals(guild.subLeader))
                        return msg.test("Ten gracz jest już zastępcą");

                    if (!guild.members.remove(subleader.getName()))
                        return msg.test("Ten gracz nie należy do twojej gildi");

                    if (guild.subLeader != null)
                        guild.members.add(guild.subLeader);

                    guild.subLeader = subleader.getName();
                    return guild.sendToMembers("%s wyznaczył %s na zastępce gildi", p.getDisplayName(), subleader.getDisplayName());
                case "ustawhome":
                case "ustawdom":
                case "sethome":
                    guild = PlayersStatements.getGuild(p.getName());

                    if (guild == null)
                        return msg.test("Nie posiadasz gildii");

                    if (p.getName().equals(guild.leader) || p.getName().equals(guild.subLeader)) {
                        guild.home = SerializableLocation.fromLocation(p.getLocation()); // TODO: sprawdzać czy stoi na terenie gildi
                        GuildStatements.updatadeData(guild);
                    } else
                        return msg.test("Nie możesz tego zrobić");

                    return guild.sendToMembers("%s wyznaczył nowy home gildi", p.getDisplayName());
                case "dom":
                case "home":
                    guild = PlayersStatements.getGuild(p.getName());

                    if (guild == null)
                        return msg.test("Nie posiadasz gildi");

                    if (guild.home == null)
                        return msg.test("Twoja gildia nie ma domu");

                    final Guild fGuild = guild;
                    Bukkit.getScheduler().runTask(GuildsAddons.getPlugin(), () -> Teleport.teleport(p, fGuild.home.toLocation()));
                    break;
                case "pvp":
                    guild = PlayersStatements.getGuild(p.getName());

                    if (guild == null)
                        return msg.test("Nie posiadasz gildii");

                    if (p.getName().equals(guild.leader) || p.getName().equals(guild.subLeader)) {
                        guild.pvp = !guild.pvp;
                        GuildStatements.updatadeData(guild);
                        guild.sendToMembers("%s ustawił status pvp między członkami gildi na %s", p.getDisplayName(), guild.pvp ? "Włączony" : "Wyłączony");
                    } else
                        return msg.test("Nie możesz tego zrobić");

                    break;
                case "powiększ":
                case "powieksz":
                    break;//TODO: powiększ gildie
                case "itemy":
                    break;//TODO: itemy gildi
                default:
                    return info(sender);
            }

            return true;
        }).test(sender instanceof Player ? (Player) sender : null));

        return true;
    }

    boolean info(CommandSender sender) {
        return true; // TODO: info o gildi
    }
}
