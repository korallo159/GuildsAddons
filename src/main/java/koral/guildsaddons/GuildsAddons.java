package koral.guildsaddons;

import koral.guildsaddons.commands.SetRtp;
import koral.guildsaddons.commands.Sethome;
import koral.guildsaddons.commands.Tpa;
import koral.guildsaddons.database.DatabaseConnection;
import koral.guildsaddons.database.statements.Table;
import koral.guildsaddons.listeners.*;
import koral.guildsaddons.managers.ConfigManager;
import koral.guildsaddons.schowek.InventoryClickListener;
import koral.guildsaddons.schowek.InventoryCloseListener;
import koral.guildsaddons.schowek.ItemPickUpListener;
import koral.guildsaddons.schowek.Schowek;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


public final class GuildsAddons extends JavaPlugin implements Listener {
//TODO: v anty logout z mimi, drop ze stone z mimi, //mimiAntylog
//TODO: v turbo drop - /turbodrop <nick> <czas> zwieksza komus drop, do zakupienia w sklepie albo eventy
//TODO: v DROP ZE STONE - Wylaczony drop z zwyklych rud, zamiast tego szanse na to ze wydropia z zwyklego stone surowce.
//TODO: exp z dropu

//TODO: v na lootbagu - COBBLEX - Z 9 stakow cobbla w craftingu, dostajesz cobbleX, jak postawisz to wylosowuje jakis losowy item z configa
//TODO: v STONIARKI - Nad specjalnie zcraftowanym itemem respi sie stone
//TODO: v Autofosa - Crafting jakiegos itema co kopie do dolu
//TODO: lootbagi z mimi

//TODO: Kosz z mimi
//TODO: Tpa miedzy serwerami
//TODO: rzucane tnt

    public static GuildsAddons plugin;
    @Override
    public void onEnable() {
        plugin = this;

        this.saveDefaultConfig();

        config = new ConfigManager("items.yml");
        reloadPlugin();

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PluginChannelListener());
        getServer().getPluginManager().registerEvents(new PlayerInteract(), this);
        getServer().getPluginManager().registerEvents(new DeletenMending(), this);
        getServer().getPluginManager().registerEvents(new DropCollector(), this);
        getServer().getPluginManager().registerEvents(new Stoniarki(), this);
        getServer().getPluginManager().registerEvents(new Boyki(), this);
        getServer().getPluginManager().registerEvents(new Cobblex(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
        getServer().getPluginManager().registerEvents(new ThrowingTnt(), this);

        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryCloseListener(), this);
        getServer().getPluginManager().registerEvents(new ItemPickUpListener(), this);
        getCommand("schowek").setExecutor(new Schowek());

        StoneDrop stoneDrop = new StoneDrop();
        getServer().getPluginManager().registerEvents(stoneDrop, this);
        getCommand("turbodrop").setExecutor(stoneDrop);
        getCommand("drop").setExecutor(stoneDrop);
        Tpa tpa = new Tpa();
        getCommand("tpa").setExecutor(tpa);
        getCommand("tpaccept").setExecutor(tpa);
        getCommand("tpdeny").setExecutor(tpa);
        getCommand("setrtp").setExecutor(new SetRtp());

        Sethome setHome = new Sethome();
        getCommand("sethome").setExecutor(setHome);
        getCommand("delhome").setExecutor(setHome);
        getCommand("home").setExecutor(setHome);

        DatabaseConnection.configureDbConnection();
        Table.createTable();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static GuildsAddons getPlugin() {
        return plugin;
    }

    private ConfigManager config;
    public static void reloadPlugin() {
        plugin.config.reloadCustomConfig();
        plugin.reloadConfig();

        Boyki.itemAutoFosa = plugin.config.config.getItemStack("AutoFosa");
        Boyki.itemBoyFarmer = plugin.config.config.getItemStack("BoyFarmer");
        Boyki.itemSandFarmer = plugin.config.config.getItemStack("SandFarmer");
        Boyki.itemAutoFosa = plugin.config.config.getItemStack("AutoFosa");
        Cobblex.itemCobblex = plugin.config.config.getItemStack("Cobblex");
        Stoniarki.itemStoniark = plugin.config.config.getItemStack("Stoniarki");
        ThrowingTnt.item = plugin.config.config.getItemStack("ThrowingTnt");

        ThrowingTnt.power_of_explosion = plugin.getConfig().getInt("ThrowingTnt.power_of_explosion");
        ThrowingTnt.power_of_throwing = plugin.getConfig().getInt("ThrowingTnt.power_of_throwing");
        ThrowingTnt.ticks_to_explosion = plugin.getConfig().getInt("ThrowingTnt.ticks_to_explosion");

        StoneDrop.reload();
        Stoniarki.reload();
        Schowek.reload();
    }
}
