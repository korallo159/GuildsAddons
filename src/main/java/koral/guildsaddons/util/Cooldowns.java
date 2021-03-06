package koral.guildsaddons.util;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class Cooldowns {
    private HashMap<String, Long> cooldown;

    public Cooldowns(HashMap<String, Long> cooldown) {
        this.cooldown = cooldown;
    }

    public void setSystemTime(Player player){
        cooldown.put(player.getUniqueId().toString(), (System.currentTimeMillis() / 1000));

    }
    public void setSystemTime(Player player, Integer additionaltime){
        cooldown.put(player.getUniqueId().toString(), (System.currentTimeMillis() / 1000 + additionaltime));

    }
    public boolean hasCooldown(Player player, Integer time) {
        if (cooldown.containsKey(player.getUniqueId().toString())) {
            if (cooldown.get(player.getUniqueId().toString()) + time >= (System.currentTimeMillis() / 1000)) {
                return true;
            } else
                return false;
        }
        else
            return false;
    }

    public boolean hasCooldown(Player player, Integer time, String message) {
        if (cooldown.containsKey(player.getUniqueId().toString())) {
            if (cooldown.get(player.getUniqueId().toString()) + time >= (System.currentTimeMillis() / 1000)) {
      //                 player.sendMessage(ChatColor.RED +  "Musisz odczekać jeszcze " + (cooldown.get(player.getUniqueId().toString()) + time - System.currentTimeMillis() / 1000) + "sekund aby to zrobić");
                player.sendMessage(message);
                return true;
            } else
                return false;
        }
        else
            return false;
    }
}