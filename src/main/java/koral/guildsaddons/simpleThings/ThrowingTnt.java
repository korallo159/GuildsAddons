package koral.guildsaddons.simpleThings;

import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.util.Cooldowns;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ThrowingTnt implements Listener {

    public static ItemStack item;
    public static float power_of_explosion;
    public static double power_of_throwing;
    public static int ticks_to_explosion;

    Set<String> cooldown = new HashSet<>();

    @EventHandler
    public void interact(PlayerInteractEvent ev) {
        if (cooldown.contains(ev.getPlayer().getName()))
            return;
        if (ev.getAction() == Action.RIGHT_CLICK_AIR || ev.getAction() == Action.RIGHT_CLICK_BLOCK) {
            PlayerInventory inv = ev.getPlayer().getInventory();
            if (inv.getItemInMainHand().isSimilar(item)) {
                summonTnt(ev.getPlayer());
                ev.setCancelled(true);
                ItemStack tnt = inv.getItemInMainHand();
                tnt.setAmount(tnt.getAmount() - 1);
                inv.setItemInMainHand(tnt.getAmount() <= 0 ? null : tnt);

                cooldown.add(ev.getPlayer().getName());
                Bukkit.getScheduler().runTaskLater(GuildsAddons.plugin, () -> cooldown.remove(ev.getPlayer().getName()), 3);
            }
        }
    }

    private void summonTnt(Player shooter) {
        TNTPrimed tnt = (TNTPrimed) shooter.getWorld().spawnEntity(shooter.getEyeLocation(), EntityType.PRIMED_TNT);
        tnt.setVelocity(shooter.getLocation().getDirection().multiply(power_of_throwing));
        tnt.setFuseTicks(ticks_to_explosion);
        tnt.setYield(power_of_explosion);
        tnt.setSource(shooter);
        shooter.getWorld().playSound(shooter.getLocation(), Sound.ENTITY_WITHER_SHOOT, .3f, .3f);
    }
}
