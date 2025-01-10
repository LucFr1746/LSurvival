package io.github.lucfr1746.LEnchants.Anvil;

import io.github.lucfr1746.LEnchants.LEnchants;
import io.github.lucfr1746.LSurvivalLib.Enchantments.Enchantment;
import io.github.lucfr1746.LSurvivalLib.Enchantments.Utils.EnchantmentsLoader;
import io.github.lucfr1746.LSurvivalLib.Entity.Player.PlayerAPI;
import io.github.lucfr1746.LSurvivalLib.Inventory.InventoryBuilderAPI;
import io.github.lucfr1746.LSurvivalLib.Inventory.InventoryButton;
import io.github.lucfr1746.LSurvivalLib.ItemStack.ItemBuilderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AnvilGUI extends InventoryBuilderAPI {

    private final LEnchants plugin;

    private final int UPGRADE_SLOT = 29;
    private final int SACRIFICE_SLOT = 33;
    private final int RESULT_SLOT = 13;

    private final List<Integer> UPGRADE_STATUS = List.of(11, 12, 20);
    private final List<Integer> SACRIFICE_STATUS = List.of(14, 15, 24);

    private final int COMBINE_BUTTON_SLOT = 22;
    private final int CLOSE_BUTTON_SLOT = 49;

    private ItemStack currentUpgradeItem;
    private ItemStack currentSacrificeItem;
    private ItemStack currentResultItem;

    public AnvilGUI(LEnchants plugin) {
        this.plugin = plugin;
        setRows(6);
        setLockMode(LockMode.GUI_LOCKED);
        setTitle("Anvil");
    }

    @Override
    public void decorate(Player player) {
        for (int i = 0; i <= 44; i++) this.addButton(i, getBackground());
        for (int i = 45; i <= 53; i++) this.addButton(i, getErrorBackground());
        for (int slot : UPGRADE_STATUS) this.addButton(slot, getErrorUpgradeStatus());
        for (int slot : SACRIFICE_STATUS) this.addButton(slot, getErrorSacrificeStatus());

        this.addButton(RESULT_SLOT, getPrepareResultButton());
        this.addButton(COMBINE_BUTTON_SLOT, getPrepareCombineButton());
        this.addButton(CLOSE_BUTTON_SLOT, getCloseButton());

        this.addButton(UPGRADE_SLOT, getUpgradeButton());
        this.addButton(SACRIFICE_SLOT, getSacrificeButton());

        new PlayerAPI(player).playButtonClickSound();
        super.decorate(player);
    }

    private void updateAnvil(InventoryClickEvent event) {
        PlayerAPI player = new PlayerAPI(event.getWhoClicked());
        event.setCancelled(false);

        Inventory anvil = this.getInventory();
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            if (this.currentResultItem != null) {
                player.addItem(this.currentResultItem);
                this.currentResultItem = null;
            }
            this.currentUpgradeItem = anvil.getItem(UPGRADE_SLOT);
            this.currentSacrificeItem = anvil.getItem(SACRIFICE_SLOT);
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                if (isValidItem(this.currentUpgradeItem) && isValidItem(this.currentSacrificeItem)) {
                    resetToDefault(player.getPlayer());
                } else if (!isValidItem(this.currentUpgradeItem) && isValidItem(this.currentSacrificeItem)) {
                    resetToDefault(player.getPlayer());
                    updateStatus(player.getPlayer(), getAllowUpgradeStatus(), getErrorSacrificeStatus());
                } else if (isValidItem(this.currentUpgradeItem) && !isValidItem(this.currentSacrificeItem)) {
                    resetToDefault(player.getPlayer());
                    updateStatus(player.getPlayer(), getErrorUpgradeStatus(), getAllowSacrificeStatus());
                } else {
                    if (AnvilAPI.canCombine(this.currentUpgradeItem, this.currentSacrificeItem)) {
                        this.updateButton(player.getPlayer(), RESULT_SLOT, createTempItem());
                        this.updateButton(player.getPlayer(), COMBINE_BUTTON_SLOT, getAllowCombineButton(AnvilAPI.getMissingEnchantments(this.currentUpgradeItem, this.currentSacrificeItem)));
                        updateStatus(player.getPlayer(), getAllowUpgradeStatus(), getAllowSacrificeStatus());
                        updateBackground(player.getPlayer(), getAllowBackground());
                    } else {
                        updateStatus(player.getPlayer(), getErrorUpgradeStatus(), getErrorSacrificeStatus());
                        updateBackground(player.getPlayer(), getErrorBackground());
                    }
                }
            });
        });
    }

    private InventoryButton createFinalItem() {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI upgradeItemBuilder = new ItemBuilderAPI(this.currentUpgradeItem);
                    AnvilAPI.getMissingEnchantments(this.currentUpgradeItem, this.currentSacrificeItem).forEach(upgradeItemBuilder::addEnchantment);
                    this.currentResultItem = upgradeItemBuilder.build();
                    return this.currentResultItem;
                })
                .consumer(event -> {
                    event.setCancelled(false);
                    Bukkit.getScheduler().runTask(this.plugin, () -> {
                        this.currentResultItem = null;
                        resetToDefault((Player) event.getWhoClicked());
                    });
                });
    }

    private InventoryButton createTempItem() {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI upgradeItemBuilder = new ItemBuilderAPI(this.currentUpgradeItem.clone());
                    Map<Enchantment, Integer> oldUpgradeItemEnchantments = upgradeItemBuilder.getEnchantments();

                    AnvilAPI.getMissingEnchantments(this.currentUpgradeItem, this.currentSacrificeItem).forEach(upgradeItemBuilder::addEnchantment);
                    Map<Enchantment, Integer> currentUpgradeItemEnchantments = upgradeItemBuilder.getEnchantments();
                    upgradeItemBuilder = new ItemBuilderAPI(upgradeItemBuilder.build());
                    upgradeItemBuilder.addLore("&7&m----------------------");
                    upgradeItemBuilder.addLore("&aThis is the item you will get.");
                    upgradeItemBuilder.addLore("&7Click the &cANVIL BELOW &ato combine.");

                    List<Enchantment> missingEnchantments = new ArrayList<>();
                    oldUpgradeItemEnchantments.forEach((enchantment, integer) -> {
                        if (!currentUpgradeItemEnchantments.containsKey(enchantment)) missingEnchantments.add(enchantment);
                    });
                    if (!missingEnchantments.isEmpty()) {
                        List<String> lores = upgradeItemBuilder.getLores();
                        lores.add("");
                        String removedEnchantments = "";
                        for (Enchantment enchantment : missingEnchantments) {
                            removedEnchantments += ", " + EnchantmentsLoader.getEnchantmentRegister(enchantment).getName();
                        }
                        lores.addAll(upgradeItemBuilder.getAutoAlignLores(33, "&c&lWARNING: This will remove " + removedEnchantments.replaceFirst(", ", "") + "."));
                        upgradeItemBuilder.setLores(lores);
                    }
                    return upgradeItemBuilder.setUnclassified(true).build();
                })
                .consumer(event -> {
                    Player player = (Player) event.getWhoClicked();
                    new PlayerAPI(player).playSoundAtPlayerLoc(Sound.ENTITY_ENDERMAN_TELEPORT).sendColoredMessage("&cYou must click the Anvil below to combine the two items!");
                })
                .name("TEMP_RESULT");
    }

    private void updateStatus(Player player, InventoryButton upgradeStatus, InventoryButton sacrificeStatus) {
        for (int slot : this.UPGRADE_STATUS) {
            this.updateButton(player.getPlayer(), slot, upgradeStatus);
        }
        for (int slot : this.SACRIFICE_STATUS) {
            this.updateButton(player.getPlayer(), slot, sacrificeStatus);
        }
    }

    private void updateBackground(Player player, InventoryButton updateStats) {
        for (int i = 45; i <= 53; i++) this.updateButton(player.getPlayer(), i, updateStats);
        this.updateButton(player, CLOSE_BUTTON_SLOT, getCloseButton());
    }

    private void resetToDefault(Player player) {
        for (int i = 45; i <= 53; i++) this.updateButton(player, i, getErrorBackground());
        for (int slot : UPGRADE_STATUS) this.updateButton(player, slot, getErrorUpgradeStatus());
        for (int slot : SACRIFICE_STATUS) this.updateButton(player, slot, getErrorSacrificeStatus());

        this.updateButton(player, RESULT_SLOT, getPrepareResultButton());
        this.updateButton(player, COMBINE_BUTTON_SLOT, getPrepareCombineButton());
        this.updateButton(player, CLOSE_BUTTON_SLOT, getCloseButton());
    }

    private InventoryButton getUpgradeButton() {
        return new InventoryButton()
                .creator(player -> new ItemStack(Material.AIR))
                .consumer(this::updateAnvil);
    }

    private InventoryButton getSacrificeButton() {
        return new InventoryButton()
                .creator(player -> new ItemStack(Material.AIR))
                .consumer(this::updateAnvil);
    }

    private InventoryButton getPrepareResultButton() {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.BARRIER);
                    itemBuilder.setDisplayName("&cAnvil", true);
                    itemBuilder.addLore("&7Place a target item in the left slot");
                    itemBuilder.addLore("&7and a sacrifice item in the right slot");
                    itemBuilder.addLore("&7to combine them!");
                    return itemBuilder.setGlowing(true).setUnclassified(true).build();
                });
    }

    private InventoryButton getAllowCombineButton(Map<Enchantment, Integer> missingEnchantments) {
        AtomicInteger finalLevelRequired = new AtomicInteger();
        return new InventoryButton()
                .creator(player -> {
                    missingEnchantments.forEach((enchantment, level) -> finalLevelRequired.addAndGet(EnchantmentsLoader.getEnchantmentRegister(enchantment).getAnvilApplyCostForLevel(level)));
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.ANVIL);
                    itemBuilder.setDisplayName("&aCombine Items", true);
                    itemBuilder.addLore("&7Combine the items in the slots to");
                    itemBuilder.addLore("&7left and right below.");
                    if (finalLevelRequired.get() > 0) {
                        itemBuilder.addLore("");
                        itemBuilder.addLore("&7Cost");
                        itemBuilder.addLore("&3" + finalLevelRequired + " Exp Levels");
                    }
                    itemBuilder.addLore("");
                    itemBuilder.addLore("&eClick to combine!");
                    return itemBuilder.setGlowing(true).setUnclassified(true).build();
                })
                .consumer(event -> {
                    Player player = (Player) event.getWhoClicked();
                    if (player.getLevel() >= finalLevelRequired.get()) {
                        resetToDefault(player);
                        this.updateButton(player, RESULT_SLOT, createFinalItem());
                        this.updateButton(player, UPGRADE_SLOT, getUpgradeButton());
                        this.updateButton(player, SACRIFICE_SLOT, getSacrificeButton());
                        this.updateButton(player, COMBINE_BUTTON_SLOT, getDoneCombineButton());
                        player.setLevel(player.getLevel() - finalLevelRequired.get());
                        new PlayerAPI(player).playSoundAtPlayerLoc(Sound.BLOCK_ANVIL_USE);
                    } else {
                        new PlayerAPI(player).sendColoredMessage("&cYou don't have enough Experience Levels!")
                                .playSoundAtPlayerLoc(Sound.ENTITY_ENDERMAN_TELEPORT, 1, 0.5f);
                    }
                });
    }

    private InventoryButton getDoneCombineButton() {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.OAK_SIGN);
                    itemBuilder.setDisplayName("&aAnvil", true);
                    itemBuilder.addLore("&7Claim the result item above!");
                    return itemBuilder.setUnclassified(true).build();
                })
                .consumer(event -> {
                    Player player = (Player) event.getWhoClicked();
                    new PlayerAPI(player).addItem(this.currentResultItem);
                    this.currentResultItem = null;
                    resetToDefault(player);
                });
    }

    private InventoryButton getPrepareCombineButton() {
        return new InventoryButton()
               .creator(player -> {
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.ANVIL);
                    itemBuilder.setDisplayName("&aCombine Items", true);
                    itemBuilder.addLore("&7Combine the items in the slots to");
                    itemBuilder.addLore("&7left and right below.");
                    return itemBuilder.setUnclassified(true).build();
                });
    }

    private InventoryButton getAllowUpgradeStatus() {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.LIME_STAINED_GLASS_PANE);
                    itemBuilder.setDisplayName("&6Item to Upgrade", true);
                    itemBuilder.addLore("&7The item you want to upgrade should");
                    itemBuilder.addLore("&7be placed in the slot on this side.");
                    return itemBuilder.setUnclassified(true).build();
                });
    }

    private InventoryButton getAllowSacrificeStatus() {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.LIME_STAINED_GLASS_PANE);
                    itemBuilder.setDisplayName("&6Item to Sacrifice", true);
                    itemBuilder.addLore("&7The item you are sacrificing in order");
                    itemBuilder.addLore("&7to upgrade the item on the left should");
                    itemBuilder.addLore("&7be placed in the slot on this side.");
                    return itemBuilder.setUnclassified(true).build();
                });
    }

    private InventoryButton getErrorUpgradeStatus() {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.RED_STAINED_GLASS_PANE);
                    itemBuilder.setDisplayName("&6Item to Upgrade", true);
                    itemBuilder.addLore("&7The item you want to upgrade should");
                    itemBuilder.addLore("&7be placed in the slot on this side.");
                    return itemBuilder.setUnclassified(true).build();
                });
    }

    private InventoryButton getErrorSacrificeStatus() {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.RED_STAINED_GLASS_PANE);
                    itemBuilder.setDisplayName("&6Item to Sacrifice", true);
                    itemBuilder.addLore("&7The item you are sacrificing in order");
                    itemBuilder.addLore("&7to upgrade the item on the left should");
                    itemBuilder.addLore("&7be placed in the slot on this side.");
                    return itemBuilder.setUnclassified(true).build();
                });
    }

    private InventoryButton getAllowBackground() {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.LIME_STAINED_GLASS_PANE);
                    itemBuilder.setHideTooltip(true);
                    return itemBuilder.setUnclassified(true).build();
                });
    }

    private InventoryButton getErrorBackground() {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.RED_STAINED_GLASS_PANE);
                    itemBuilder.setHideTooltip(true);
                    return itemBuilder.setUnclassified(true).build();
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
                })
                .name("CLOSE");
    }

    private boolean isValidItem(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory() instanceof PlayerInventory) {
            if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                updateAnvil(event);
            }
            return;
        }
        event.setCancelled(true);

        int slot = event.getSlot();
        InventoryButton button = this.getButtonMap().get(slot);
        if (button != null && button.getEventConsumer() != null) {
            button.getEventConsumer().accept(event);
        }
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();
        ItemStack upgradeItem = inventory.getItem(UPGRADE_SLOT);
        ItemStack sacrificeItem = inventory.getItem(SACRIFICE_SLOT);
        InventoryButton result = this.getButtonMap().get(RESULT_SLOT);
        if (result.getButtonName() == null || (!result.getButtonName().equals("CLOSE") && !result.getButtonName().equals("TEMP_RESULT"))) {
            new PlayerAPI(player).addItem(inventory.getItem(RESULT_SLOT));
        }
        if (upgradeItem != null && upgradeItem.getType() != Material.AIR) {
            new PlayerAPI(player).addItem(upgradeItem);
        }
        if (sacrificeItem != null && sacrificeItem.getType() != Material.AIR) {
            new PlayerAPI(player).addItem(sacrificeItem);
        }
    }
}
