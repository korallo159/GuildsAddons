package koral.guildsaddons.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.database.statements.PlayersStatements;
import koral.guildsaddons.model.Home;
import koral.sectorserver.util.Teleport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.List;

public class Sethome implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.plugin, () -> {
            Player player = null;
            if (sender instanceof Player)
                player = (Player) sender;

            String homeName = "home";
            if (args.length >= 1)
                homeName = args[0];

            if (command.getName().equalsIgnoreCase("sethome")) {
                setHome(player, homeName);
            } else if(command.getName().equalsIgnoreCase("home")){
                String owner = player.getName();
                if (sender.hasPermission("home.others") && homeName.contains(":")) {
                    owner = homeName.substring(0, homeName.indexOf(":"));
                    homeName = homeName.substring(homeName.indexOf(":") + 1);
                }
                teleportHome(player, owner, homeName);
            } else if (command.getName().equalsIgnoreCase("delhome")) {
                delHone(player, homeName);
            } else
                System.out.println("Problem z komendą " + label + " skontaktuj się z administratorem");
        });
        return true;
    }

    public static int getHomeLimit(Player player) {
        //TODO: Napisać
        return 3;
    }


    private void delHone(Player player, String homeName) {
        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray;
        try {
            jsonArray = (JSONArray) jsonParser.parse(PlayersStatements.getHomeData(player.getName())); //TODO: ASYNC
        } catch (ParseException e) {
            jsonArray = new JSONArray();
        }

        if (jsonArray.size() >= getHomeLimit(player)) {
            boolean was = false;
            for (int i=0; i < jsonArray.size(); i++) {
                JSONObject json = (JSONObject) jsonArray.get(i);
                if (json.get("homename").toString().replace("\"", "").equalsIgnoreCase(homeName)) {
                    jsonArray.remove(i);
                    PlayersStatements.setHomeData(player, jsonArray.toJSONString()); //TODO ASYNC
                    player.sendMessage("Usunięto home " + homeName);
                    return;
                }
            }
            if (!was) {
                player.sendMessage("Nie posiadasz takiego home");
                return;
            }
        }
    }
    private void setHome(Player player, String arg) {
        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray;
        try {
            jsonArray = (JSONArray) jsonParser.parse(PlayersStatements.getHomeData(player.getName())); //TODO: ASYNC
        } catch (ParseException e) {
            jsonArray = new JSONArray();
        }

        if (jsonArray.size() >= getHomeLimit(player)) {
            boolean was = false;
            for (int i=0; i < jsonArray.size(); i++) {
                JSONObject json = (JSONObject) jsonArray.get(i);
                if (json.get("homename").toString().replace("\"", "").equalsIgnoreCase(arg)) {
                    arg = json.get("homename").toString().replace("\"", "");
                    was = true;
                }
            }
            if (!was) {
                player.sendMessage("Nie możesz postawić kolejnego home");
                return;
            }
        }
        Location loc = player.getLocation();
        Home home = new Home(player.getName(), arg, player.getWorld().getName(), GuildsAddons.getPlugin().getConfig().getString("homename"),
                loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        Gson gson = new GsonBuilder().create();
        jsonArray.add(gson.toJsonTree(home));
        PlayersStatements.setHomeData(player, jsonArray.toJSONString()); //TODO ASYNC
        player.sendMessage("Ustawiono home " + arg);
    }

    private boolean send(CommandSender sender, String msg) {sender.sendMessage(msg); return true;}
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

        for(int i =0; i< jsonArray.size(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            if(jsonObject.get("homename").toString().replace("\"", "").equalsIgnoreCase(homeName)){
               Home home = new Gson().fromJson(jsonObject.toJSONString(), Home.class);
               player.sendMessage("Teleportowanie na home " + homeName);
               Bukkit.getScheduler().runTask(GuildsAddons.plugin, () -> Teleport.teleport(player, home.getLocation()));
               return true;
            }
        }

        return send(player, "Niepoprawna nazwa home: " + homeName);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
