package koral.guildsaddons.commands;

import koral.sectorserver.SectorServer;
import koral.sectorserver.util.Teleport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;
import java.util.function.UnaryOperator;

import static koral.guildsaddons.guilds.GuildCommand.msg;

public class rtp implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length < 1)
            return false;

        List<Entity> entities = Bukkit.selectEntities(commandSender, args[0]);
        if (entities.isEmpty())
            return msg(commandSender, "Nieporawny gracz");

        entities.forEach(p -> {
            if (p instanceof Player)
                tp((Player) p);
        });

        return msg(commandSender, "Przeteleportowano");
    }
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }

    static Random random = new Random();
    public static void tp(Player p) {
        UnaryOperator<Double> rand = shift -> random.nextInt(SectorServer.width) * (random.nextInt(SectorServer.serversPerSide()) + 1) + shift;
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 750, 1));
        Teleport.teleport(p, new Location(p.getWorld(),
                rand.apply(SectorServer.shiftX), 255, rand.apply(SectorServer.shiftZ)));
    }
}
