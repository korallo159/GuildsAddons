package koral.guildsaddons.simpleThings;

import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.managers.ConfigManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class Klatka implements Listener, TabExecutor {

    Queue<Player> queue = new ArrayDeque<>(2);
    public static ConfigManager cageConfig = new ConfigManager("cage.yml");
    Cage cage = new Cage();







    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        if(args.length == 1 && player.hasPermission("guildsaddons.gadmin")){
            switch(args[0]){
                case"loc1":
                    cage.setSpawnPoint1(player.getLocation());
                    cageConfig.getConfig().set("cage.s1", player.getLocation());
                    cageConfig.save();
                    player.sendMessage("ustawiles 1miejsce spawna klatki");
                    break;

                case"loc2":
                    cage.setSpawnPoint2(player.getLocation());
                    cageConfig.getConfig().set("cage.s2", player.getLocation());
                    cageConfig.save();
                    player.sendMessage("ustawiles 2miejsce spawna klatki");
                    break;
                case"bell":
                    cageConfig.getConfig().set("bell", ((Player) sender).getTargetBlock(5).getLocation());
                    cageConfig.save();
                    sender.sendMessage(ChatColor.GREEN + "Ustawiles dzwon dolaczania do klatki.");
                    break;

            }

            return true;
        }

        if(!isCageConfigured()){
            player.sendMessage("§6Klatka jeszcze nie jest skonfigurowana!");
            return true;
        }

        if(!cage.isCageReady()){
            player.sendMessage("§6Nie możesz dołączyć do kolejki, ponieważ ktoś się już bije!");
            return true;
        }
        if(isQueued(player)) {
            queue.remove(player);
            player.sendMessage("§6Opuściłeś kolejkę do bitwy w klatce.");
        } else {
            queue.add(player);
            Bukkit.getScheduler().runTaskLater(GuildsAddons.getPlugin(), () -> {
               if(queue.contains(player)){
                   queue.remove();
               }
            }, 600);
            player.sendMessage("§6Dołączyłeś do kolejki w klatce, jeżeli w ciągu 30 sekund nie znajdzie się chętny, zostaniesz usunięty z kolejki.");
        }

        if(queue.size() == 2){
            cage.fightingPlayers.add(queue.remove());
            cage.fightingPlayers.add(queue.remove());
            cage.startFight();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        if(cage.getPlayers().contains(event.getPlayer())){
            if(cage.getPlayers().size() == 2){
                event.getPlayer().setHealth(0);
            }
            cage.fightingPlayers.remove(event.getPlayer());
            Bukkit.getScheduler().runTaskLater(GuildsAddons.getPlugin(), () -> {
                cage.endFight();
            }, 200);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        if(cage.getPlayers().contains(event.getEntity().getPlayer())){
            cage.fightingPlayers.remove(event.getEntity().getPlayer());
            Bukkit.getScheduler().runTaskLater(GuildsAddons.getPlugin(), () -> {
                cage.endFight();
            }, 200);
        }
    }

    public boolean isCageConfigured() {
        Location loc1 = cageConfig.getConfig().getLocation("cage.s1");
        Location loc2 = cageConfig.getConfig().getLocation("cage.s2");
        if (loc1 == null || loc2 == null)
            System.out.println("§6[GUILDS] Lokalizacje klatki nie sa ustawione, zrob to koniecznie!");
        else{
            return true;

        }
        return false;
    }

    class Cage {
        {
            if(isCageConfigured()){
                Location loc1 = cageConfig.getConfig().getLocation("cage.s1");
                Location loc2 = cageConfig.getConfig().getLocation("cage.s2");
                setSpawnPoint1(loc1);
                setSpawnPoint2(loc2);
            }
        }

        private List<Player> fightingPlayers = new ArrayList<>();


        private Location spawnPoint1;
        private Location spawnPoint2;

        public List<Player> getPlayers() {
            return fightingPlayers;
        }

        public Location getSpawnPoint1() {
            return spawnPoint1;
        }

        public Location getSpawnPoint2() {
            return spawnPoint2;
        }

        public boolean isCageReady() {
            if (fightingPlayers.isEmpty()) return true;

            return false;
        }

        public void startFight(){
            Player p1 = fightingPlayers.get(0); Player p2 = fightingPlayers.get(1);
            p1.teleport(getSpawnPoint1());p2.teleport(getSpawnPoint2());

            getPlayers().stream().forEach(p -> prepareTimer(p, 5, p.getLocation()));
        }
        public void endFight(){
            getPlayers().forEach(p -> p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation()));
            fightingPlayers.clear();
        }


        public void setSpawnPoint1(Location spawnPoint1) {
            this.spawnPoint1 = spawnPoint1;
        }

        public void setSpawnPoint2(Location spawnPoint2) {
            this.spawnPoint2 = spawnPoint2;
        }

        void prepareTimer(Player player, int secondsLeft, Location lastLoc) {

            if(player.getLocation().distance(lastLoc) > 1){
                player.teleport(lastLoc);
            }
            if (secondsLeft <= 0) {
                return;
            } else {
                player.sendTitle("§c§lPrzygotuj się!", secondsLeft + "s §7 do startu walki!", 0, 22, 5);
                if(player.getInventory().getItemInMainHand().getType().equals(Material.BOW)) player.getInventory().setHeldItemSlot(0);
                if(player.getInventory().getItemInMainHand().getType().equals(Material.BOW) && player.getInventory().getHeldItemSlot() == 0) player.getInventory().setHeldItemSlot(1);
                Bukkit.getScheduler().runTaskLater(GuildsAddons.getPlugin(), () -> prepareTimer(player ,secondsLeft - 1, lastLoc), 20);
            }
        }

    }

    public boolean isQueued(Player player){
        if(queue.contains(player)){
            queue.remove(player);
            return true;
        }
        return false;
    }

}
