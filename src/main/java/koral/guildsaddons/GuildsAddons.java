package koral.guildsaddons;

import com.sk89q.worldedit.extent.clipboard.io.SpongeSchematicWriter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import koral.guildsaddons.commands.Is;
import koral.guildsaddons.commands.SetRtp;
import koral.guildsaddons.commands.Sethome;
import koral.guildsaddons.commands.rtp;
import koral.guildsaddons.database.DatabaseConnection;
import koral.guildsaddons.database.statements.Table;
import koral.guildsaddons.guilds.CustomTabList;
import koral.guildsaddons.guilds.GuildAdminCommand;
import koral.guildsaddons.guilds.GuildCommand;
import koral.guildsaddons.guilds.GuildSocketForwardChannelListener;
import koral.guildsaddons.listeners.*;
import koral.guildsaddons.managers.ConfigManager;
import koral.guildsaddons.schowek.ItemPickUpListener;
import koral.guildsaddons.schowek.PlayerInteractListener;
import koral.guildsaddons.schowek.Schowek;
import koral.guildsaddons.simpleThings.*;
import koral.guildsaddons.util.Pair;
import koral.guildsaddons.util.PanelYesNo;
import koral.sectorserver.SectorServer;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;


public final class GuildsAddons extends JavaPlugin implements Listener {
    public static WorldGuardPlugin rg;
    public static final StateFlag flagGuildCreate = new StateFlag("guildCreating", true);

    public static GuildsAddons plugin;

    public static Objective pointsObjective;

    @Override
    public void onLoad() {
        plugin = this;

        rg = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        WorldGuard.getInstance().getFlagRegistry().register(flagGuildCreate);
    }
    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        pointsObjective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("pkt");
        if (pointsObjective == null) {
            pointsObjective = Bukkit.getScoreboardManager().getMainScoreboard().registerNewObjective("pkt", "dummy", "pkt");
            pointsObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        }

        włączVault();

        config = new ConfigManager("items.yml");
        reloadPlugin();

        //getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        //getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PluginChannelListener());
        getServer().getPluginManager().registerEvents(new PlayerInteract(), this);
        getServer().getPluginManager().registerEvents(new DeletenMending(), this);
        getServer().getPluginManager().registerEvents(new DropCollector(), this);
        getServer().getPluginManager().registerEvents(new Stoniarki(), this);
        getServer().getPluginManager().registerEvents(new Boyki(), this);
        getServer().getPluginManager().registerEvents(new Cobblex(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuit(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
        getServer().getPluginManager().registerEvents(new ThrowingTnt(), this);
        getServer().getPluginManager().registerEvents(new EntityExplodeListener(), this);
        getServer().getPluginManager().registerEvents(new PrepareSmithingListener(), this);
        getServer().getPluginManager().registerEvents(new EntityDamageByEntityListener(), this);
        getServer().getPluginManager().registerEvents(new koral.guildsaddons.listeners.InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new koral.guildsaddons.listeners.InventoryCloseListener(), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        getServer().getPluginManager().registerEvents(new koral.guildsaddons.schowek.InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new koral.guildsaddons.schowek.InventoryCloseListener(), this);

        getServer().getPluginManager().registerEvents(new PlayerCommandPreprocessListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryCloseListener(), this);
        getServer().getPluginManager().registerEvents(new ItemPickUpListener(), this);
        getServer().getPluginManager().registerEvents(new PanelYesNo(), this);
        getServer().getPluginManager().registerEvents(new Is(), this);
        getCommand("schowek").setExecutor(new Schowek());

        StoneDrop stoneDrop = new StoneDrop();
        getServer().getPluginManager().registerEvents(stoneDrop, this);
        getCommand("turbodrop").setExecutor(stoneDrop);
        getCommand("drop").setExecutor(stoneDrop);
        getCommand("setrtp").setExecutor(new SetRtp());

        Sethome setHome = new Sethome();
        Is is = new Is();
        getCommand("sethome").setExecutor(setHome);
        getCommand("delhome").setExecutor(setHome);
        getCommand("home").setExecutor(setHome);
        getCommand("itemshop").setExecutor(is);
        getCommand("isadmin").setExecutor(is);
        getCommand("isedit").setExecutor(is);
        getCommand("gildia").setExecutor(new GuildCommand());
        getCommand("gadmin").setExecutor(new GuildAdminCommand());
        getCommand("rtp").setExecutor(new rtp());

        DatabaseConnection.configureDbConnection();
        Table.createTables();

        SectorServer.registerForwardChannelListener(GuildSocketForwardChannelListener.class);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> Bukkit.getOnlinePlayers().forEach(CustomTabList::updateOnlineAll), 0, 20 * 10);
    }

    public static Chat chat;
    static void włączVault() {
        try {
            if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
                RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
                rsp.getProvider();
                chat = plugin.getServer().getServicesManager().getRegistration(Chat.class).getProvider();
            }
        } catch (Throwable e) {
            System.out.println("Problem z Valuts");
        }
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

        PlayerDeathListener.elo_divider = plugin.getConfig().getDouble("elo.divider", 400);
        PlayerDeathListener.elo_exponent = plugin.getConfig().getDouble("elo.exponent", 10);
        PlayerDeathListener.elo_constants.clear();
        plugin.getConfig().getStringList("elo.constants").forEach(str -> {
            String min = str.substring(0, str.indexOf('-'));
            String max = str.substring(str.indexOf('-') + 1, str.indexOf(' '));
            String s = str.substring(str.indexOf(' ') + 1);
            PlayerDeathListener.elo_constants.put(new Pair<>(Integer.parseInt(min), Integer.parseInt(max)), Integer.parseInt(s));
        });
        PlayerCommandPreprocessListener.blockedCmds = plugin.getConfig().getStringList("blockedCmdOnOtherGuild");

        StoneDrop.reload();
        Stoniarki.reload();
        Schowek.reload();
        GuildCommand.reloadGuildItems();
    }


    public static int[] slots(int neededSlots, int rows) {
        if (rows == 1)
            switch (neededSlots) {
                case 0: return new int[] {};
                case 1: return new int[] {4};
                case 2: return new int[] {3, 5};
                case 3: return new int[] {2, 4, 6};
                case 4: return new int[] {1, 3, 5, 7};
                case 5: return new int[] {2, 3, 4, 5, 6};
                case 6: return new int[] {1, 2, 3, 5, 6, 7};
                case 7: return new int[] {1, 2, 3, 4, 5, 6, 7};
                case 8: return new int[] {0, 1, 2, 3, 5, 6, 7, 8};
                case 9: return new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8};
                default:return null;
            }


        int[] sloty = new int[neededSlots];

        int ubytek = neededSlots / rows;
        int reszta = neededSlots % rows;

        int index = 0;

        int mn = 0;

        while (neededSlots > 0) {
            int dodatek = reszta-- > 0 ? 1 : 0;
            neededSlots -= ubytek + dodatek;
            for (int i : slots(ubytek + dodatek, 1))
                sloty[index++] = mn*9 + i;
            mn++;
        }

        return sloty;
    }
}
