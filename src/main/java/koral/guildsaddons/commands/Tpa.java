package koral.guildsaddons.commands;

import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.listeners.PluginChannelListener;
import koral.guildsaddons.util.Cooldowns;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;

public class Tpa implements CommandExecutor {
    Cooldowns cooldown = new Cooldowns(new HashMap<>());
    HashMap<String, String> localTpMap = new HashMap<>();
    final String tpaPrefix = "§2[§aTPA§2] ";
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) return false;
            if(sender instanceof Player){
                Player player = (Player) sender;
        if(command.getName().equalsIgnoreCase("tpa") && args.length == 1) {
            if(!cooldown.hasCooldown((((Player) sender).getPlayer()), 5, tpaPrefix + "§aMusisz odczekać jeszcze chwilę zanim wyślesz propozycję o tpa ponownie")) {
                if (Bukkit.getPlayer(args[0]) != null && !args[0].equalsIgnoreCase(player.getName())) {
                    localTpMap.put(args[0].toLowerCase(), player.getName());
                    player.sendMessage(tpaPrefix + "§aWysłałeś prośbę o teleportację do " + args[0]);
                    Bukkit.getPlayer(args[0]).sendMessage(tpaPrefix + "§cDostałeś prośbę o teleportację od gracza " + player.getName() +
                            " aby zaakceptować wpisz /tpaccept");
                    return true;
                } else {
                    PluginChannelListener.sendTpaRequest("TpaChannel", "ALL", (Player) sender, args[0]);
                    player.sendMessage(tpaPrefix + "§aWysłałeś prośbę o teleportację do " + args[0]);
                }
                cooldown.setSystemTime(player);
            }
        }

        if(command.getName().equalsIgnoreCase("tpaccept")) {
            if (localTpMap.containsKey(player.getName().toLowerCase())) {
                Player target = Bukkit.getPlayer(localTpMap.remove(player.getName().toLowerCase()));
                target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 11*20, 0, false, false, false));
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 11*20, 2, false, false, false));
                tpaLocalTimer(player, target, target.getLocation(), 10);
            }
            else if (PluginChannelListener.tpaMap.containsKey(sender.getName().toLowerCase())) {
                PluginChannelListener.sendTpaAcceptation("TpaChannel", "ALL", (Player) sender, PluginChannelListener.tpaMap.get(sender.getName().toLowerCase()), true);
                sender.sendMessage(tpaPrefix + "§aZaakceptowałes prośbę o teleportacje, gracz pojawi się obok Ciebie");
                PluginChannelListener.tpaMap.remove(sender.getName());
            } else sender.sendMessage(tpaPrefix + "§cNie posiadasz żadnej prośby o teleportacje!");
        }
        if(command.getName().equalsIgnoreCase("tpdeny")){
            if(localTpMap.containsKey(player.getName().toLowerCase())) {
                localTpMap.remove(player.getName().toLowerCase());
                sender.sendMessage(tpaPrefix + "§cOdrzuciłeś prośbę o teleportację");
            }
            else if(PluginChannelListener.tpaMap.containsKey(sender.getName().toLowerCase())) {
                PluginChannelListener.sendTpaAcceptation("TpaChannel", "ALL", (Player) sender, PluginChannelListener.tpaMap.get(sender.getName().toLowerCase()), false);
                sender.sendMessage(tpaPrefix + "§cOdrzuciłeś prośbę o teleportację");
            }
            else sender.sendMessage(tpaPrefix + "§cNie posiadasz żadnej prośby o teleportacje!");
          }
      }
       return  true;
    }

    private void tpaLocalTimer(Player player, Player target, Location lastLocation, int secondsLeft) {
        if (target.getLocation().distance(lastLocation) > 1) {
            target.sendMessage("§cPoruszyłeś się! Teleportacja anulowana.");
            target.removePotionEffect(PotionEffectType.CONFUSION);
            target.removePotionEffect(PotionEffectType.BLINDNESS);
        } else if (secondsLeft <= 0) {
            target.teleport(player);
        } else {
            target.sendTitle("§5§lTeleportacja", "§9Nie ruszaj się, za §a " + secondsLeft + "s §9 zostaniesz przeteleportowany", 0, 22, 5);
            Bukkit.getScheduler().runTaskLater(GuildsAddons.plugin, () -> tpaLocalTimer(player, target, lastLocation,secondsLeft - 1), 20);
        }
    }
}