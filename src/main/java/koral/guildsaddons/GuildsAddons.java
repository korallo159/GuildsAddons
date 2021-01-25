package koral.guildsaddons;

import koral.guildsaddons.listeners.PluginChannelListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
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

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PluginChannelListener());
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
