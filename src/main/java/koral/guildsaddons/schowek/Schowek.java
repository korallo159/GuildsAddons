package koral.guildsaddons.schowek;

import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.database.statements.PlayersStatements;
import koral.guildsaddons.managers.ConfigManager;
import koral.guildsaddons.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.*;

public class Schowek implements TabExecutor {
    public static class Holder implements InventoryHolder {
        static final ItemStack emptySlot;
        static {
            emptySlot = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = emptySlot.getItemMeta();
            meta.setDisplayName(ChatColor.GRAY + " ");
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            emptySlot.setItemMeta(meta);
        }

        final Inventory inv;
        public Holder(JSONObject json) {
            inv = Bukkit.createInventory(this, invSize, ChatColor.DARK_RED + "" + ChatColor.BOLD + "Schowek");

            for (int i=0; i < inv.getSize(); i++)
                inv.setItem(i, emptySlot);

            dataMap.forEach((mat, pair) -> {
                int slot = pair.t2;

                int count = (int) (long) json.getOrDefault(mat.toString(), 0L);

                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                meta.addItemFlags(ItemFlag.HIDE_DYE);
                meta.setDisplayName("§6Posiadane§8:§e " + count);
                meta.setLore(Arrays.asList("§aKliknij aby wypłacić"));
                item.setItemMeta(meta);
                if (count > 0 && count <= 64) {
                    item.setAmount(count);
                }

                inv.setItem(slot, item);
            });
        }

        @Override
        public Inventory getInventory() {
            return inv;
        }
    }

    // map: (limit, slot)
    static final Map<Material, Pair<Integer, Integer>> dataMap = new HashMap<>();
    static int invSize;

    private static final Pair<Integer, Integer> emptyPair = new Pair<>(0, 0);
    public static int getLimit(Material mat) {
        return dataMap.getOrDefault(mat, emptyPair).t1;
    }

    public static void reload() {
        dataMap.clear();

        ConfigManager config = new ConfigManager("schowek.yml");

        invSize = config.config.getInt("size") * 9;

        config.config.getConfigurationSection("items").getKeys(false).forEach(str -> {
            try {
                Material mat = Material.valueOf(str.toUpperCase().replace(' ', '_'));
                int limit = config.config.getInt("items." + str + ".limit");
                int slot  = config.config.getInt("items." + str + ".slot");

                dataMap.put(mat, new Pair<>(limit, slot));
            } catch (Throwable e) {
                System.out.println("Niepoprawny item w \"items limit.yml\": " + str);
            }
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player))
            sender.sendMessage("Tej komendy może użyć tylko gracz");
        else
            Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.plugin, () -> {
                try {
                    if (args.length >= 1 && sender.hasPermission("guildsaddons.schowek.admin")) {
                        JSONObject data = getData(args[0]);
                        if (data == null)
                            sender.sendMessage("Niepoprawny gracz: " + args[0]);
                        else {
                            sender.sendMessage("§aItemy w showku gracza §2" + args[0]);
                            data.forEach((material, amount) ->
                                    sender.sendMessage("§6" + material.toString().toLowerCase().replace("_", " ") + "§8: §e" + amount));
                        }

                        if (args.length >= 2 && args[1].equalsIgnoreCase("wyczyść")) {
                            PlayersStatements.setPlayerData(args[0], "{}");
                            sender.sendMessage("\n§aWyczyszczono schowek gracza §2" + args[0]);
                        }
                    }

                    Inventory inv = new Holder(getData(sender.getName())).getInventory();
                    Bukkit.getScheduler().runTask(GuildsAddons.plugin, () -> ((Player) sender).openInventory(inv));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
        return true;
    }

    private JSONObject getData(String playerName) throws ParseException {
        String data = PlayersStatements.getMysqlPlayerData(playerName);
        return data == null ? new JSONObject() : (JSONObject) new JSONParser().parse(data);
    }

}
