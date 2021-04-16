package koral.guildsaddons.simpleThings;

import com.mojang.datafixers.util.Pair;
import koral.guildsaddons.GuildsAddons;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityEquipment;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class DiscoArmor implements CommandExecutor {

            HashMap<String, BukkitTask> discoTask = new HashMap<>();

    public void runDiscoArmor(Player player){
//550154838
        if(discoTask.containsKey(player.getName()) && !discoTask.get(player.getName()).isCancelled()){
            Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack> p1 = null;
            Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack> p2 = null;
            Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack> p3 = null;
            Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack> p4 = null;

            if(player.getInventory().getHelmet() != null) p1 = new com.mojang.datafixers.util.Pair<>(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy( player.getInventory().getHelmet()));
            if(player.getInventory().getChestplate() != null) p2 = new com.mojang.datafixers.util.Pair<>(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(player.getInventory().getChestplate()));
            if(player.getInventory().getLeggings() != null) p3 = new com.mojang.datafixers.util.Pair<>(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(player.getInventory().getLeggings()));
            if(player.getInventory().getLeggings() != null) p4 = new com.mojang.datafixers.util.Pair<>(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(player.getInventory().getBoots()));

            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(player.getEntityId(), Arrays.asList(p1, p2, p3, p4));

            discoTask.get(player.getName()).cancel();

                        Bukkit.getOnlinePlayers().forEach(player1 -> {
                            if(player1.getName() != player.getName())
                            ((CraftPlayer) player1).getHandle().playerConnection.sendPacket(packet);
                        });
            return;
        }

        Random r = new Random();
        if(!discoTask.containsKey(player.getName()) || discoTask.get(player.getName()).isCancelled()) {

            BukkitTask bukkitTask =  Bukkit.getScheduler().runTaskTimer(GuildsAddons.getPlugin(), () -> {
                ItemStack isHelmet = new ItemStack(Material.LEATHER_HELMET);
                LeatherArmorMeta m1 = (LeatherArmorMeta) isHelmet.getItemMeta();
                ItemStack isChestPlate = new ItemStack(Material.LEATHER_CHESTPLATE);
                LeatherArmorMeta m2 = (LeatherArmorMeta) isChestPlate.getItemMeta();
                ItemStack isLeggings = new ItemStack(Material.LEATHER_LEGGINGS);
                LeatherArmorMeta m3 = (LeatherArmorMeta) isLeggings.getItemMeta();
                ItemStack isBoots = new ItemStack(Material.LEATHER_BOOTS);
                LeatherArmorMeta m4 = (LeatherArmorMeta) isBoots.getItemMeta();
                if(player.getEquipment().getHelmet() != null && player.getEquipment().getHelmet().getType().equals(Material.DIAMOND_HELMET))
                    m1.setColor(Color.fromRGB(1, r.nextInt(180 - 150)+150, r.nextInt(255 - 190)+190));
                else m1.setColor(Color.fromBGR(r.nextInt(255), r.nextInt(255), r.nextInt(255)));

                if(player.getEquipment().getChestplate() != null && player.getEquipment().getChestplate().getType().equals(Material.DIAMOND_CHESTPLATE))
                    m2.setColor(Color.fromRGB(1, r.nextInt(180 - 150)+150, r.nextInt(255 - 190)+190));
                else m2.setColor(Color.fromBGR(r.nextInt(255), r.nextInt(255), r.nextInt(255)));

                if(player.getEquipment().getLeggings() != null && player.getEquipment().getLeggings().getType().equals(Material.DIAMOND_LEGGINGS))
                    m3.setColor(Color.fromRGB(1, r.nextInt(180 - 150)+150, r.nextInt(255 - 190)+190));
                else m3.setColor(Color.fromBGR(r.nextInt(255), r.nextInt(255), r.nextInt(255)));

                if(player.getEquipment().getBoots() != null && player.getEquipment().getBoots().getType().equals(Material.DIAMOND_BOOTS))
                    m4.setColor(Color.fromRGB(1, r.nextInt(180 - 150)+150, r.nextInt(255 - 190)+190));
                else m4.setColor(Color.fromBGR(r.nextInt(255), r.nextInt(255), r.nextInt(255)));

                isHelmet.setItemMeta(m1);
                isChestPlate.setItemMeta(m2);
                isLeggings.setItemMeta(m3);
                isBoots.setItemMeta(m4);

                Bukkit.getOnlinePlayers().forEach(gracz -> {
                    Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack> p1 = new com.mojang.datafixers.util.Pair<>(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(isHelmet));
                    Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack> p2 = new com.mojang.datafixers.util.Pair<>(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(isChestPlate));
                    Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack> p3 = new com.mojang.datafixers.util.Pair<>(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(isLeggings));
                    Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack> p4 = new com.mojang.datafixers.util.Pair<>(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(isBoots));

                    PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(player.getEntityId(), Arrays.asList(p1, p2, p3, p4));

                    if(gracz.getName() != player.getName())
                    ((CraftPlayer) gracz).getHandle().playerConnection.sendPacket(packet);
                });

            }, 0, 8);

            discoTask.put(player.getName(), bukkitTask);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        runDiscoArmor((Player) sender);
        return true;
    }
}
