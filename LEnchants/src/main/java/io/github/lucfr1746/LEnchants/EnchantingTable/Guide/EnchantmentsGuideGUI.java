package io.github.lucfr1746.LEnchants.EnchantingTable.Guide;

import io.github.lucfr1746.LEnchants.EnchantingTable.EnchantingTableManager;
import io.github.lucfr1746.LSurvivalLib.Enchantments.Enchantment;
import io.github.lucfr1746.LSurvivalLib.Enchantments.Utils.EnchantmentRegister;
import io.github.lucfr1746.LSurvivalLib.Enchantments.Utils.EnchantmentsLoader;
import io.github.lucfr1746.LSurvivalLib.Entity.Player.PlayerAPI;
import io.github.lucfr1746.LSurvivalLib.Inventory.*;
import io.github.lucfr1746.LSurvivalLib.ItemStack.Category.Category;
import io.github.lucfr1746.LSurvivalLib.ItemStack.ItemBuilderAPI;
import io.github.lucfr1746.LSurvivalLib.Utils.APIs.NumberAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class EnchantmentsGuideGUI extends InventoryBuilderAPI {

    private final int page;
    private final int totalPage;

    private final int power;

    public EnchantmentsGuideGUI(int page, int power) {
        this.page = page;
        this.totalPage = (int) Math.ceil((double) EnchantmentsLoader.getAllRegisteredEnchantments().size() / 28);
        this.power = power;

        setRows(6);
        setLockMode(LockMode.ALL);
        setTitle("Enchantments Guide (" + page + "/" + totalPage + ")");
    }

    @Override
    public void decorate(Player player) {
        for (int slot : InventorySlot.BORDER_OF_54.getSlots()) {
            this.addButton(slot, getBackground());
        }
        this.addButton(4, getEnchantmentsGuide());
        this.addButton(45, getPreviousPageButton());
        this.addButton(48, getGoBackButton());
        this.addButton(49, getCloseButton());
        this.addButton(53, getNextPageButton());

        List<Integer> enchantmentSlots = new ArrayList<>();
        for (int slot = 10; slot <= 43; slot++) {
            if (InventorySlot.BORDER_OF_54.getSlots().contains(slot)) continue;
            enchantmentSlots.add(slot);
        }

        for (int slot = 0; slot < enchantmentSlots.size(); slot++) {
            int index = (page - 1) * 28 + slot;
            Map<Enchantment, EnchantmentRegister> allEnchantments = EnchantmentsLoader.getAllRegisteredEnchantments();
            List<Enchantment> sortedEnchantments = new ArrayList<>(allEnchantments.keySet());
            sortedEnchantments.sort(Comparator.comparing(Enchantment::name));

            if (index >= sortedEnchantments.size()) break;
            Enchantment enchantment = sortedEnchantments.get(index);
            this.addButton(enchantmentSlots.get(slot), getEnchantment(enchantment));
        }

        new PlayerAPI(player).playButtonClickSound();
        super.decorate(player);
    }

    private InventoryButton getEnchantment(Enchantment enchantment) {
        return new InventoryButton()
                .creator(player -> {
                    EnchantmentRegister enchantRegister = EnchantmentsLoader.getEnchantmentRegister(enchantment);
                    ItemBuilderAPI itemBuilderAPI = new ItemBuilderAPI(Material.ENCHANTED_BOOK);
                                   itemBuilderAPI.setDisplayName("&a" + enchantRegister.getName() + " " + NumberAPI.toRoman(enchantRegister.getMaxLevel()), true);
                                   List<String> lores = new ArrayList<>(itemBuilderAPI.getAutoAlignLores(33, enchantRegister.getDescriptionForLevel(enchantRegister.getMaxLevel())));

                                   if (!enchantRegister.getSources().isEmpty()) {
                                       lores.add("");
                                       lores.add("&6Sources:");
                                       lores.addAll(enchantRegister.getSources());
                                   }
                                   if (!enchantRegister.getSupportCategories().isEmpty()) {
                                       lores.add("");
                                       lores.add("&6Applied To:");
                                       for (Category category : enchantRegister.getSupportCategories()) {
                                           lores.add(" &7- &f" + getFormattedName(category.name()));
                                       }
                                   }
                                   if (enchantRegister.getEnchantingSkillLevelRequired() != 0 || enchantRegister.getBookshelfPowerRequired() != 0) {
                                       lores.add("");
                                       lores.add("&6Requirements:");
                                   }
                                   if (enchantRegister.getEnchantingSkillLevelRequired() != 0) {
                                       lores.add(" &7- &bEnchanting Level " + enchantRegister.getEnchantingSkillLevelRequired());
                                   }
                                   if (enchantRegister.getBookshelfPowerRequired() != 0) {
                                       lores.add(" &7- &d" + enchantRegister.getBookshelfPowerRequired() + " Bookshelf Power");
                                   }
                                    if (!enchantRegister.getExclusiveEnchantments().isEmpty()) {
                                        lores.add("");
                                        lores.add("&6Conflicts:");
                                        for (Enchantment exclusiveEnchant : enchantRegister.getExclusiveEnchantments()) {
                                            if (!EnchantmentsLoader.getAllRegisteredEnchantments().containsKey(exclusiveEnchant)) continue;
                                            lores.add(" &7- &c" + EnchantmentsLoader.getEnchantmentRegister(exclusiveEnchant).getName());
                                        }
                                    }
                    return itemBuilderAPI.setLores(lores).setUnclassified(true).build();
                })
                .consumer(event -> {
                    if (event.getWhoClicked().hasPermission("lsurvival.lenchants.enchantments.get")) {
                        EnchantingTableManager.openEnchantmentsGuideSubGUI((Player) event.getWhoClicked(), enchantment, this.page, this.power);
                    }
                });
    }

    private InventoryButton getEnchantmentsGuide() {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.BOOK);
                    itemBuilder.setDisplayName("&aEnchantments Guide", true);
                    itemBuilder.addLore("&7View a complete list of all");
                    itemBuilder.addLore("&7enchantments and their requirements.");
                    return itemBuilder.setUnclassified(true).build();
                });
    }

    private InventoryButton getNextPageButton() {
        if (this.page <= 0 || this.page >= this.totalPage) return getBackground();
        return createNavigationButton(
                "&aNext Page",
                "&ePage " + (this.page + 1),
                this.page + 1
        );
    }

    private InventoryButton getPreviousPageButton() {
        if (this.page <= 1 || this.page > this.totalPage) return getBackground();
        return createNavigationButton(
                "&aPrevious Page",
                "&ePage " + (this.page - 1),
                this.page - 1
        );
    }

    private InventoryButton createNavigationButton(String display, String lore, int targetPage) {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.ARROW);
                    itemBuilder.setDisplayName(display, true);
                    itemBuilder.addLore(lore);
                    return itemBuilder.setUnclassified(true).build();
                })
                .consumer(event -> {
                    EnchantingTableManager.openEnchantmentsGuide((Player) event.getWhoClicked(), targetPage, this.power);
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
                .consumer(event -> EnchantingTableManager.openEnchantingTable((Player) event.getWhoClicked(), this.power));
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

    public String getFormattedName(String input) {
        String[] words = input.toLowerCase().split("_");
        StringBuilder formatted = new StringBuilder();

        for (String word : words) {
            formatted.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
        }

        return formatted.toString().trim();
    }
}
