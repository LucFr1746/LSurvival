package io.github.lucfr1746.LItems.ServerItems.MenuItem;

import io.github.lucfr1746.LItems.DataManager.PluginFiles.Languages;
import io.github.lucfr1746.LItems.LItems;
import io.github.lucfr1746.LSurvivalLib.Entity.Player.PlayerAPI;
import io.github.lucfr1746.LSurvivalLib.Inventory.InventoryBuilderAPI;
import io.github.lucfr1746.LSurvivalLib.Inventory.InventoryButton;
import io.github.lucfr1746.LSurvivalLib.ItemStack.ItemBuilderAPI;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SurvivalMenu extends InventoryBuilderAPI {

    private final LItems plugin;

    public SurvivalMenu(LItems plugin) {
        this.plugin = plugin;
        setRows(6);
        setLockMode(LockMode.ALL);
        setTitle(Languages.getDefaultItem_menu_title());
    }

    @Override
    public void decorate(Player player) {
        for (int i = 0; i <= 53; i++) {
            this.addButton(i, getBackground());
        }
        this.addButton(49, getCloseButton());
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            if (i == 8) continue;
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR && new ItemBuilderAPI(item).getId().equals("SURVIVAL_MENU")) {
                inv.setItem(i, null);
            }
        }
        new PlayerAPI(player).playButtonClickSound();
        super.decorate(player);
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
