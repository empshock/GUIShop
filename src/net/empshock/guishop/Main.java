package net.empshock.guishop;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.empshock.guishop.utils.ConfigManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main
        extends JavaPlugin
{
    public static Main instance;
    public static Economy econ = null;
    public static Permission permission = null;

    public static Main getInstance()
    {
        return instance;
    }

    public String title = ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "GUIShop" + ChatColor.GOLD + "] " + ChatColor.GRAY;

    public void onEnable()
    {
        instance = this;
        setupPlugin();
        getServer().getConsoleSender().sendMessage(this.title + " has been enabled!");
    }

    public void onDisable()
    {
        getServer().getConsoleSender().sendMessage(this.title + " has been disabled.");
        instance = null;
    }

    private void setupPlugin()
    {
        ConfigManager.load(this, "buyItems.yml");
        ConfigManager.load(this, "sellItems.yml");
        ConfigManager.load(this, "enchantItems.yml");
        this.title = ChatColor.translateAlternateColorCodes('&', ConfigManager.get("buyItems.yml").getString("plugin-header", "&6[&3Shop&6]&7") + " ");

        new Commands();
        Commands cmd = new Commands();
        getCommand("shop").setExecutor(cmd);

        Bukkit.getServer().getPluginManager().registerEvents(new Listeners(), this);
        for (int i = 0; i < 100; i++) {
            if (i == 100)
            {
                if (!setupEconomy())
                {
                    System.out.println("No vault!");
                    getServer().getPluginManager().disablePlugin(this);
                }
                if (!setupPermissions())
                {
                    System.out.println("No permissions!");
                    getServer().getPluginManager().disablePlugin(this);
                }
            }
        }
        Set<String> enchants = ConfigManager.get("enchantItems.yml").getKeys(false);
        for (String s : enchants) {
            if (ConfigManager.get("enchantItems.yml").getConfigurationSection(s) != null)
            {
                List<String> ids = ConfigManager.get("enchantItems.yml").getConfigurationSection(s).getStringList("availableItems");
                Listeners.availableIds.put(s, ids);
                for (String id : ids) {
                    if (!Listeners.validIds.contains(Integer.valueOf(Integer.parseInt(id)))) {
                        Listeners.validIds.add(Integer.valueOf(Integer.parseInt(id)));
                    }
                }
            }
        }
    }

    private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            econ = (Economy)economyProvider.getProvider();
        }
        return econ != null;
    }

    private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
        if (permissionProvider != null) {
            permission = (Permission)permissionProvider.getProvider();
        }
        return permission != null;
    }
}

