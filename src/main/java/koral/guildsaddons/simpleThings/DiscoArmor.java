package koral.guildsaddons.simpleThings;

import com.mojang.datafixers.util.Pair;
import koral.guildsaddons.GuildsAddons;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityEquipment;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class DiscoArmor {



    public void runDiscoArmor(Player player){

        Random r = new Random();
        Bukkit.getScheduler().runTaskTimer(GuildsAddons.getPlugin(), () ->{
            ItemStack isHelmet = new ItemStack(Material.LEATHER_HELMET); LeatherArmorMeta m1 = (LeatherArmorMeta) isHelmet.getItemMeta();
            ItemStack isChestPlate = new ItemStack(Material.LEATHER_CHESTPLATE); LeatherArmorMeta m2 = (LeatherArmorMeta) isChestPlate.getItemMeta();
            ItemStack isLeggings = new ItemStack(Material.LEATHER_LEGGINGS); LeatherArmorMeta m3 = (LeatherArmorMeta) isLeggings.getItemMeta();
            ItemStack isBoots = new ItemStack(Material.LEATHER_BOOTS); LeatherArmorMeta m4 = (LeatherArmorMeta) isBoots.getItemMeta();

            m1.setColor(Color.fromBGR(r.nextInt(255), r.nextInt(255), r.nextInt(255)));
            m2.setColor(Color.fromBGR(r.nextInt(255), r.nextInt(255), r.nextInt(255)));
            m3.setColor(Color.fromBGR(r.nextInt(255), r.nextInt(255), r.nextInt(255)));
            m4.setColor(Color.fromBGR(r.nextInt(255), r.nextInt(255), r.nextInt(255)));

            Bukkit.getOnlinePlayers().forEach(gracz ->{
                Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack> p1 = new com.mojang.datafixers.util.Pair<>(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(isHelmet));
                Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack> p2 = new com.mojang.datafixers.util.Pair<>(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(isChestPlate));
                Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack> p3 = new com.mojang.datafixers.util.Pair<>(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(isLeggings));
                Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack> p4 = new com.mojang.datafixers.util.Pair<>(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(isBoots));

                PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(player.getEntityId(), Arrays.asList(p1, p2, p3, p4));

                ((CraftPlayer) gracz).getHandle().playerConnection.sendPacket(packet);
            });

        }, 0, 10);
    }
}
