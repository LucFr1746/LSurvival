package io.github.lucfr1746.LEnchants.EnchantingTable;

import io.github.lucfr1746.LEnchants.EnchantingTable.Guide.EnchantmentsGuideGUI;
import io.github.lucfr1746.LEnchants.EnchantingTable.Guide.EnchantmentsGuideSubGUI;
import io.github.lucfr1746.LEnchants.EnchantingTable.SubGUI.EnchantmentGUI;
import io.github.lucfr1746.LEnchants.LEnchants;
import io.github.lucfr1746.LSurvivalLib.Enchantments.Enchantment;
import io.github.lucfr1746.LSurvivalLib.Inventory.InventoryListener;
import io.github.lucfr1746.LSurvivalLib.Inventory.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EnchantingTableManager {

    private static LEnchants plugin;
    private static InventoryManager inventoryManager;

    public EnchantingTableManager(LEnchants plugin) {
        EnchantingTableManager.plugin = plugin;
        inventoryManager = new InventoryManager();
        InventoryListener guiListener = new InventoryListener(inventoryManager);
        Bukkit.getPluginManager().registerEvents(guiListener, plugin);
    }

    public static void openEnchantingTable(Player player, int power) {
        inventoryManager.openGUI(plugin, new EnchantingGUI(plugin, power), player);
    }

    public static void openEnchantingTable(Player player, int power, ItemStack currentEnchantingItem) {
        inventoryManager.openGUI(plugin, new EnchantingGUI(plugin, power, currentEnchantingItem), player);
    }

    public static void openEnchantmentGUI(Player player, ItemStack currentEnchantingItem, Enchantment enchantment, int power) {
        inventoryManager.openGUI(plugin, new EnchantmentGUI(currentEnchantingItem, enchantment, power), player);
    }

    public static void openEnchantmentsGuide(Player player, int page, int power) {
        inventoryManager.openGUI(plugin, new EnchantmentsGuideGUI(page, power), player);
    }

    public static void openEnchantmentsGuideSubGUI(Player player, Enchantment enchantment, int backPage,int power) {
        inventoryManager.openGUI(plugin, new EnchantmentsGuideSubGUI(enchantment, backPage, power), player);
    }
}
