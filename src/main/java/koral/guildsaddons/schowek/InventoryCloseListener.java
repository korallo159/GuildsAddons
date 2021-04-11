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

public class InventoryCloseListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryCloseMonitor(InventoryCloseEvent ev) {
        if (ev.getInventory().getHolder() != null && ev.getInventory().getHolder() instanceof Schowek.Holder) {
            JSONObject json = new JSONObject();
            ev.getInventory().forEach(item -> {
                if (!Schowek.Holder.emptySlot.isSimilar(item)) {
                    String key = item.getType().toString();
                    Object numObj = json.getOrDefault(key, 0l);
                    int num;
                    if (numObj instanceof Long)
                        num = (int) (long) numObj;
                    else if (numObj instanceof Integer)
                        num = (int) numObj;
                    else
                        num = Integer.parseInt(numObj.toString());
                    json.put(key, num + item.getAmount());
                }
            });
            SectorScheduler.addTaskToQueue("prePlayerChangeSectorEvent",
                    () -> PlayersStatements.setPlayerData((Player) ev.getPlayer(), json.toJSONString()));
            checkEq(ev, true);
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
        if (!force && ev.getInventory().getHolder() != null && ev.getInventory().getHolder() instanceof Schowek.Holder)
            return;
        Map<Material, Integer> amounts = new HashMap<>();
        PlayerInventory inv = p.getInventory();
        Map<String, Integer> toSend = new HashMap<>();
        for (int i=0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null) {
                Material mat = item.getType();
                Integer max = Schowek.limits.get(mat);
                if (max != null) {
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
