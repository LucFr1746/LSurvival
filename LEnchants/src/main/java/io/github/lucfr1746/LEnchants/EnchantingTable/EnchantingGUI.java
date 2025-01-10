package io.github.lucfr1746.LEnchants.EnchantingTable;

import io.github.lucfr1746.LEnchants.LEnchants;
import io.github.lucfr1746.LSurvivalLib.Enchantments.Enchantment;
import io.github.lucfr1746.LSurvivalLib.Enchantments.Utils.EnchantmentRegister;
import io.github.lucfr1746.LSurvivalLib.Enchantments.Utils.EnchantmentsLoader;
import io.github.lucfr1746.LSurvivalLib.Entity.Player.PlayerAPI;
import io.github.lucfr1746.LSurvivalLib.Inventory.InventoryBuilderAPI;
import io.github.lucfr1746.LSurvivalLib.Inventory.InventoryButton;
import io.github.lucfr1746.LSurvivalLib.Inventory.InventoryManager;
import io.github.lucfr1746.LSurvivalLib.ItemStack.ItemBuilderAPI;
import io.github.lucfr1746.LSurvivalLib.Utils.APIs.NumberAPI;
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

import java.util.*;

public class EnchantingGUI extends InventoryBuilderAPI {

    private final LEnchants plugin;

    private ItemStack currentEnchantingItem;
    private final int power;
    private final int inputSlot = 19;
    private final List<Integer> enchantSlots = Arrays.asList(12,13,14,15,16,21,22,23,24,25,30,31,32,33,34);

    private boolean getBackItem = true;

    public EnchantingGUI(LEnchants plugin, int power) {
        this.plugin = plugin;
        this.power = power;
        this.currentEnchantingItem = null;
        setRows(6);
        setCurrentPage(power);
        setLockMode(LockMode.GUI_LOCKED);
        setTitle("Enchant Item");
    }

    public EnchantingGUI(LEnchants plugin, int power, ItemStack currentEnchantingItem) {
        this.plugin = plugin;
        this.power = power;
        this.currentEnchantingItem = currentEnchantingItem;
        setRows(6);
        setLockMode(LockMode.GUI_LOCKED);
        setTitle("Enchant Item");
    }

    @Override
    public void decorate(Player player) {
        for (int i = 0; i <= 53; i++) {
            this.addButton(i, getBackground());
        }
        this.addButton(inputSlot, getInputSlot());
        this.addButton(23, getEnchantItem());
        this.addButton(28, getEnchantingTableIcon());
        this.addButton(48, getBookshelfPower());
        this.addButton(49, getCloseButton());
        this.addButton(50, getEnchantmentsGuide());

        updateCurrentInv(this, player);

        new PlayerAPI(player).playButtonClickSound();
        super.decorate(player);
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory() instanceof PlayerInventory) {
            if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                updateFirstPage(event);
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
        if (this.getBackItem && event.getInventory().getItem(inputSlot) != null && Objects.requireNonNull(event.getInventory().getItem(inputSlot)).getType() != Material.AIR) {
            new PlayerAPI(player).addItem(event.getInventory().getItem(inputSlot));
        }
    }

    private InventoryButton getInputSlot() {
        return new InventoryButton()
                .creator(player -> this.currentEnchantingItem == null ? new ItemStack(Material.AIR) : this.currentEnchantingItem)
                .consumer(this::updateFirstPage);
    }

    private void updateFirstPage(InventoryClickEvent event) {
        PlayerAPI player = new PlayerAPI(event.getWhoClicked());
        event.setCancelled(false);

        Bukkit.getScheduler().runTask(this.plugin, () -> this.currentEnchantingItem = event.getWhoClicked().getOpenInventory().getTopInventory().getItem(inputSlot));

        Inventory enchantingGUI = event.getWhoClicked().getOpenInventory().getTopInventory();
        InventoryBuilderAPI inventoryBuilderAPI = new InventoryManager().getActiveInventoryBuilderAPI(enchantingGUI);
        updateCurrentInv(inventoryBuilderAPI, player.getPlayer());
    }

