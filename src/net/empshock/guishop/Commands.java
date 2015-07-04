package net.empshock.guishop;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;

public class Commands
        implements CommandExecutor
{
    private static final Permission permission = (Permission)Bukkit.getServer().getServicesManager().getRegistration(Permission.class).getProvider();

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player)sender;
        if (cmd.getName().equalsIgnoreCase("shop"))
        {
            if (args.length != 1)
            {
                player.sendMessage(Main.getInstance().title + ChatColor.RED + "/Shop [Buy | Sell | Enchant]");
                return false;
            }
            if (args[0].equalsIgnoreCase("sell"))
            {
                if (!permission.has(player, "guishop.sell"))
                {
                    player.sendMessage(Main.getInstance().title + ChatColor.RED + "You do not have permission to do this.");
                    return false;
                }
                Inventory inv = Bukkit.createInventory(null, 36, "Sell Items");
                player.openInventory(inv);
                return false;
            }
            if (args[0].equalsIgnoreCase("buy"))
            {
                if (!permission.has(player, "guishop.buy"))
                {
                    player.sendMessage(Main.getInstance().title + ChatColor.RED + "You do not have permission to do this.");
                    return false;
                }
                return false;
            }
            if (args[0].equalsIgnoreCase("enchant"))
            {
                if (!permission.has(player, "guishop.enchant"))
                {
                    player.sendMessage(Main.getInstance().title + ChatColor.RED + "You do not have permission to do this.");
                    return false;
                }
                return false;
            }
            player.sendMessage(Main.getInstance().title + ChatColor.RED + "/shop [Buy | Sell | Enchant]");
            return false;
        }
        return true;
    }
}

