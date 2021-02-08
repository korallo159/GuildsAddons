package koral.guildsaddons.commands;

import com.google.gson.Gson;
import koral.guildsaddons.database.statements.PlayersStatements;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.*;

public class Is implements TabExecutor, Listener {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("isadmin")) {
            addToPlayerItemShop(args[0], args[1], Integer.valueOf(args[2]));
        }
        if (command.getName().equalsIgnoreCase("itemshop")) {
            refreshPlayerItemShop(sender.getName());
            ((Player) sender).openInventory(inventoryMap.get(sender.getName()));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    //TODO: jezeli ktos zakupi ten sam item to musi mu zwiekszyc ilosc.
    private void addToPlayerItemShop(String playername, String item, int amount) {

        JSONObject jsonObject = null;
        String data = PlayersStatements.getItemShopData(playername);
        JSONParser jsonParser = new JSONParser();
        HashMap<String, Object> itemsMap = null;
        if (!data.isEmpty()) {
            try {
                jsonObject = (JSONObject) new JSONParser().parse(data);
                itemsMap = new Gson().fromJson(jsonObject.toString(), HashMap.class);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Double ilosc = (Double) itemsMap.get(item);

            itemsMap.put(item, amount + ilosc.intValue());
            jsonObject = new JSONObject(itemsMap);
            PlayersStatements.setItemShopData(playername, jsonObject.toJSONString());
        } else {
            itemsMap = new HashMap<>();
            itemsMap.put(item, amount);
            jsonObject = new JSONObject(itemsMap);
            PlayersStatements.setItemShopData(playername, jsonObject.toJSONString());
        }
    }

    private void setToPlayerItemShop(String playername, String item, int amount) {

        JSONObject jsonObject = null;
        String data = PlayersStatements.getItemShopData(playername);
        JSONParser jsonParser = new JSONParser();
        HashMap<String, Object> itemsMap = null;
        if (!data.isEmpty()) {
            try {
                jsonObject = (JSONObject) new JSONParser().parse(data);
                itemsMap = new Gson().fromJson(jsonObject.toString(), HashMap.class);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            itemsMap.put(item, amount);
            jsonObject = new JSONObject(itemsMap);
            PlayersStatements.setItemShopData(playername, jsonObject.toJSONString());
        } else {
            itemsMap = new HashMap<>();
            itemsMap.put(item, amount);
            jsonObject = new JSONObject(itemsMap);
            PlayersStatements.setItemShopData(playername, jsonObject.toJSONString());
        }
    }

    //TODO: jezeli jest pusty json.
    public static HashMap<String, Inventory> inventoryMap = new HashMap<>();
    //TODO: jezeli jest pusty json jest NULL, avoid this bro
    private void refreshPlayerItemShop(String playername) {
        Is.inventoryMap.put(playername, Bukkit.getServer().createInventory(null, 27, "ITEMSHOP"));
        JSONObject jsonObject = null;
        String data = PlayersStatements.getItemShopData(playername);
        HashMap<String, Object> itemsMap = null;
        try {
            jsonObject = (JSONObject) new JSONParser().parse(data);
            itemsMap = new Gson().fromJson(jsonObject.toString(), HashMap.class);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (itemsMap.containsKey("turbodrop")) {
            for (int i = 0; i < inventoryMap.get(playername).getSize(); i++)
                if (inventoryMap.get(playername).getItem(i) == null) {
                    ItemStack item = turboDrop();
                    Double amount = (Double) itemsMap.get("turbodrop");
                    item.setAmount(amount.intValue());
                    inventoryMap.get(playername).setItem(i, item);
                    break;
                }
        }

    }

    public ItemStack turboDrop(){
        ItemStack is = new ItemStack(Material.BOOK);
        ItemMeta itemMeta = is.getItemMeta();
        itemMeta.setDisplayName("§4TurboDrop");
        List<String> lore = new ArrayList<>();
        lore.add("§ePPM aby zużyć turbodrop");
        itemMeta.setLore(lore);
        is.setItemMeta(itemMeta);
        return is;
    }

    //TODO wkladanie dodaje do listy, przez co mozna przechowywac tam rzeczy.
    @EventHandler
    public void onPlayerInventoryClick(InventoryClickEvent ev) {
        Player player = (Player) ev.getWhoClicked();
        if (ev.getInventory().equals(inventoryMap.get(player.getName()))) {
                if(ev.getCurrentItem() != null && !ev.getCurrentItem().isSimilar(turboDrop()))
            ev.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent ev){
        Player player = (Player) ev.getPlayer();
        if(ev.getInventory().equals(inventoryMap.get(player.getName()))){
         Inventory inv = inventoryMap.get(player.getName());
         int amountOfItems = 0;
         if(inv.containsAtLeast(turboDrop(), 1)){
             for(int i =0; i<inv.getSize(); i++){
                 ItemStack it = inv.getItem(i);
                 if(it != null && it.isSimilar(turboDrop())){
                   amountOfItems = amountOfItems + it.getAmount();
                 }
             }
         }
        setToPlayerItemShop(player.getName(), "turbodrop", amountOfItems);
       }
    }

}
  /*      List<String> commandsList = new ArrayList<>();
        String[] s = PlayersStatements.getItemShopData(playername).split(",");
       Arrays.asList(s).forEach(value -> {
           if(!value.isEmpty())
           commandsList.add(value);
       });
      commandsList.add(komenda);
      String[] correct = commandsList.toArray(new String[0]);
      PlayersStatements.setItemShopData(playername, String.join(",", correct));
    }

   */