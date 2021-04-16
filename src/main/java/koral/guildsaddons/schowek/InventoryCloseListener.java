package koral.guildsaddons.schowek;

import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.database.statements.PlayersStatements;
import koral.sectorserver.util.SectorScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InventoryCloseListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryCloseMonitor(InventoryCloseEvent ev) {
        if (ev.getInventory().getHolder() != null && ev.getInventory().getHolder() instanceof Schowek.Holder) {
            JSONObject json = new JSONObject();

            Pattern pattern = Pattern.compile(".+ (-?\\d+)");
            Schowek.dataMap.forEach((mat, pair) -> {
                int slot = pair.t2;

                Matcher matcher = pattern.matcher(ev.getInventory().getItem(slot).getItemMeta().getDisplayName());

                matcher.matches();

                json.put(mat.toString(), Integer.parseInt(matcher.group(1)));
            });

            SectorScheduler.addTaskToQueue("prePlayerChangeSectorEvent",
                    () -> PlayersStatements.setPlayerData((Player) ev.getPlayer(), json.toJSONString()));
            //checkEq(ev, true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent ev) {
        checkEq(ev, false);
    }

    static void checkEq(InventoryCloseEvent ev, boolean force) {
        checkEq((Player) ev.getPlayer(), ev, force);
    }
    static void checkEq(Player p, InventoryEvent ev, boolean force) {
        if (p.hasPermission("guildsaddons.schowek.bypass")) return;
        if (!force && ev.getInventory().getHolder() != null && ev.getInventory().getHolder() instanceof Schowek.Holder)
            return;
        Map<Material, Integer> amounts = new HashMap<>();
        PlayerInventory inv = p.getInventory();
        Map<String, Integer> toSend = new HashMap<>();
        for (int i=0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null) {
                Material mat = item.getType();
                int max = Schowek.getLimit(mat);
                if (max > 0) {
                    int akt = amounts.getOrDefault(mat, 0);
                    if (akt >= max) {
                        toSend.put(mat.toString(), toSend.getOrDefault(mat.toString(), 0) + item.getAmount());
                        inv.setItem(i, null);
                    } else {
                        int amount = akt + item.getAmount();
                        amounts.put(mat, amount);
                        if (amount > max) {
                            int count = amount - max; // do zabrania

                            toSend.put(mat.toString(), toSend.getOrDefault(mat.toString(), 0) + count);

                            item.setAmount(item.getAmount() - count);
                            inv.setItem(i, item.getAmount() <= 0 ? null : item);
                        }
                    }
                }
            }
        }
        if (!toSend.isEmpty())
            SectorScheduler.addTaskToQueue("prePlayerChangeSectorEvent", () -> PlayersStatements.updatePlayerData(p, toSend));

    }
}
