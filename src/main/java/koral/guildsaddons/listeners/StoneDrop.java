package koral.guildsaddons.listeners;

import com.google.common.collect.Lists;
import koral.guildsaddons.Ciąg;
import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public class StoneDrop implements Listener, TabExecutor {
    public static int multiplier = 1;

    static final Ciąg<Material> ciag = new Ciąg<>();

    private static final String tagPrefix = "GuildsAddonsTurboDrop:";
    private static final String tag = "GuildsAddonsTurboDrop";

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent ev) {
        if (ev.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;
        if (!ev.isCancelled() && ev.isDropItems() && ev.getBlock().getType() == Material.STONE) {
            ev.setDropItems(false);
            int multiplier = StoneDrop.multiplier;
            if (ev.getPlayer().getScoreboardTags().contains(StoneDrop.tag)) {
                for (String tag : ev.getPlayer().getScoreboardTags())
                    if (tag.startsWith(tagPrefix)) {
                        long finalTime = Long.parseLong(tag.substring(tagPrefix.length()));
                        if (finalTime < System.currentTimeMillis()) {
                            ev.getPlayer().removeScoreboardTag(StoneDrop.tag);
                            ev.getPlayer().removeScoreboardTag(tag);
                        } else
                            multiplier++;
                        break;
                    }
            }

            for (int i = 0; i < multiplier; i++) {
                ev.getPlayer().giveExp(GuildsAddons.plugin.getConfig().getInt("exp from blocks", 0));
                Material mat = ciag.losuj();
                if (!ev.getPlayer().getScoreboardTags().contains(unwantedTag(mat)))
                    ev.getPlayer().getInventory().addItem(new ItemStack(mat)).forEach((count, cannceledItem) -> {
                        cannceledItem.setAmount(count);
                        ev.getBlock().getWorld().dropItem(ev.getBlock().getLocation(), cannceledItem);
                    });

            }
        }
    }

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
        Inventory inv;

        int slotIndex; // dummy
        public Holder(Player p, Map<Material, Double> changeMap) {
            inv = Bukkit.createInventory(this, ((changeMap.size() - 1) / 9 + 1) * 9, ChatColor.DARK_RED + "" + ChatColor.BOLD + "Drop ze Stone");
            for (int i=0; i < inv.getSize(); i++) inv.setItem(i, emptySlot);
            slotIndex = 0;
            int[] slots = sloty(changeMap.size(), inv.getSize() / 9);
            changeMap.forEach((mat, change) -> inv.setItem(slots[slotIndex++], refactor(p, new ItemStack(mat), changeMap)));
        }
        // enchantowe = włączone
        public static ItemStack refactor(Player p, ItemStack item) {
            return refactor(p, item, ciag.szanse());
        }
        // enchantowe = włączone
        public static ItemStack refactor(Player p, ItemStack item, Map<Material, Double> changeMap) {
            ItemMeta meta = item.getItemMeta();

            if (!meta.hasLore())
                meta.setLore(Lists.newArrayList(ChatColor.GREEN + "Szansa: " +
                        ChatColor.YELLOW + String.format("%.2f", changeMap.getOrDefault(item.getType(), 0d) * 100) + "%"));

            if (!p.getScoreboardTags().contains(unwantedTag(item))) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                meta.addEnchant(Enchantment.ARROW_INFINITE, 1, false);
            } else {
                meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
                meta.removeEnchant(Enchantment.ARROW_INFINITE);
            }
            item.setItemMeta(meta);

            return item;
        }

        @Override
        public Inventory getInventory() {
            return inv;
        }

        private static int[] sloty(int potrzebneSloty, int rzędy) {
            if (rzędy == 1)
                switch (potrzebneSloty) {
                    case 0: return new int[] {};
                    case 1: return new int[] {4};
                    case 2: return new int[] {3, 5};
                    case 3: return new int[] {2, 4, 6};
                    case 4: return new int[] {1, 3, 5, 7};
                    case 5: return new int[] {2, 3, 4, 5, 6};
                    case 6: return new int[] {1, 2, 3, 5, 6, 7};
                    case 7: return new int[] {1, 2, 3, 4, 5, 6, 7};
                    case 8: return new int[] {0, 1, 2, 3, 5, 6, 7, 8};
                    case 9: return new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8};
                    default:return null;
                }


            int[] sloty = new int[potrzebneSloty];

            int ubytek = potrzebneSloty / rzędy;
            int reszta = potrzebneSloty % rzędy;

            int index = 0;

            int mn = 0;

            while (potrzebneSloty > 0) {
                int dodatek = reszta-- > 0 ? 1 : 0;
                potrzebneSloty -= ubytek + dodatek;
                for (int i : sloty(ubytek + dodatek, 1))
                    sloty[index++] = mn*9 + i;
                mn++;
            }

            return sloty;
        }
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent ev) {
        ItemStack item = ev.getCurrentItem();
        if (ev.getInventory().getHolder() != null && ev.getInventory().getHolder() instanceof Holder && ev.getRawSlot() >= 0 && ev.getRawSlot() < ev.getInventory().getSize()) {
            ev.setCancelled(true);
            if (item != null && !item.isSimilar(Holder.emptySlot)) {
                Holder holder = (Holder) ev.getInventory().getHolder();
                if (item.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) {
                    // właczone, wyłączanie
                    ev.getWhoClicked().addScoreboardTag(unwantedTag(item));
                } else {
                    // wyłączone, włączanie
                    ev.getWhoClicked().removeScoreboardTag(unwantedTag(item));
                }
                ev.getInventory().setItem(ev.getRawSlot(), Holder.refactor((Player) ev.getWhoClicked(), item));
            }
        }
    }

    public static void openDropInv(Player p) {
        p.openInventory(new Holder(p, ciag.szanse()).getInventory());
    }

    static String unwantedTag(Material mat) {
        return  "GuildsAddonsStoneDropUnwanted" + mat;
    }
    static String unwantedTag(ItemStack item) {
        return unwantedTag(item.getType());
    }

    public static ConfigManager config = new ConfigManager("Stone Drop.yml");
    public static void reload() {
        config.reloadCustomConfig();
        ciag.wyczyść();

        config.config.getValues(false).forEach((material, change) -> ciag.dodaj(Material.valueOf(material.toUpperCase()), (int) change));
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command cmd, String label, String[] args) {
        if (args.length == 1 && cmd.getName().equals("turbodrop") && args[0].startsWith("@"))
            return Lists.newArrayList("@server");
        return null;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command cmd, String label, String[] args) {
        if (cmd.getName().equals("drop")) {
            if (commandSender instanceof Player)
                openDropInv((Player) commandSender);
            else
                commandSender.sendMessage("Musisz być graczem aby tego użyć");
        } else {
            if (args.length < 2)
                return false;

            int minutes;
            try {
                minutes = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                return false;
            }

            if (args[0].equalsIgnoreCase("@server")) {
                multiplier++;// TODO: wysyłać na reszte serwerów zmiany w multiplier
                //TODO: bossbar mówiący graczom o aktywnych turbo dropach
                // turbo dropy na @server sie stakują jak coś
                // turbo dropy na graczy się NIE stakują
                commandSender.sendMessage("Dano Turo Drop na " + minutes + " minut dla całego serwera");
                Bukkit.getScheduler().runTaskLater(GuildsAddons.plugin, () -> {
                    multiplier--;// TODO: wysyłać na reszte serwerów zmiany w multiplier
                }, 20*60*minutes);
            } else {
                Player p = Bukkit.getPlayer(args[0]);
                if (p == null) {
                    commandSender.sendMessage("Nieprawidłowy gracz " + args[0]);
                    return true;
                }
                if (p.getScoreboardTags().contains(StoneDrop.tag)) {
                    for (String tag : p.getScoreboardTags())
                        if (tag.startsWith(tagPrefix)) {
                            p.removeScoreboardTag(StoneDrop.tag);
                            p.removeScoreboardTag(tag);
                            break;
                        }
                }
                p.addScoreboardTag(StoneDrop.tag);
                p.addScoreboardTag(tagPrefix + (System.currentTimeMillis() + minutes*60*1000));
                commandSender.sendMessage(String.format("Dano turbo Drop dla %s na %s minut", p.getDisplayName(), minutes));
            }
        }
        return true;
    }
}