    private void updateCurrentInv(InventoryBuilderAPI inventoryBuilderAPI, Player player) {
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            if (this.currentEnchantingItem != null && this.currentEnchantingItem.getType() != Material.AIR) {
                ItemBuilderAPI currentEnchantingItem = new ItemBuilderAPI(this.currentEnchantingItem);
                if (currentEnchantingItem.getAmount() != 1) {
                    for (int slot : enchantSlots) {
                        inventoryBuilderAPI.updateButton(player, slot, getBackground());
                    }
                    inventoryBuilderAPI.updateButton(player, 17, getBackground());
                    inventoryBuilderAPI.updateButton(player, 35, getBackground());
                    inventoryBuilderAPI.updateButton(player, 23, getStackedSizeError());
                    return;
                }
                List<Enchantment> enchantmentList = new ArrayList<>();
                for (Map.Entry<Enchantment, EnchantmentRegister> enchantment : EnchantmentsLoader.getAllRegisteredEnchantments().entrySet()) {
                    if (enchantment.getValue().getSupportCategories().contains(currentEnchantingItem.getCategory()) && !enchantment.getValue().getEnchantingTableCost().isEmpty()) {
                        enchantmentList.add(enchantment.getKey());
                    }
                }
                enchantmentList.sort(Comparator.comparing(Enum::name));
                if (enchantmentList.isEmpty()) {
                    for (int slot : enchantSlots) {
                        inventoryBuilderAPI.updateButton(player, slot, getBackground());
                    }
                    inventoryBuilderAPI.updateButton(player, 17, getBackground());
                    inventoryBuilderAPI.updateButton(player, 35, getBackground());
                    inventoryBuilderAPI.updateButton(player, 23, getCannotEnchantError());
                    return;
                }
                inventoryBuilderAPI.updateButton(player.getPlayer(), 23, getBackground());
                for (int i = 0; i < enchantSlots.size(); i++) {
                    if (i >= enchantmentList.size()) break;
                    inventoryBuilderAPI.updateButton(player, enchantSlots.get(i),getEnchantmentButton(currentEnchantingItem, enchantmentList.get(i)));
                }
                updateNavigationButton(inventoryBuilderAPI, player, currentEnchantingItem, enchantmentList, 1);
            } else {
                for (int slot : enchantSlots) {
                    inventoryBuilderAPI.updateButton(player, slot, getBackground());
                }
                inventoryBuilderAPI.updateButton(player, 17, getBackground());
                inventoryBuilderAPI.updateButton(player, 35, getBackground());
                inventoryBuilderAPI.updateButton(player, 23, getEnchantItem());
            }
        });
    }

    private InventoryButton getEnchantmentButton(ItemBuilderAPI currentEnchantingItem, Enchantment enchantment) {
        if (this.power < EnchantmentsLoader.getEnchantmentRegister(enchantment).getBookshelfPowerRequired()) {
            return getEnchantmentButtonDenied(enchantment);
        } else {
            return getEnchantmentButtonAccept(currentEnchantingItem, enchantment);
        }
    }

    private InventoryButton getEnchantmentButtonAccept(ItemBuilderAPI currentEnchantingItem, Enchantment enchantment) {
        return new InventoryButton()
                .creator(player -> {
                    EnchantmentRegister enchantmentRegister = EnchantmentsLoader.getEnchantmentRegister(enchantment);
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.ENCHANTED_BOOK);
                    itemBuilder.setDisplayName("&a" + enchantmentRegister.getName(), true);

                    List<String> lores = new ArrayList<>(itemBuilder.getAutoAlignLores(33, enchantmentRegister.getDescriptionForLevel(enchantmentRegister.getMinLevel())));
                    lores.add("");
                    if (currentEnchantingItem.hasEnchantment(enchantment)) {
                        lores.add("  &a" + enchantmentRegister.getName() + " " + NumberAPI.toRoman(currentEnchantingItem.getEnchantmentLevel(enchantment)) + " ✔");
                    } else {
                        lores.add("  &c" + enchantmentRegister.getName() + " ❌");
                    }
                    lores.add("");
                    lores.add("&eClick to view!");
                    itemBuilder.setLores(lores);
                    return itemBuilder.setUnclassified(true).build();
                })
                .consumer(event -> {
                    this.getBackItem = false;
                    PlayerAPI player = new PlayerAPI(event.getWhoClicked());
                    EnchantingTableManager.openEnchantmentGUI(player.getPlayer(), currentEnchantingItem.build(), enchantment, this.power);
                });
    }

    private InventoryButton getEnchantmentButtonDenied(Enchantment enchantment) {
        return new InventoryButton()
                .creator(player -> {
                    EnchantmentRegister enchantmentRegister = EnchantmentsLoader.getEnchantmentRegister(enchantment);
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.ENCHANTED_BOOK);
                    itemBuilder.setDisplayName("&a" + enchantmentRegister.getName(), true);

                    List<String> lores = new ArrayList<>(itemBuilder.getAutoAlignLores(33,enchantmentRegister.getDescriptionForLevel(enchantmentRegister.getMinLevel())));
                    lores.add("");
                    lores.add("  &c" + enchantmentRegister.getName() + " ❌");
                    lores.add("");
                    lores.add("&cRequires " + enchantmentRegister.getBookshelfPowerRequired() + " Bookshelf Power!");
                    itemBuilder.setLores(lores);
                    return itemBuilder.setUnclassified(true).build();
                });
    }

    private void updateNavigationButton(InventoryBuilderAPI inventoryBuilderAPI, Player player, ItemBuilderAPI currentEnchantingItem, List<Enchantment> enchantments, int currentPage) {
        int startIndex = (currentPage) * enchantSlots.size();
        if (enchantments.size() >= startIndex) {
            inventoryBuilderAPI.updateButton(player, 35, getNextPageButton(inventoryBuilderAPI, player, currentEnchantingItem, enchantments, currentPage));
        } else {
            inventoryBuilderAPI.updateButton(player, 35, getBackground());
        }

        if (currentPage >= 2) {
            inventoryBuilderAPI.updateButton(player, 17, getPreviousPageButton(inventoryBuilderAPI, player, currentEnchantingItem, enchantments, currentPage));
        } else {
            inventoryBuilderAPI.updateButton(player, 17, getBackground());
        }
    }

    private InventoryButton getNextPageButton(InventoryBuilderAPI inventoryBuilderAPI, Player playerT, ItemBuilderAPI currentEnchantingItem, List<Enchantment> enchantments, int currentPage) {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.PLAYER_HEAD);
                    itemBuilder.setDisplayName("&aNext Page", true);
                    itemBuilder.addLore("&8Page " + (currentPage + 1));
                    itemBuilder.setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDFiNjJkYjVjMGEzZmExZWY0NDFiZjcwNDRmNTExYmU1OGJlZGY5YjY3MzE4NTNlNTBjZTkwY2Q0NGZiNjkifX19");
                    return itemBuilder.setUnclassified(true).build();
                })
                .consumer(event -> {
                    PlayerAPI player = new PlayerAPI(event.getWhoClicked());
                    player.playSoundAtPlayerLoc(Sound.ITEM_BUNDLE_REMOVE_ONE);
                    for (int slot : enchantSlots) {
                        inventoryBuilderAPI.updateButton(playerT, slot, getBackground());
                    }
                    for (int i = 0; i < enchantSlots.size(); i++) {
                        int index = currentPage * enchantSlots.size() + i;
                        if (index >= enchantments.size()) break;
                        inventoryBuilderAPI.updateButton(playerT, enchantSlots.get(i), getEnchantmentButton(currentEnchantingItem, enchantments.get(index)));
                    }
                    updateNavigationButton(inventoryBuilderAPI, playerT, currentEnchantingItem, enchantments, currentPage + 1);
                });
    }

    private InventoryButton getPreviousPageButton(InventoryBuilderAPI inventoryBuilderAPI, Player playerT, ItemBuilderAPI currentEnchantingItem, List<Enchantment> enchantments, int currentPage) {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.PLAYER_HEAD);
                    itemBuilder.setDisplayName("&aPrevious Page", true);
                    itemBuilder.addLore("&8Page " + (currentPage - 1));
                    itemBuilder.setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTRhNTY2N2VmNzI4NWM5MjI1ZmMyNjdkNDUxMTdlYWI1NDc4Yzc4NmJkNWFmMGExOTljMjlhMmMxNGMxZiJ9fX0=");
                    return itemBuilder.setUnclassified(true).build();
                })
                .consumer(event -> {
                    PlayerAPI player = new PlayerAPI(event.getWhoClicked());
                    player.playSoundAtPlayerLoc(Sound.ITEM_BUNDLE_REMOVE_ONE);
                    for (int slot : enchantSlots) {
                        inventoryBuilderAPI.updateButton(playerT, slot, getBackground());
                    }
                    for (int i = 0; i < enchantSlots.size(); i++) {
                        inventoryBuilderAPI.updateButton(playerT, enchantSlots.get(i), getEnchantmentButton(currentEnchantingItem, enchantments.get((currentPage - 2) * enchantSlots.size() + i)));
                    }
                    updateNavigationButton(inventoryBuilderAPI, playerT, currentEnchantingItem, enchantments, currentPage - 1);
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

    private InventoryButton getEnchantItem() {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.GRAY_DYE);
                    itemBuilder.setDisplayName("&cEnchant Item", true);
                    itemBuilder.addLore("&7Place an item in the open slot to");
                    itemBuilder.addLore("&7enchant it!");
                    return itemBuilder.setUnclassified(true).build();
                });
    }

    private InventoryButton getStackedSizeError() {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.RED_DYE);
                    itemBuilder.setDisplayName("&cInvalid Stack Size", true);
                    itemBuilder.addLore("&7You cannot enchant stacked items!");
                    return itemBuilder.setUnclassified(true).build();
                });
    }

    private InventoryButton getCannotEnchantError() {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.RED_DYE);
                    itemBuilder.setDisplayName("&cCannot Enchant Item!", true);
                    itemBuilder.addLore("&7This item cannot be enchanted!");
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
