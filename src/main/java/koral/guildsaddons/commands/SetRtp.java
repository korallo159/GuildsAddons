package koral.guildsaddons.commands;

import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.managers.ConfigManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class SetRtp implements CommandExecutor {

    public static ConfigManager rtpConfig = new ConfigManager("rtp.yml");
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Aby ustawić RTP wpisz /setrtp confirm, pamiętaj, że musisz patrzeć na przycisk i musi to być stone button.");
                return true;
            }
            if(args[0].equalsIgnoreCase("confirm")) {
                int rtps = rtpConfig.getConfig().getConfigurationSection("rtps").getKeys(false).size();
                rtpConfig.getConfig().set("rtps." + (rtps + 1), ((Player) sender).getTargetBlock(5).getLocation());
                rtpConfig.save();
                sender.sendMessage(ChatColor.GREEN + "Ustawiles RTP.");
            }
            if(args[0].equalsIgnoreCase("delete")){
                for(String s: rtpConfig.getConfig().getConfigurationSection("rtps").getKeys(true)){
                   Location loc = rtpConfig.getConfig().getLocation("rtps." + s);
                        if(loc.equals(((Player) sender).getTargetBlock(5).getLocation())){
                            rtpConfig.getConfig().set("rtps." + s, null);
                            rtpConfig.save();
                            sender.sendMessage(ChatColor.RED + "Usunales RTP.");

                    }

                }
            }
        }
        return false;
    }
}
