package io.github.lucfr1746.LItems.ServerItems;

import io.github.lucfr1746.LItems.LItems;
import io.github.lucfr1746.LSurvivalLib.Entity.Player.PlayerAPI;
import io.github.lucfr1746.LSurvivalLib.Inventory.InventoryBuilderAPI;
import io.github.lucfr1746.LSurvivalLib.Inventory.InventoryButton;
import io.github.lucfr1746.LSurvivalLib.Inventory.InventoryListener;
import io.github.lucfr1746.LSurvivalLib.Inventory.InventoryManager;
import io.github.lucfr1746.LSurvivalLib.ItemStack.ItemBuilderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ServerItemsGUI extends InventoryBuilderAPI {

    private final LItems plugin;
    private final int page;
    private final int total_page;

    public ServerItemsGUI(LItems plugin, int page) {
        this.plugin = plugin;
        this.page = page;
        this.total_page = (int) Math.ceil((double) ServerItemsLoader.getItemsList().size() / 45);
        setRows(6);
        setCurrentPage(page);
        setLockMode(LockMode.ALL);
        setTitle("Server Items (" + page + "/" + this.total_page + ")");
    }

    @Override
    public void decorate(Player player) {
        for (int i = 0; i <= 53; i++) {
            this.addButton(i, getBackground());
        }
        this.addButton(45, getPreviousPageButton());
        this.addButton(49, getCloseButton());
        this.addButton(53, getNextPageButton());

        List<ItemStack> itemList = ServerItemsLoader.getItemsList();
        for (int i = 0; i <= 44; i++) {
            int index = (page - 1) * 45 + i;
            if (index >= itemList.size()) break;
            this.addButton(i, getItem(itemList.get(index)));
        }
        new PlayerAPI(player).playButtonClickSound();
        super.decorate(player);
    }

    private InventoryButton getItem(ItemStack itemStack) {
        return new InventoryButton()
                .creator(player -> itemStack)
                .consumer(event -> {
                    PlayerAPI playerAPI = new PlayerAPI(event.getWhoClicked());
                    playerAPI.playSoundAtPlayerLoc(Sound.ENTITY_ITEM_PICKUP);
                    playerAPI.addItem(itemStack);
                });
    }

    private InventoryButton getNextPageButton() {
        if (this.page <= 0 || this.page >= this.total_page) return getBackground();
        return createNavigationButton(
                "&ePage " + (this.page + 1),
                this.page + 1
        );
    }

    private InventoryButton getPreviousPageButton() {
        if (this.page <= 1 || this.page > this.total_page) return getBackground();
        return createNavigationButton(
                "&ePage " + (this.page - 1),
                this.page - 1
        );
    }

    private InventoryButton createNavigationButton(String displayName, int targetPage) {
        return new InventoryButton()
                .creator(player -> {
                    ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.ARROW);
                    itemBuilder.setDisplayName(displayName, true);
                    return itemBuilder.setUnclassified(true).build();
                })
                .consumer(event -> {
                    InventoryManager inventoryManager = new InventoryManager();
                    InventoryListener guiListener = new InventoryListener(inventoryManager);
                    Bukkit.getPluginManager().registerEvents(guiListener, this.plugin);
                    inventoryManager.openGUI(this.plugin, new ServerItemsGUI(this.plugin, targetPage), (Player) event.getWhoClicked());
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
