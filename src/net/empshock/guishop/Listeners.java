package net.empshock.guishop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import net.empshock.guishop.utils.ConfigManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.scheduler.BukkitRunnable;

public class Listeners
        implements Listener
{
    private static final Economy econ = (Economy)Bukkit.getServer().getServicesManager().getRegistration(Economy.class).getProvider();
    private static ArrayList<UUID> enchanting = new ArrayList();
    static HashMap<String, List<String>> availableIds = new HashMap();
    static ArrayList<Integer> validIds = new ArrayList();

    private static ItemStack getColoredPane(String name, Short damage)
    {
        ItemStack coloredPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, damage.shortValue());
        ItemMeta im = coloredPane.getItemMeta();
        im.setDisplayName(name);
        coloredPane.setItemMeta(im);
        return coloredPane;
    }

    public void openEnchantMenu(Player player)
    {
        try
        {
            if (ConfigManager.get("enchantItems.yml") == null) {
                throw new Exception(" failed to load 'enchantItems.yml'.");
            }
            String menu_title = ChatColor.translateAlternateColorCodes('&', ConfigManager.get("enchantItems.yml").getString("title"));

            final Inventory inv = Bukkit.createInventory(null, 27, menu_title);
            for (int i = 0; i < 27; i++)
            {
                ItemStack border = getColoredPane(" ", Short.valueOf((short)15));
                ItemStack close = getColoredPane(ChatColor.RED + "CLOSE", Short.valueOf((short)14));
                ItemStack noItem = getColoredPane(ChatColor.GRAY  + "enchantment unavailable", Short.valueOf((short)0));
                if ((i < 9) || (i == 9) || (i > 16)) {
                    inv.setItem(i, border);
                }
                if ((i > 11) && (i < 17)) {
                    inv.setItem(i, noItem);
                }
                if (i == 11) {
                    inv.setItem(i, close);
                }
            }
            new BukkitRunnable()
            {
                public void run()
                {
                    Listeners.this.openInventory(inv);
                }
            }.runTaskLater(Main.getInstance(), 10L);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Bukkit.getServer().getConsoleSender().sendMessage(Main.getInstance().title + ("GUIShop has stopped working. Please contact empshock"));
        }
    }

    public void openMainMenu(Player player)
    {
        try
        {
            if (ConfigManager.get("buyItems.yml") == null) {
                throw new Exception(" failed to load 'buyItems.yml'.");
            }
            String menu_title = ChatColor.translateAlternateColorCodes('&', ConfigManager.get("buyItems.yml").getConfigurationSection("main_menu").getString("title"));
            Integer menu_slots = Integer.valueOf(ConfigManager.get("buyItems.yml").getConfigurationSection("main_menu").getInt("slots"));

            final Inventory inv = Bukkit.createInventory(null, menu_slots.intValue(), menu_title);
            for (int i = 0; i <= menu_slots.intValue(); i++)
            {
                if (i == 0) {
                    i++;
                }
                if (ConfigManager.get("buyItems.yml").getConfigurationSection("categories").getConfigurationSection(String.valueOf(i)) == null) {
                    throw new Exception(" failed to find category section: 'categories." + String.valueOf(i) + "'.");
                }
                ConfigurationSection menu = ConfigManager.get("buyItems.yml").getConfigurationSection("categories").getConfigurationSection(String.valueOf(i));

                String description = ChatColor.translateAlternateColorCodes('&', menu.getString("description"));
                String category_title = ChatColor.translateAlternateColorCodes('&', menu.getString("title"));
                String category_icon = menu.getString("icon");

                ItemStack item = null;
                Integer id = null;
                Short durability = null;
                if (category_icon.contains(":"))
                {
                    String[] ITEM_ID = category_icon.split(":");
                    id = Integer.valueOf(Integer.parseInt(ITEM_ID[0]));
                    durability = Short.valueOf(Short.parseShort(ITEM_ID[1]));
                    item = new ItemStack(id.intValue(), 1, durability.shortValue());
                }
                else
                {
                    id = Integer.valueOf(Integer.parseInt(category_icon));
                    item = new ItemStack(id.intValue(), 1);
                }
                ItemMeta itemMeta = item.getItemMeta();
                if ((category_title != null) && (!category_title.isEmpty())) {
                    itemMeta.setDisplayName(category_title);
                }
                itemMeta.setLore(Arrays.asList(new String[] { description }));
                item.setItemMeta(itemMeta);

                inv.setItem(i - 1, item);
            }
            new BukkitRunnable()
            {
                public void run()
                {
                    Listeners.this.openInventory(inv);
                }
            }.runTaskLater(Main.getInstance(), 10L);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Bukkit.getServer().getConsoleSender().sendMessage(Main.getInstance().title + "Failed to load Main GUI, due to error: " + ex.getCause());
        }
    }

    private void openInventory(Inventory inv) {
    }

    @EventHandler
    public void onExitToMainMenu(InventoryCloseEvent event)
    {
        if ((event.getPlayer() == null) || (!(event.getPlayer() instanceof Player))) {
            return;
        }
        final Player player = (Player)event.getPlayer();

        Set<String> categories = ConfigManager.get("buyItems.yml").getConfigurationSection("categories").getKeys(false);
        ArrayList<String> titles = new ArrayList();
        for (String category : categories) {
            titles.add(ChatColor.translateAlternateColorCodes('&', ConfigManager.get("buyItems.yml").getConfigurationSection("categories." + category).getString("title")));
        }
        if (titles.contains(event.getInventory().getTitle())) {
            new BukkitRunnable()
            {
                public void run()
                {
                }
            }.runTaskLater(Main.getInstance(), 10L);
        }
    }
   @EventHandler
    public void onSellItems(InventoryCloseEvent event)
    {
        if ((event.getPlayer() == null) || (!(event.getPlayer() instanceof Player))) {
            return;
        }
        Player player = (Player)event.getPlayer();
        Inventory inv = event.getInventory();
        if ((inv.getTitle().contains("Sell Items")) && (inv.getContents() != null) && (inv.getContents().length > 0))
        {
            Set<String> sellItems = ConfigManager.get("sellItems.yml").getConfigurationSection("").getKeys(false);
            ArrayList<ItemStack> notAllowed = new ArrayList();
            double profit = 0.0D;
            if ((sellItems == null) || (sellItems.isEmpty()))
            {
                {
                }
            }
            econ.depositPlayer(player, profit);
            player.sendMessage(Main.getInstance().title + "$" + profit + " has been deposited into your account.");
            player.updateInventory();
        }
    }

    @EventHandler
    public void onClickCategory(InventoryClickEvent event)
    {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player)event.getWhoClicked();

        Set<String> categories = ConfigManager.get("buyItems.yml").getConfigurationSection("categories").getKeys(false);
        ArrayList<String> titles = new ArrayList();
        for (String category : categories) {
            titles.add(ChatColor.translateAlternateColorCodes('&', ConfigManager.get("buyItems.yml").getConfigurationSection("categories." + category).getString("title")));
        }
        if (!titles.contains(event.getInventory().getTitle())) {
            return;
        }
        if ((event.getCurrentItem() == null) || (!event.getCurrentItem().hasItemMeta()) || (!event.getCurrentItem().getItemMeta().hasLore()))
        {
            event.setCancelled(true);
            return;
        }
        String lorePrice = ChatColor.stripColor(((String)event.getCurrentItem().getItemMeta().getLore().get(0)).replace('$', ' ').split(":")[1].replaceAll(" ", ""));
        double price = 0.0D;
        if (!event.getClick().equals(ClickType.SHIFT_LEFT)) {
            price = Double.parseDouble(lorePrice);
        } else {
            price = Double.parseDouble(lorePrice) * event.getCurrentItem().getMaxStackSize();
        }
        EconomyResponse r = econ.withdrawPlayer(player, price);
        if (r.transactionSuccess())
        {
            ItemStack purchase = new ItemStack(event.getCurrentItem().getTypeId());
            if (event.getCurrentItem().getDurability() != 0) {
                purchase.setDurability(event.getCurrentItem().getDurability());
            }
            if (event.getClick().equals(ClickType.SHIFT_LEFT))
            {
                purchase.setAmount(purchase.getMaxStackSize());
                event.getWhoClicked().getInventory().addItem(new ItemStack[] { purchase });
                player.sendMessage(Main.getInstance().title + "You bought a stack of " + event.getCurrentItem().getType().toString().toLowerCase().replaceAll("_", " ") + " for $" + price + ".");
            }
            else
            {
                event.getWhoClicked().getInventory().addItem(new ItemStack[] { purchase });
                player.sendMessage(Main.getInstance().title + "You bought a " + event.getCurrentItem().getType().toString().toLowerCase().replaceAll("_", " ") + " for $" + price + ".");
            }
            player.updateInventory();
        }
        else
        {
            player.sendMessage(Main.getInstance().title + "You do not have enough money!");
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onClickMainMenu(InventoryClickEvent event)
    {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        final Player player = (Player)event.getWhoClicked();
        int clickedSlot = event.getRawSlot() + 1;

        String main_title = ChatColor.stripColor(ConfigManager.get("buyItems.yml").getConfigurationSection("main_menu").getString("title"));
        if (!main_title.contains(ChatColor.stripColor(event.getInventory().getTitle()))) {
            return;
        }
        if ((event.getCurrentItem() != null) && (event.getCurrentItem().hasItemMeta()))
        {
            ConfigurationSection menu = ConfigManager.get("buyItems.yml").getConfigurationSection("categories." + String.valueOf(clickedSlot));
            String category_title = ChatColor.translateAlternateColorCodes('&', menu.getString("title"));
            Integer category_slots = Integer.valueOf(menu.getInt("slots"));

            final Inventory inv = Bukkit.createInventory(null, category_slots.intValue(), category_title);
            Set<String> categoryItems = menu.getKeys(false);
            categoryItems.remove("icon");
            categoryItems.remove("title");
            categoryItems.remove("description");
            categoryItems.remove("slots");
            for (String categoryItem : categoryItems)
            {
                ConfigurationSection categoryMenu = ConfigManager.get("buyItems.yml").getConfigurationSection("categories." + String.valueOf(clickedSlot) + "." + categoryItem);

                String icon = categoryMenu.getString("item");
                String buyPrice = categoryMenu.getString("price");
                String sellPrice = ConfigManager.get("sellItems.yml").getString(icon);
                String name = ChatColor.translateAlternateColorCodes('&', categoryMenu.getString("name"));

                ItemStack item = null;
                Integer id = null;
                Short durability = null;
                if (icon.contains(":"))
                {
                    String[] ITEM_ID = icon.split(":");
                    id = Integer.valueOf(Integer.parseInt(ITEM_ID[0]));
                    durability = Short.valueOf(Short.parseShort(ITEM_ID[1]));
                    item = new ItemStack(id.intValue(), 1, durability.shortValue());
                }
                else
                {
                    id = Integer.valueOf(Integer.parseInt(icon));
                    item = new ItemStack(id.intValue(), 1);
                }
                ItemMeta itemMeta = item.getItemMeta();
                if ((name != null) && (!name.isEmpty())) {
                    itemMeta.setDisplayName(name);
                }
                itemMeta.setLore(Arrays.asList(new String[] { ChatColor.GRAY + "Buy: " + ChatColor.GREEN + "$" + buyPrice, (sellPrice != null) && (!sellPrice.isEmpty()) ? ChatColor.GRAY + "Sell: " + ChatColor.RED + "$" + sellPrice : "" }));
                item.setItemMeta(itemMeta);
                inv.setItem(Integer.valueOf(categoryItem).intValue(), item);
            }
            event.setCancelled(true);

            new BukkitRunnable()
            {
                public void run()
                {
                    player.closeInventory();
                    player.openInventory(inv);
                }
            }.runTaskLater(Main.getInstance(), 10L);
        }
    }

    @EventHandler
    private void onSelectEnchantMenu(InventoryClickEvent event)
    {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        String menu_title = ChatColor.translateAlternateColorCodes('&', ConfigManager.get("enchantItems.yml").getString("title"));
        ItemStack noItem = getColoredPane(ChatColor.GRAY + "enchantment unavailable", Short.valueOf((short)0));
        Player player = (Player)event.getWhoClicked();
        Enchantment enchant = null;
        if (!event.getInventory().getTitle().contains(menu_title)) {
            return;
        }
        event.setCancelled(true);
        if ((event.getInventory().getItem(10) == null) || (event.getSlot() < 12) || (event.getSlot() > 16)) {
            return;
        }
        if ((event.getCurrentItem() == null) || (event.getCurrentItem().getItemMeta() == null) || (event.getCurrentItem().getEnchantments() == null)) {
            return;
        }
        for (Enchantment e : event.getCurrentItem().getEnchantments().keySet()) {
            enchant = e;
        }
        int amplifier = ConfigManager.get("enchantItems.yml").getInt(enchant.getName().toUpperCase() + ".amplifier");
        double price = ConfigManager.get("enchantItems.yml").getDouble(enchant.getName().toUpperCase() + ".price");
        EconomyResponse r = econ.withdrawPlayer(player, price);
        ItemStack c = event.getInventory().getItem(10).clone();
        if (r.transactionSuccess())
        {
            c.addUnsafeEnchantment(enchant, amplifier);
            player.getInventory().addItem(new ItemStack[] { c });
        }
        else
        {
            player.closeInventory();
            player.sendMessage(Main.getInstance().title + "You do not have enough money!");
        }
        for (int ii = 16; ii > 11; ii--) {
            event.getInventory().setItem(ii, noItem);
        }
        event.getInventory().setItem(10, null);
        player.updateInventory();
    }

    @EventHandler
    private void onUseEnchantMenuUse(InventoryClickEvent event)
    {
        if ((!(event.getWhoClicked() instanceof Player)) || (event.getCurrentItem() == null)) {
            return;
        }
        if (!enchanting.contains(event.getWhoClicked().getUniqueId())) {
            return;
        }
        String menu_title = ChatColor.translateAlternateColorCodes('&', ConfigManager.get("enchantItems.yml").getString("title"));
        if (!event.getInventory().getTitle().contains(menu_title)) {
            return;
        }
        event.setCancelled(true);
        Player player = (Player)event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem().clone();
        ItemStack itemSlot = event.getInventory().getItem(10) != null ? event.getInventory().getItem(10).clone() : null;
        if ((event.getSlot() == 10) && (itemSlot != null) && (itemSlot.getType() == clickedItem.getType()) && (itemSlot.getDurability() == clickedItem.getDurability()))
        {
            ItemStack noItem = getColoredPane(ChatColor.GRAY + "enchantment unavailable", Short.valueOf((short)0));
            player.getInventory().addItem(new ItemStack[] { itemSlot.clone() });
            event.getInventory().setItem(10, null);
            for (int ii = 16; ii > 11; ii--) {
                event.getInventory().setItem(ii, noItem);
            }
            player.updateInventory();
            return;
        }
        if ((event.getSlot() == 11) && (clickedItem.getType() == Material.STAINED_GLASS_PANE))
        {
            if (itemSlot != null)
            {
                player.getInventory().addItem(new ItemStack[] { itemSlot.clone() });
                event.getInventory().setItem(10, null);
            }
            player.closeInventory();
            player.updateInventory();
            return;
        }
        if (player.getInventory().contains(clickedItem)) {
            if (validIds.contains(Integer.valueOf(clickedItem.getTypeId())))
            {
                event.setCurrentItem(null);
                if ((itemSlot != null) && (itemSlot.getType() == clickedItem.getType()) && (itemSlot.getDurability() == clickedItem.getDurability()))
                {
                    ItemStack stack = clickedItem.clone();
                    boolean same = true;
                    Enchantment[] arrayOfEnchantment;
                    int j = (arrayOfEnchantment = Enchantment.values()).length;
                    for (int i = 0; i < j; i++)
                    {
                        Enchantment e = arrayOfEnchantment[i];
                        if (((itemSlot.containsEnchantment(e)) && (!stack.containsEnchantment(e))) || ((stack.containsEnchantment(e)) && (!itemSlot.containsEnchantment(e))))
                        {
                            same = false;
                            break;
                        }
                    }
                    if (!same)
                    {
                        if (itemSlot != null) {
                            player.getInventory().addItem(new ItemStack[] { itemSlot.clone() });
                        }
                        event.getInventory().setItem(10, clickedItem.clone());
                    }
                    else
                    {
                        stack.setAmount(itemSlot.getAmount() + stack.getAmount());
                        event.getInventory().setItem(10, stack);
                    }
                }
                else
                {
                    if (itemSlot != null) {
                        player.getInventory().addItem(new ItemStack[] { itemSlot.clone() });
                    }
                    event.getInventory().setItem(10, clickedItem.clone());
                }
                updateEnchantments(event.getInventory());
                player.updateInventory();
            }
        }
    }

    private ItemStack getEnchantDisplay(ItemStack item, Enchantment enchant, Integer amplifier, Double price)
    {
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.DARK_GRAY  + enchant.getName());
        itemMeta.setLore(Arrays.asList(new String[] { ChatColor.GRAY + "Price: " + ChatColor.GREEN + "$" + String.valueOf(price) }));
        for (Enchantment e : itemMeta.getEnchants().keySet()) {
            itemMeta.removeEnchant(e);
        }
        item.setItemMeta(itemMeta);
        item.addUnsafeEnchantment(enchant, amplifier.intValue());
        return item;
    }

    private void updateEnchantments(Inventory i)
    {
        Random r = new Random();
        ItemStack c = i.getItem(10);
        ArrayList<String> validEnchants = new ArrayList();
        for (String eName : availableIds.keySet())
        {
            List<String> ids = (List)availableIds.get(eName);
            if (ids.contains(String.valueOf(c.getTypeId()))) {
                validEnchants.add(eName);
            }
        }
        for (int ii = 16; ii > 11; ii--)
        {
            if (validEnchants.isEmpty()) {
                break;
            }
            Enchantment randomEnchant = Enchantment.getByName(validEnchants.size() > 1 ? (String)validEnchants.get(r.nextInt(validEnchants.size())) : (String)validEnchants.get(0));
            int amplifier = ConfigManager.get("enchantItems.yml").getInt(randomEnchant.getName().toUpperCase() + ".amplifier");
            double price = ConfigManager.get("enchantItems.yml").getDouble(randomEnchant.getName().toUpperCase() + ".price");
            validEnchants.remove(randomEnchant.getName());

            i.setItem(ii, getEnchantDisplay(c.clone(), randomEnchant, Integer.valueOf(amplifier), Double.valueOf(price)));
        }
    }

    @EventHandler
    public void onCloseEnchantMenu(InventoryCloseEvent event)
    {
        if ((event.getPlayer() == null) || (!(event.getPlayer() instanceof Player)) || (event.getInventory().getTitle() == null)) {
            return;
        }
        String menu_title = ChatColor.translateAlternateColorCodes('&', ConfigManager.get("enchantItems.yml").getString("title"));
        final Player player = (Player)event.getPlayer();
        if (!event.getInventory().getTitle().contains(menu_title)) {
            return;
        }
        if (event.getInventory().getItem(10) != null) {
            player.getInventory().addItem(new ItemStack[] { event.getInventory().getItem(10).clone() });
        }
        enchanting.remove(player.getUniqueId());
        player.updateInventory();
        new BukkitRunnable()
        {
            public void run()
            {
                player.updateInventory();
            }
        }.runTaskLater(Main.getInstance(), 20L);
    }
}


