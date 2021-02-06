package koral.guildsaddons.listeners;

import com.google.common.collect.Lists;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoneDrop implements Listener, TabExecutor {
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

        public Holder(Player p) {
            inv = Bukkit.createInventory(this, ((drops.length - 1) / 9 + 1) * 9, ChatColor.DARK_RED + "" + ChatColor.BOLD + "Drop ze Stone");
            for (int i=0; i < inv.getSize(); i++) inv.setItem(i, emptySlot);
            int[] slots = sloty(drops.length, inv.getSize() / 9);
            int slotIndex = 0;
            for (Drop drop : drops)
                inv.setItem(slots[slotIndex++], refactor(p, new ItemStack(drop.mat), drop));
        }
        // enchantowe = włączone
        public static ItemStack refactor(Player p, ItemStack item) {
            for (Drop drop : drops)
                if (drop.mat == item.getType())
                    return refactor(p, item, drop);
            return item;
        }

        static final String yes = ChatColor.GREEN + "" + ChatColor.BOLD + "✔";
        static final String no  = ChatColor.RED   + "" + ChatColor.BOLD + "❌";
        // enchantowe = włączone
        public static ItemStack refactor(Player p, ItemStack item, Drop drop) {
            ItemMeta meta = item.getItemMeta();

            boolean on = !p.getScoreboardTags().contains(unwantedTag(item));

            if (!meta.hasLore()) {
                List<String> lore = Lists.newArrayList(
                        ChatColor.GREEN + "Szansa: " + ChatColor.YELLOW + String.format("%.2f", drop.defaultChange * 100) + "%",
                                "",
                                ChatColor.BLUE + "Włączony: " + (on ? yes : no),
                                ChatColor.BLUE + "Fortuna: "  + (drop.fortune_bonus != 0 ? yes : no)
                        );
                if (drop.permsMap != null) {
                    lore.add("");
                    drop.permsMap.forEach((perm, change) -> lore.add(
                            ChatColor.GOLD + GuildsAddons.plugin.getConfig().getString("Permnames." + perm, perm) +
                                    "§8: §a+§e" + String.format("%.2f", change * 100)
                    ));
                }
                meta.setLore(lore);
            }

            if (on) {
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
    static class Drop {
        Material mat;
        double defaultChange = 1;
        double fortune_bonus = .1;
        Map<String, Double> permsMap;

        public Drop(Material mat, String[] args) {
            this.mat = mat;
            switch (args.length) {
                default:
                    permsMap = new HashMap<>();
                    for (int i=2; i < args.length; i++) {
                        String[] subArgs = args[i].split(":");
                        permsMap.put(subArgs[0], Double.parseDouble(subArgs[1]));
                    }
                case 2:
                    fortune_bonus = Double.parseDouble(args[1]);
                case 1:
                    defaultChange = Double.parseDouble(args[0]);
                case 0:
            }
        }
    }


    public static int multiplier = 1;

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
                for (Drop drop : drops)
                    if (!ev.getPlayer().getScoreboardTags().contains(unwantedTag(drop.mat)))
                        ev.getPlayer().getInventory().addItem(new ItemStack(drop.mat)).forEach((count, cannceledItem) -> {
                            cannceledItem.setAmount(count);
                            ev.getBlock().getWorld().dropItem(ev.getBlock().getLocation(), cannceledItem);
                        });
            }
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
        p.openInventory(new Holder(p).getInventory());
    }

    static String unwantedTag(Material mat) {
        return  "GuildsAddonsStoneDropUnwanted" + mat;
    }
    static String unwantedTag(ItemStack item) {
        return unwantedTag(item.getType());
    }


    static Drop[] drops;
    private static int drops_index; // dummy
    public static ConfigManager config = new ConfigManager("Stone Drop.yml");
    public static void reload() {
        config.reloadCustomConfig();

        Map<String, Object> map = config.config.getValues(false);
        drops = new Drop[map.size()];
        drops_index = 0;
        map.forEach((mat, args) -> drops[drops_index++] = new Drop(Material.valueOf(mat.toUpperCase()), ((String) args).split(" ")));
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
