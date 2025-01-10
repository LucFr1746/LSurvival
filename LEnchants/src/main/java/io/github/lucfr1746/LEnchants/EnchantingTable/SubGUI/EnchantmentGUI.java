package io.github.lucfr1746.LEnchants.EnchantingTable.SubGUI;

import io.github.lucfr1746.LEnchants.EnchantingTable.EnchantingTableManager;
import io.github.lucfr1746.LEnchants.LEnchants;
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
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class EnchantmentGUI extends InventoryBuilderAPI {

    private final Enchantment enchantment;
    private final EnchantmentRegister enchantmentRegister;
    private ItemStack currentEnchantingItem;

    private final int power;
    private final int inputSlot = 19;
    private final List<Integer> enchantSlots = Arrays.asList(22,23,24,31,32,33);

    public EnchantmentGUI(ItemStack currentEnchantingItem, Enchantment enchantment, int power) {
        this.currentEnchantingItem = currentEnchantingItem;
        this.enchantment = enchantment;
        this.enchantmentRegister = EnchantmentsLoader.getEnchantmentRegister(enchantment);
        this.power = power;
        setRows(6);
        setLockMode(LockMode.ALL);
        setTitle("Enchant ➜ " + enchantmentRegister.getName());
    }

    @Override
    public void decorate(Player player) {
        for (int i = 0; i <= 53; i++) {
            this.addButton(i, getBackground());
        }
        this.addButton(inputSlot, getInputSlot());
        this.addButton(28, getEnchantingTableIcon());
        this.addButton(48, getBookshelfPower());
        this.addButton(49, getCloseButton());
        this.addButton(45, getGoBackButton());
        this.addButton(50, getEnchantmentsGuide());

        updateEnchantment(player);

        new PlayerAPI(player).playButtonClickSound();
        super.decorate(player);
    }

    private boolean getBackItem = true;
    @Override
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (getBackItem && event.getInventory().getItem(inputSlot) != null && Objects.requireNonNull(event.getInventory().getItem(inputSlot)).getType() != Material.AIR) {
            new PlayerAPI(player).addItem(event.getInventory().getItem(inputSlot));
        }
    }

    private void updateEnchantment(Player player) {
        this.updateButton(player, inputSlot, getInputSlot());
        List<InventoryButton> enchantmentPlaceholder = new ArrayList<>();

        ItemBuilderAPI itemBuilder = new ItemBuilderAPI(this.currentEnchantingItem);
        int currentLevel = itemBuilder.hasEnchantment(this.enchantment) ? itemBuilder.getEnchantmentLevel(this.enchantment) : 0;
        for (int level = this.enchantmentRegister.getEnchantingTableMinLevel(); level <= this.enchantmentRegister.getEnchantingTableMaxLevel(); level++) {
            if (level == currentLevel) enchantmentPlaceholder.add(getPresentEnchant(level));
            else if (level < currentLevel) enchantmentPlaceholder.add(getLowerEnchant(level));
            else enchantmentPlaceholder.add(getApplyEnchant(level));
        }
        for (int i = 0; i < enchantmentPlaceholder.size(); i++) {
            this.updateButton(player, enchantSlots.get(i), enchantmentPlaceholder.get(i));
        }
    }

    private InventoryButton getLowerEnchant(int enchantmentLevel) {
        return new InventoryButton()
                .creator(player -> {
                    int applyCost = this.enchantmentRegister.getEnchantingTableApplyCostForLevel(enchantmentLevel);
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.GRAY_DYE);
                    itemBuilder.setDisplayName("&9" + this.enchantmentRegister.getName() + " " + NumberAPI.toRoman(enchantmentLevel), true);
                    List<String> lores = new ArrayList<>(itemBuilder.getAutoAlignLores(33, this.enchantmentRegister.getDescriptionForLevel(enchantmentLevel)));
                    lores.add("");
                    lores.add("&7Cost");
                    lores.add("&3" + applyCost + " Exp Levels " + (player.getLevel() >= applyCost ? "&a✔" : "&c❌"));
                    lores.add("");
                    lores.add("&cHigher level already present!");
                    itemBuilder.setLores(lores);
                    return itemBuilder.setUnclassified(true).build();
                });
    }

    private InventoryButton getPresentEnchant(int enchantmentLevel) {
        return new InventoryButton()
                .creator(player -> {
                    int applyCost = this.enchantmentRegister.getEnchantingTableApplyCostForLevel(enchantmentLevel);
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.ENCHANTED_BOOK);
                    itemBuilder.setDisplayName("&9" + this.enchantmentRegister.getName() + " " + NumberAPI.toRoman(enchantmentLevel), true);
                    List<String> lores = new ArrayList<>(itemBuilder.getAutoAlignLores(33, this.enchantmentRegister.getDescriptionForLevel(enchantmentLevel)));
                    lores.add("");
                    lores.add("&cThis enchantment is already present");
                    lores.add("&cand can be removed.");
                    lores.add("");
                    lores.add("&7Cost");
                    lores.add("&3" + applyCost + " Exp Levels " + (player.getLevel() >= applyCost ? "&a✔" : "&c❌"));
                    lores.add("");
                    if (this.enchantmentRegister.getEnchantingTableApplyCostForLevel(enchantmentLevel) > player.getLevel()) {
                        lores.add("&cYou don't have enough Experience");
                        lores.add("&cLevels!");
                    } else {
                        lores.add("&eClick to remove!");
                    }
                    itemBuilder.setLores(lores);
                    return itemBuilder.setUnclassified(true).build();
                })
                .consumer(event -> {
                    Player player = (Player) event.getWhoClicked();
                    if (player.getLevel() >= this.enchantmentRegister.getEnchantingTableApplyCostForLevel(enchantmentLevel)) {
                        ItemBuilderAPI itemBuilderAPI = new ItemBuilderAPI(this.currentEnchantingItem);
                        itemBuilderAPI.removeEnchantment(this.enchantment);
                        this.currentEnchantingItem = itemBuilderAPI.build();
                        player.setLevel(player.getLevel() - this.enchantmentRegister.getEnchantingTableApplyCostForLevel(enchantmentLevel));
                        updateEnchantment(player);
                        new PlayerAPI(event.getWhoClicked()).playSoundAtPlayerLoc(Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR);
                    } else {
                        new PlayerAPI(event.getWhoClicked()).playSoundAtPlayerLoc(Sound.ENTITY_ENDERMAN_TELEPORT, 1, 0.5f);
                    }
                });
    }

    private InventoryButton getApplyEnchant(int enchantmentLevel) {
        return new InventoryButton()
                .creator(player -> {
                    int applyCost = this.enchantmentRegister.getEnchantingTableApplyCostForLevel(enchantmentLevel);
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.ENCHANTED_BOOK);
                    itemBuilder.setDisplayName("&9" + this.enchantmentRegister.getName() + " " + NumberAPI.toRoman(enchantmentLevel), true);
                    List<String> lores = new ArrayList<>(itemBuilder.getAutoAlignLores(33, this.enchantmentRegister.getDescriptionForLevel(enchantmentLevel)));
                    if (!this.enchantmentRegister.getExclusiveEnchantments().isEmpty()) {
                        for (Enchantment enchantment : this.enchantmentRegister.getExclusiveEnchantments()) {
                            EnchantmentRegister enchantRegis = EnchantmentsLoader.getEnchantmentRegister(enchantment);
                            if (new ItemBuilderAPI(this.currentEnchantingItem).hasEnchantment(enchantment)) {
                                lores.add("");
                                lores.add("&c&lWARNING: This will remove");
                                lores.add("&c&l" + enchantRegis.getName());
                            }
                        }
                    }
                    lores.add("");
                    lores.add("&7Cost");
                    lores.add("&3" + applyCost + " Exp Levels " + (player.getLevel() >= applyCost ? "&a✔" : "&c❌"));
                    lores.add("");
                    if (this.enchantmentRegister.getEnchantingTableApplyCostForLevel(enchantmentLevel) > player.getLevel()) {
                        lores.add("&cYou don't have enough Experience");
                        lores.add("&cLevels!");
                    } else {
                        lores.add("&eClick to enchant!");
                    }
                    itemBuilder.setLores(lores);
                    return itemBuilder.setUnclassified(true).build();
                })
                .consumer(event -> {
                    Player player = (Player) event.getWhoClicked();
                    if (player.getLevel() >= this.enchantmentRegister.getEnchantingTableApplyCostForLevel(enchantmentLevel)) {
                        ItemBuilderAPI itemBuilderAPI = new ItemBuilderAPI(this.currentEnchantingItem);
                        itemBuilderAPI.addEnchantment(this.enchantment, enchantmentLevel);
                        this.currentEnchantingItem = itemBuilderAPI.build();
                        player.setLevel(player.getLevel() - this.enchantmentRegister.getEnchantingTableApplyCostForLevel(enchantmentLevel));
                        updateEnchantment(player);
                        new PlayerAPI(event.getWhoClicked()).playSoundAtPlayerLoc(Sound.ENTITY_EVOKER_CAST_SPELL).playSoundAtPlayerLoc(Sound.ENTITY_PLAYER_LEVELUP);
                    } else {
                        new PlayerAPI(event.getWhoClicked()).playSoundAtPlayerLoc(Sound.ENTITY_ENDERMAN_TELEPORT, 1, 0.5f);
                    }
                });
    }

    private InventoryButton getInputSlot() {
        return new InventoryButton()
                .creator(player -> new ItemStack(this.currentEnchantingItem))
                .consumer(event -> {
                    this.getBackItem = false;
                    PlayerAPI playerAPI = new PlayerAPI(event.getWhoClicked());
                    playerAPI.addItem(this.currentEnchantingItem);
                    EnchantingTableManager.openEnchantingTable((Player) event.getWhoClicked(), this.power);
                });
    }

    private InventoryButton getEnchantingTableIcon() {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.ENCHANTING_TABLE);
                    itemBuilder.setDisplayName("&aEnchant Item", true);
                    itemBuilder.addLore("&7Add and remove enchantments from");
                    itemBuilder.addLore("&7the item in the slot above!");
                    return itemBuilder.setUnclassified(true).build();
                });
    }

    private InventoryButton getBookshelfPower() {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.BOOKSHELF);
                    itemBuilder.setDisplayName("&dBookshelf Power", true);
                    itemBuilder.addLore("&7Stronger enchantments require more");
                    itemBuilder.addLore("&7Bookshelf Power, which can be");
                    itemBuilder.addLore("&7increased by placing bookshelves");
                    itemBuilder.addLore("&7nearby.");
                    itemBuilder.addLore("");
                    itemBuilder.addLore("&7Bookshelf Power: &d" + this.power);
                    return itemBuilder.setUnclassified(true).build();
                });
    }

    private InventoryButton getEnchantmentsGuide() {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.BOOK);
                    itemBuilder.setDisplayName("&aEnchantments Guide", true);
                    itemBuilder.addLore("&7View a complete list of all");
                    itemBuilder.addLore("&7enchantments and their requirements.");
                    itemBuilder.addLore("");
                    itemBuilder.addLore("&eClick to view!");
                    return itemBuilder.setUnclassified(true).build();
                })
                .consumer(event -> {
                    PlayerAPI player = new PlayerAPI(event.getWhoClicked());
                    EnchantingTableManager.openEnchantmentsGuide(player.getPlayer(), 1, this.power);
                });
    }

    private InventoryButton getGoBackButton() {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.ARROW);
                    itemBuilder.setDisplayName("&aGo Back", true);
                    itemBuilder.addLore("&7To Enchant Item");
                    return itemBuilder.setUnclassified(true).build();
                })
                .consumer(event -> {
                    this.getBackItem = false;
                    EnchantingTableManager.openEnchantingTable((Player) event.getWhoClicked(), this.power, this.currentEnchantingItem);
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
}
