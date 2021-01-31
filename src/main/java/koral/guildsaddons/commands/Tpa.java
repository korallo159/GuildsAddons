package koral.guildsaddons.commands;

import koral.guildsaddons.listeners.PluginChannelListener;;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Tpa implements CommandExecutor {
    //TODO lepsze prefixy itd
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(command.getName().equalsIgnoreCase("tpa") && args.length == 1)
            PluginChannelListener.sendTpaRequest("TpaChannel", "ALL", (Player) sender, args[0]);
        if(command.getName().equalsIgnoreCase("tpaccept"))
            if(PluginChannelListener.tpaMap.containsKey(sender.getName())){
                System.out.println(PluginChannelListener.tpaMap.get(sender.getName()));
                PluginChannelListener.sendTpaAcceptation("TpaChannel", "ALL", (Player) sender, PluginChannelListener.tpaMap.get(sender.getName()), true);//tu
                sender.sendMessage(ChatColor.GREEN + "Zaakceptowales prośbę o teleportacje, gracz pojawi się obok Ciebie");
                PluginChannelListener.tpaMap.remove(sender.getName());
            }
        if(command.getName().equalsIgnoreCase("tpdeny")){
            if(PluginChannelListener.tpaMap.containsKey(sender.getName())) {
                PluginChannelListener.sendTpaAcceptation("TpaChannel", "ALL", (Player) sender, PluginChannelListener.tpaMap.get(sender.getName()), false);
                sender.sendMessage(ChatColor.GREEN + "Odrzuciłeś prośbę o teleportację");
            }
            else sender.sendMessage(ChatColor.RED + "Nie posiadasz żadnej prośby o teleportacje!");
        }
        //send Pluginmessage
        return true;
    }
}