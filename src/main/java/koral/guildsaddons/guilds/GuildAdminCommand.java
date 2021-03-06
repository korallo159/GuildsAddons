package koral.guildsaddons.guilds;

import koral.guildsaddons.GuildsAddons;
import koral.guildsaddons.database.statements.PlayersStatements;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Arrays;
import java.util.List;

public class GuildAdminCommand implements TabExecutor {
    //usuwanie gildii UPDATE `Players` SET `guild` = NULL WHERE `Players`.`NICK` = 'korallo';
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return false;
        switch(args[0]){
            case"usunzgildii":
                Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.getPlugin(), () -> PlayersStatements.setGuild(args[1], null));
                sender.sendMessage("Usunales gracza " + args[1] + " z gildii");
                break;
            case"dodajpunkty":
                if(args.length == 1) return false;
                Bukkit.getScheduler().runTaskAsynchronously(GuildsAddons.getPlugin(), () -> PlayersStatements.setPointsData(args[1], Double.valueOf(args[2])));
                sender.sendMessage("Dodales graczowi punkty.");
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return args.length <= 1 ? Arrays.asList("usunzgildii", "dodajpunkty") : null;
    }
}
