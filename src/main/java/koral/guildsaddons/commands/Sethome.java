package koral.guildsaddons.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.database.statements.PlayersStatements;
import koral.guildsaddons.model.Home;
import koral.sectorserver.util.Teleport;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sethome implements CommandExecutor, TabExecutor {
    public static HashMap<String, List<String>> homeMap = new HashMap<>();


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.plugin, () -> {
            Player player = null;
            if (sender instanceof Player)
                player = (Player) sender;

            String homeName = "home";
            if (args.length >= 1)
                homeName = args[0];

            switch (command.getName().toLowerCase()) {
                case "sethome":
                    setHome(player, homeName);
                    break;
                case "homes":
                    List<String> homes = homeMap.get(player.getName());
                    if (homes == null)
                        homes = new ArrayList<>();
                    sender.sendMessage(ChatColor.GREEN + "Lista twoich home: " + ChatColor.LIGHT_PURPLE + homes);
                    break;
                case "home":
                    String owner = player.getName();
                    if(homeMap.containsKey(player.getName()) && homeMap.get(player.getName()).contains(homeName))
                        homeTimer(player, owner, homeName, player.getLocation(), 10);
                    else sender.sendMessage(new TextComponent(ChatColor.RED + "Nie masz takiego home!"));
                    break;
                case "delhome":
                    delHome(player, homeName);
                    break;
                default:
                    System.out.println("Problem z komendą " + label + " skontaktuj się z administratorem");
                    break;
            }
        });
        return true;
    }

    public static int getHomeLimit(Player player) {
        AtomicInteger limit = new AtomicInteger(3);
        Pattern pattern = Pattern.compile("guildsaddons\\.home\\.limit\\.(\\d+)");
        player.getEffectivePermissions().forEach(perm -> {
            Matcher matcher;
            if (perm.getValue() && (matcher = pattern.matcher(perm.getPermission())).matches())
                limit.set(Math.max(limit.get(), Integer.parseInt(matcher.group(1))));
        });
        return limit.get();
    }


    private void delHome(Player player, String homeName) {
        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray;
        try {
            jsonArray = (JSONArray) jsonParser.parse(PlayersStatements.getHomeData(player.getName())); //TODO: ASYNC
        } catch (ParseException e) {
            jsonArray = new JSONArray();
        }

            boolean was = false;
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject json = (JSONObject) jsonArray.get(i);
                if (json.get("homename").toString().replace("\"", "").equalsIgnoreCase(homeName)) {
                    jsonArray.remove(i);
                    PlayersStatements.setHomeData(player, jsonArray.toJSONString()); //TODO ASYNC
                    player.sendMessage("§cUsunięto home " + homeName);
                    Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.getPlugin(), () -> homesCompleterGet(player));
                    return;
                }
            }
            if (!was) {
                player.sendMessage("§c Nie posiadasz takiego home");
                return;
            }

    }

    private void setHome(Player player, String homeName) {
        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray;
        try {
            jsonArray = (JSONArray) jsonParser.parse(PlayersStatements.getHomeData(player.getName()));
        } catch (ParseException e) {
            jsonArray = new JSONArray();
        }

        Location loc = player.getLocation();
        Gson gson = new GsonBuilder().create();

        for (int i = jsonArray.size() - 1; i >= 0; i--) {
            Home old_home = new Gson().fromJson(((JSONObject) jsonArray.get(i)).toJSONString(), Home.class);
            if (old_home.getHomename().equals(homeName))
                jsonArray.remove(i);
        }

        if (jsonArray.size() >= getHomeLimit(player)) {
            player.sendMessage("§c Nie możesz postawić kolejnego home, wykorzystałeś maksymalną ilość /home dla twojej rangi");
            return;
        }

        Home new_home = new Home(player.getName(), homeName, player.getWorld().getName(), GuildsAddons.getPlugin().getConfig().getString("homename"),
                loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

        jsonArray.add(gson.toJsonTree(new_home));
        PlayersStatements.setHomeData(player, jsonArray.toJSONString());
        player.sendMessage("§aUstawiono home " + homeName);
        Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.getPlugin(), () -> homesCompleterGet(player));
    }

    private boolean send(CommandSender sender, String msg) {
        sender.sendMessage(msg);
        return true;
    }

    private boolean teleportHome(Player player, String owner, String homeName) {
        if (homeName.contains(":") || homeName.isEmpty())
            return send(player, "Niepoprawna nazwa home: " + homeName);

        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray;
        try {
            jsonArray = (JSONArray) jsonParser.parse(PlayersStatements.getHomeData(owner)); //TODO: ASYNC
        } catch (ParseException | NullPointerException e) {
            return send(player, "Nie masz żadnego home");
        }

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            if (jsonObject.get("homename").toString().replace("\"", "").equalsIgnoreCase(homeName)) {
                Home home = new Gson().fromJson(jsonObject.toJSONString(), Home.class);
                Bukkit.getScheduler().runTask(GuildsAddons.plugin, () -> Teleport.teleport(player, home.getLocation()));
                return true;
            }
        }

        return send(player, "Niepoprawna nazwa home: " + homeName);
    }

    public static void homesCompleterGet(Player player){
        List<String> homes = new ArrayList<>();
        JSONArray jsonArray = null;
        JSONParser jsonParser = new JSONParser();
        homeMap.put(player.getName(), null);
        try {
            jsonArray = (JSONArray) jsonParser.parse(PlayersStatements.getHomeData(player.getName())); //TODO: ASYNC
            for(int i =0; i< jsonArray.size(); i++){
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                homes.add(jsonObject.get("homename").toString().replace("\"", ""));
                homeMap.put(player.getName(), homes);
            }
        } catch (ParseException | NullPointerException e) {

        }

    }
    private void homeTimer(Player player, String owner, String homename, Location lastLocation, int secondsLeft) {
        if (player.getLocation().distance(lastLocation) > 1) {
            player.sendMessage("§cPoruszyłeś się! Teleportacja anulowana.");
            player.removePotionEffect(PotionEffectType.CONFUSION);
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        } else if (secondsLeft <= 0) {
            teleportHome(player, owner, homename);
        } else {
            player.sendTitle("§5§lTeleportacja", "§9Nie ruszaj się, za §a " + secondsLeft + "s §9 zostaniesz przeteleportowany", 0, 22, 5);
            Bukkit.getScheduler().runTaskLater(GuildsAddons.plugin, () -> homeTimer(player, owner, homename, lastLocation,secondsLeft - 1), 20);
        }
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {


        return homeMap.get(sender.getName());
    }
}
