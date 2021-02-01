package koral.guildsaddons.schowek;

import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.database.statements.PlayersStatements;
import koral.guildsaddons.managers.ConfigManager;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Schowek implements TabExecutor {
    public static class Holder implements InventoryHolder {
        static final ItemStack emptySlot;
        static {
            emptySlot = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = emptySlot.getItemMeta();
            meta.setDisplayName(ChatColor.GRAY + " ");
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            emptySlot.setItemMeta(meta);
        }

        final Inventory inv;
        public Holder(JSONObject json) {
            List<ItemStack> items = new ArrayList<>();

            json.forEach((item, _amount) -> {
                int amount = (int) (long) _amount;
                Material mat = Material.valueOf((String) item);

                while (amount > 0) {
                    int count = Math.min(amount, 64);
                    amount -= count;

                    ItemStack itemStack = new ItemStack(mat);
                    itemStack.setAmount(count);
                    items.add(itemStack);
                }
            });

            inv = Bukkit.createInventory(this, ((items.size() - 1) / 9 + 1) * 9, ChatColor.DARK_RED + "" + ChatColor.BOLD + "Schowek");

            int i = 0;
            for (ItemStack item : items)
                inv.setItem(i++, item);
            while (i < inv.getSize())
                inv.setItem(i++, emptySlot);
        }

        @Override
        public Inventory getInventory() {
            return inv;
        }
    }

    static final Map<Material, Integer> limits = new HashMap<>();

    public static void reload() {
        limits.clear();
        ConfigManager config = new ConfigManager("items limit.yml");
        config.config.getValues(false).forEach((str, count) -> {
            try {
                limits.put(Material.valueOf(str.toUpperCase().replace(' ', '_')), (Integer) count);
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
                    String data = PlayersStatements.getMysqlPlayerData(((Player) sender));
                    JSONObject json = data == null ? new JSONObject() : (JSONObject) new JSONParser().parse(data);
                    Inventory inv = new Holder(json).getInventory();
                    Bukkit.getScheduler().runTask(GuildsAddons.plugin, () -> ((Player) sender).openInventory(inv));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
        return true;
    }
}
