package io.github.lucfr1746.LEnchants.EnchantingTable.Guide;

import io.github.lucfr1746.LEnchants.EnchantingTable.EnchantingTableManager;
import io.github.lucfr1746.LSurvivalLib.Enchantments.Enchantment;
import io.github.lucfr1746.LSurvivalLib.Enchantments.Utils.EnchantmentRegister;
import io.github.lucfr1746.LSurvivalLib.Enchantments.Utils.EnchantmentsLoader;
import io.github.lucfr1746.LSurvivalLib.Entity.Player.PlayerAPI;
import io.github.lucfr1746.LSurvivalLib.Inventory.InventoryBuilderAPI;
import io.github.lucfr1746.LSurvivalLib.Inventory.InventoryButton;
import io.github.lucfr1746.LSurvivalLib.ItemStack.ItemBuilderAPI;
import io.github.lucfr1746.LSurvivalLib.Utils.APIs.NumberAPI;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EnchantmentsGuideSubGUI extends InventoryBuilderAPI {

    private final Enchantment enchantment;
    private final EnchantmentRegister enchantmentRegister;
    private final int backPage;
    private final int power;

    private final List<Integer> enchantmentSlots_10 = List.of(11,12,13,14,15,20,21,22,23,24);
    private final List<Integer> enchantmentSlots_9 = List.of(11,12,13,14,15,20,21,23,24);
    private final List<Integer> enchantmentSlots_8 = List.of(11,12,13,14,15,21,22,23);
    private final List<Integer> enchantmentSlots_7 = List.of(11,12,13,14,15,21,23);
    private final List<Integer> enchantmentSlots_6 = List.of(11,12,13,14,15,22);
    private final List<Integer> enchantmentSlots_5 = List.of(11,12,13,14,15);
    private final List<Integer> enchantmentSlots_4 = List.of(11,12,14,15);
    private final List<Integer> enchantmentSlots_3 = List.of(12,13,14);
    private final List<Integer> enchantmentSlots_2 = List.of(12,14);
    private final List<Integer> enchantmentSlots_1 = List.of(13);

    public EnchantmentsGuideSubGUI(Enchantment enchantment, int backPage, int power) {
        this.enchantment = enchantment;
        this.enchantmentRegister = EnchantmentsLoader.getEnchantmentRegister(enchantment);
        this.backPage = backPage;
        this.power = power;

        setRows(5);
        setLockMode(LockMode.ALL);
        setTitle(EnchantmentsLoader.getEnchantmentRegister(enchantment).getName());
    }

    @Override
    public void decorate(Player player) {
        for (int slot = 0; slot <= 44; slot++) {
            this.addButton(slot, getBackground());
        }
        this.addButton(39, getGoBackButton());
        this.addButton(40, getCloseButton());

        List<InventoryButton> enchantmentsButtons = getEnchantmentButton();
        List<Integer> enchantmentsSlots = new ArrayList<>();
        switch (enchantmentsButtons.size()) {
            case 1: enchantmentsSlots.addAll(enchantmentSlots_1);
            case 2: enchantmentsSlots.addAll(enchantmentSlots_2);
            case 3: enchantmentsSlots.addAll(enchantmentSlots_3);
            case 4: enchantmentsSlots.addAll(enchantmentSlots_4);
            case 5: enchantmentsSlots.addAll(enchantmentSlots_5);
            case 6: enchantmentsSlots.addAll(enchantmentSlots_6);
            case 7: enchantmentsSlots.addAll(enchantmentSlots_7);
            case 8: enchantmentsSlots.addAll(enchantmentSlots_8);
            case 9: enchantmentsSlots.addAll(enchantmentSlots_9);
            case 10: enchantmentsSlots.addAll(enchantmentSlots_10);
        }
        for (int i = 0; i < enchantmentsButtons.size(); i++) {
            this.addButton(enchantmentsSlots.get(i), enchantmentsButtons.get(i));
        }

        new PlayerAPI(player).playButtonClickSound();
        super.decorate(player);
    }

    private List<InventoryButton> getEnchantmentButton() {
        List<InventoryButton> buttons = new ArrayList<>();
        for (int level = this.enchantmentRegister.getMinLevel(); level <= this.enchantmentRegister.getMaxLevel(); level++) {
            int enchantLevel = level;
            buttons.add(new InventoryButton()
                    .creator(player -> {
                        ItemBuilderAPI itemBuilderAPI = new ItemBuilderAPI(Material.ENCHANTED_BOOK);
                        itemBuilderAPI.setItemName("Enchanted Book");
                        itemBuilderAPI.addEnchantment(this.enchantment, enchantLevel);
                        itemBuilderAPI.setTier(this.enchantmentRegister.getRarityForLevel(enchantLevel));
                        itemBuilderAPI.setId(this.enchantmentRegister.getName() + "_" + NumberAPI.toRoman(enchantLevel) + "_ENCHANTED_BOOK");
                        return itemBuilderAPI.build();
                    })
                    .consumer(event -> new PlayerAPI(event.getWhoClicked()).playSoundAtPlayerLoc(Sound.ENTITY_ITEM_PICKUP).addItem(event.getCurrentItem())));
        }
        return buttons;
    }

    private InventoryButton getGoBackButton() {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.ARROW);
                    itemBuilder.setDisplayName("&aGo Back", true);
                    itemBuilder.addLore("&7To Enchantments Guide");
                    return itemBuilder.setUnclassified(true).build();
                })
                .consumer(event -> EnchantingTableManager.openEnchantmentsGuide((Player) event.getWhoClicked(), this.backPage, this.power));
    }

    private InventoryButton getCloseButton() {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.BARRIER);
                    itemBuilder.setDisplayName("&cClose", true);
                    return itemBuilder.setUnclassified(true).build();
                })
                .consumer(event -> {
                    PlayerAPI player = new PlayerAPI(event.getWhoClicked());
                    player.getPlayer().closeInventory();
                    player.playSoundAtPlayerLoc(Sound.ITEM_BUNDLE_REMOVE_ONE);
                });
    }

    private InventoryButton getBackground() {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.BLACK_STAINED_GLASS_PANE);
                    itemBuilder.setHideTooltip(true);
                    return itemBuilder.setUnclassified(true).build();
                });
    }
}
