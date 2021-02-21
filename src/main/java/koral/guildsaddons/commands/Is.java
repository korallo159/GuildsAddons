package koral.guildsaddons.commands;

import com.google.gson.Gson;
import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.database.statements.PlayersStatements;
import koral.guildsaddons.managers.ConfigManager;
import koral.guildsaddons.util.Cooldowns;
import org.bukkit.Bukkit;
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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Is implements TabExecutor, Listener {

    public static HashMap<String, Inventory> inventoryMap = new HashMap<>();
    ConfigManager config = new ConfigManager("itemshop.yml");
    static Cooldowns cooldowns = new Cooldowns(new HashMap<>());
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("isadmin")) {
            for (String s : config.getConfig().getKeys(false))
                if (s.equals(args[1])) {
                    AsyncaddToPlayerItemShop(args[0], args[1], Integer.valueOf(args[2]));
                    sender.sendMessage("Pomyslnie dodano przedmiot do itemshopu gracza");
                }
        }
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if(command.getName().equalsIgnoreCase("isedit")){
                switch(args[0]){
                    case"add":
                        config.getConfig().set(args[1] + ".item", player.getInventory().getItemInMainHand());
                        config.save();
                        player.sendMessage("Dodałeś do configu item");
                        break;
                    case"delete":
                        config.getConfig().set(args[1] + ".item", null);
                        config.save();
                        player.sendMessage("usunales z configu item.");
                        break;
                }
            }
            if (command.getName().equalsIgnoreCase("itemshop")) {
                if(!cooldowns.hasCooldown(player, 5, "Nie mozesz tego jeszcze wykonać")) {
                    openPlayerItemShop(sender.getName());
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    if(command.getName().equalsIgnoreCase("isadmin")) {
        List<String> list = new ArrayList<>();
        for (String s : config.config.getKeys(false)) {
            list.add(s);
        }
        if (args.length == 2) {
            return list;
        }
    }

        return null;
    }

    private void AsyncaddToPlayerItemShop(String playername, String item, int amount) {
        Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.getPlugin(), () -> {
        JSONObject jsonObject;
        String data = PlayersStatements.getItemShopData(playername);
        HashMap<String, Object> itemsMap = null;
        if (!data.isEmpty()) {
            try {
                jsonObject = (JSONObject) new JSONParser().parse(data);
                itemsMap = new Gson().fromJson(jsonObject.toString(), HashMap.class);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (itemsMap.containsKey(item)) {
                Double ilosc = (Double) itemsMap.get(item);
                itemsMap.put(item, amount + ilosc.intValue());
                jsonObject = new JSONObject(itemsMap);
                PlayersStatements.setItemShopData(playername, jsonObject.toJSONString());
            } else {
                itemsMap.put(item, amount);
                jsonObject = new JSONObject(itemsMap);
                PlayersStatements.setItemShopData(playername, jsonObject.toJSONString());
               }

        } else {
            itemsMap = new HashMap<>();
            itemsMap.put(item, amount);
            jsonObject = new JSONObject(itemsMap);
            PlayersStatements.setItemShopData(playername, jsonObject.toJSONString());
             }
         });
    }

    private void setToPlayerItemShop(String playername, String item, int amount) {
        JSONObject jsonObject;
        String data = PlayersStatements.getItemShopData(playername);
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

    private void openPlayerItemShop(String playername) { //async
        Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.getPlugin(), () -> {
            Is.inventoryMap.put(playername, Bukkit.getServer().createInventory(null, 27, "§4§lITEMSHOP"));
            JSONObject jsonObject;
            String data = PlayersStatements.getItemShopData(playername);
            HashMap<String, Object> itemsMap = null;

            if (data.isEmpty()) {
                Bukkit.getScheduler().runTask(GuildsAddons.getPlugin(), () -> Bukkit.getPlayer(playername).openInventory(inventoryMap.get(playername)));
                return;
            }
            try {
                jsonObject = (JSONObject) new JSONParser().parse(data);
                itemsMap = new Gson().fromJson(jsonObject.toString(), HashMap.class);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            for (String s : config.getConfig().getKeys(false))
                if (itemsMap.containsKey(s)) {
                    for (int i = 0; i < inventoryMap.get(playername).getSize(); i++)
                        if (inventoryMap.get(playername).getItem(i) == null) {
                            ItemStack item = config.getConfig().getItemStack(s + ".item").clone();
                            Double amount = (Double) itemsMap.get(s);         //zle to dziala, czasem item jest air itd, zabiera rzeczy
                            if (amount > 64) {
                                item.setAmount(64);
                                int howManyStacks = (int) (amount / 64);
                                System.out.println(howManyStacks);
                                for (int j = 0; j < howManyStacks; j++) {
                                    if (inventoryMap.get(playername).getItem(i + j) == null)
                                        inventoryMap.get(playername).setItem(i + j, item);
                                }
                                int reszta = (amount.intValue() - 64 * howManyStacks);
                                item.setAmount(reszta);
                                inventoryMap.get(playername).setItem(i + howManyStacks, item);
                                break;
                            }
                            item.setAmount(amount.intValue());
                            inventoryMap.get(playername).setItem(i, item);
                            break;
                        }
                }

           Bukkit.getScheduler().runTask(GuildsAddons.getPlugin(), () ->Bukkit.getPlayer(playername).openInventory(inventoryMap.get(playername)));
        });
    }

    @EventHandler
    public void onPlayerInventoryClick(InventoryClickEvent ev) {
        Player player = (Player) ev.getWhoClicked();
        if (ev.getInventory().equals(inventoryMap.get(player.getName()))) {
            if (ev.getCurrentItem() != null)
                ev.setCancelled(true);

                if (ev.getCurrentItem() != null
                        && ev.getClickedInventory().equals(inventoryMap.get(player.getName())))
                             ev.setCancelled(false);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent ev) {
        Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.getPlugin(), () -> {
        Player player = (Player) ev.getPlayer();
        if (ev.getInventory().equals(inventoryMap.get(player.getName()))) {
            Inventory inv = inventoryMap.get(player.getName());
            cooldowns.setSystemTime(player);
            for (String s : config.getConfig().getKeys(false)) {
                int amountOfItems = 0;
                if (inv.containsAtLeast(config.getConfig().getItemStack(s + ".item"), 1)) {
                    for (int i = 0; i < inv.getSize(); i++) {
                        ItemStack it = inv.getItem(i);
                        if (it != null && it.isSimilar(config.getConfig().getItemStack(s + ".item"))) {
                            amountOfItems = amountOfItems + it.getAmount();
                        }
                    }
                    System.out.println(amountOfItems);
                    setToPlayerItemShop(player.getName(), s, amountOfItems); //todo zeby gdzies zapisalo a dopiero potem wyslalo
                }
                else
                    setToPlayerItemShop(player.getName(), s, amountOfItems);
            }
        }
        });
    }

}
