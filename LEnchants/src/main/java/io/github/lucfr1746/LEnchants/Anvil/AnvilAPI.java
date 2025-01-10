package io.github.lucfr1746.LEnchants.Anvil;

import io.github.lucfr1746.LEnchants.LEnchants;
import io.github.lucfr1746.LSurvivalLib.Enchantments.Enchantment;
import io.github.lucfr1746.LSurvivalLib.Enchantments.Utils.EnchantmentRegister;
import io.github.lucfr1746.LSurvivalLib.Enchantments.Utils.EnchantmentsLoader;
import io.github.lucfr1746.LSurvivalLib.Inventory.InventoryListener;
import io.github.lucfr1746.LSurvivalLib.Inventory.InventoryManager;
import io.github.lucfr1746.LSurvivalLib.ItemStack.ItemBuilderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class AnvilAPI {

    private static LEnchants plugin;

    private static InventoryManager inventoryManager;

    public AnvilAPI(LEnchants plugin) {
        AnvilAPI.plugin = plugin;
    }

    public void registerInventory() {
        AnvilAPI.inventoryManager = new InventoryManager();
        InventoryListener guiListener = new InventoryListener(AnvilAPI.inventoryManager);
        Bukkit.getPluginManager().registerEvents(guiListener, AnvilAPI.plugin);
    }

    public static boolean canCombine(ItemStack upgradeItem, ItemStack sacrificeItem) {
        if (upgradeItem == null || sacrificeItem == null) return false;
        return !getMissingEnchantments(upgradeItem, sacrificeItem).isEmpty();
    }

    public static Map<Enchantment, Integer> getMissingEnchantments(ItemStack upgradeItem, ItemStack sacrificeItem) {
        Map<Enchantment, Integer> missingEnchantments = new HashMap<>();
        ItemBuilderAPI upgradeItemBuilder = new ItemBuilderAPI(upgradeItem);
        ItemBuilderAPI sacrificeItemBuilder = new ItemBuilderAPI(sacrificeItem);

        Map<Enchantment, Integer> upgradeItemEnchantments = upgradeItemBuilder.getEnchantments();
        Map<Enchantment, Integer> sacrificeItemEnchantments = sacrificeItemBuilder.getEnchantments();

        for (Map.Entry<Enchantment, Integer> entry : sacrificeItemEnchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int sacrificeLevel = entry.getValue();
            EnchantmentRegister enchantmentRegister = EnchantmentsLoader.getEnchantmentRegister(enchantment);

            boolean isEnchantedBook = sacrificeItemBuilder.getId().endsWith("_ENCHANTED_BOOK");
            boolean isSupported = enchantmentRegister.getSupportCategories().contains(upgradeItemBuilder.getCategory());
            boolean isNotPresent = !upgradeItemEnchantments.containsKey(enchantment);

            if (!isSupported) continue;
            if ((isEnchantedBook || isNotPresent)) {
                enchantmentRegister.getExclusiveEnchantments().forEach(missingEnchantments::remove);
                missingEnchantments.put(enchantment, sacrificeLevel);
            } else {
                int upgradeLevel = upgradeItemEnchantments.getOrDefault(enchantment, 0);
                if (upgradeLevel == sacrificeLevel && upgradeLevel < enchantmentRegister.getAnvilMaxAnvilCombine()) {
                    missingEnchantments.put(enchantment, upgradeLevel + 1);
                } else if (upgradeLevel < sacrificeLevel) {
                    missingEnchantments.put(enchantment, sacrificeLevel);
                }
            }
        }
        return missingEnchantments;
    }

    public static void openAnvilGUI(Player target) {
        AnvilAPI.inventoryManager.openGUI(AnvilAPI.plugin, new AnvilGUI(AnvilAPI.plugin) , target);
    }
}
