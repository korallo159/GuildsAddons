package koral.guildsaddons.schowek;

import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.database.statements.PlayersStatements;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
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
                    json.put(key, (int) (long) json.getOrDefault(key, 0l) + item.getAmount());
                }
            });

            Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.plugin, () -> {
                PlayersStatements.setPlayerData((Player) ev.getPlayer(), json.toJSONString());
                Bukkit.getScheduler().runTask(GuildsAddons.plugin, () -> checkEq(ev, true));
            });
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent ev) {
        checkEq(ev, false);
    }

    private void checkEq(InventoryCloseEvent ev, boolean force) {
        if (!force && ev.getInventory().getHolder() != null && ev.getInventory().getHolder() instanceof  Schowek.Holder)
            return;
        Map<Material, Integer> amounts = new HashMap<>();
        PlayerInventory inv = ev.getPlayer().getInventory();
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
            Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.plugin, () ->
                    PlayersStatements.updatePlayerData((Player) ev.getPlayer(), toSend));

    }
}