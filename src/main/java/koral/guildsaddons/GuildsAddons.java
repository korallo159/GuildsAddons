package koral.guildsaddons;
import koral.guildsaddons.commands.SetRtp;
import koral.guildsaddons.listeners.PlayerInteract;
import koral.guildsaddons.listeners.PluginChannelListener;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


public final class GuildsAddons extends JavaPlugin implements Listener {
//TODO:Random TP pomiedzy serwerami i dostepnymi kordami
//TODO: anty logout z mimi, drop ze stone z mimi,
//TODO: COBBLEX - Z 9 stakow cobbla w craftingu, dostajesz cobbleX, jak postawisz to wylosowuje jakis losowy item z configa
//TODO: STONIARKI - Nad specjalnie zcraftowanym itemem respi sie stone
//TODO: DROP ZE STONE - Wylaczony drop z zwyklych rud, zamiast tego szanse na to ze wydropia z zwyklego stone surowce.
//TODO: Autofosa - Crafting jakiegos itema co kopie do dolu
//TODO: turbo drop - /turbodrop <nick> <czas> zwieksza komus drop, do zakupienia w sklepie albo eventy
//TODO: lootbagi z mimi

    public static GuildsAddons plugin;
    @Override
    public void onEnable() {
        plugin = this;
        this.saveDefaultConfig();
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PluginChannelListener());
        getServer().getPluginManager().registerEvents(new PlayerInteract(), this);
        getCommand("setrtp").setExecutor(new SetRtp());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static GuildsAddons getPlugin() {
        return plugin;
    }
}
