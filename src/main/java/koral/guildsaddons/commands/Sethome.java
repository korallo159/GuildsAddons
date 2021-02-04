package koral.guildsaddons.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.database.statements.PlayersStatements;
import koral.guildsaddons.listeners.PluginChannelListener;
import koral.guildsaddons.model.Home;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Sethome implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if (command.getName().equalsIgnoreCase("sethome") && args.length == 1) {
                setHome(player, args[0]);
                sender.sendMessage("Ustawiono sethome" + args[0]);
            }
          else if(command.getName().equalsIgnoreCase("home") && args.length == 1){

            }
        }
        return true;
    }

    private void setHome(Player player, String arg){
        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray = null;
        try {
            jsonArray = (JSONArray) jsonParser.parse(PlayersStatements.getHomeData(player)); //TODO: ASYNC
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Home home = new Home(player.getName(), arg, player.getWorld().getName(), GuildsAddons.getPlugin().getConfig().getString("name"),player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
        Gson gson = new GsonBuilder().create();
        jsonArray.add(gson.toJsonTree(home));
        PlayersStatements.setHomeData(player, jsonArray.toJSONString()); //TODO ASYNC
    }

    private void teleportHome(Player player, String arg){
        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray = null;
        try {
            jsonArray = (JSONArray) jsonParser.parse(PlayersStatements.getHomeData(player)); //TODO: ASYNC
        } catch (ParseException e) {
            e.printStackTrace();
        }
        for(int i =0; i< jsonArray.size(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            if(jsonObject.get("homename").toString().replace("\"", "").equalsIgnoreCase(arg)){
               Home home = new Gson().fromJson(jsonObject.toJSONString(), Home.class);
               //wyslac do mapy on player join
               PluginChannelListener.connectAnotherServer(home.getServer(), player);
                break;
            }
            else player.sendMessage("Taki home nie istnieje!");

        }
    }
}
