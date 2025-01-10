package io.github.lucfr1746.LItems.ServerItems.MenuItem;

import io.github.lucfr1746.LItems.DataManager.PluginFiles.Languages;
import io.github.lucfr1746.LItems.LItems;
import io.github.lucfr1746.LSurvivalLib.Inventory.InventoryListener;
import io.github.lucfr1746.LSurvivalLib.Inventory.InventoryManager;
import io.github.lucfr1746.LSurvivalLib.ItemStack.ItemBuilderAPI;
import io.github.lucfr1746.Shade.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class SurvivalMenuItem implements Listener {

    private final LItems plugin;
    private final ItemStack survivalMenuItem;
    private final InventoryManager inventoryManager;

    public SurvivalMenuItem(LItems plugin) {
        this.plugin = plugin;
        this.survivalMenuItem = createSurvivalMenuItem();
        this.inventoryManager = new InventoryManager();

        // Register event listeners
        InventoryListener guiListener = new InventoryListener(inventoryManager);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getPluginManager().registerEvents(guiListener, plugin);

        registerCommand(); // svmenu
    }

    /**
     * Handles giving the survival menu item to players when they join.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().getInventory().setItem(8, this.survivalMenuItem);
    }

    /**
     * Handles player inventory clicks to open the survival menu GUI.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isSurvivalMenuItem(event.getCurrentItem())) return;

        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);

        if (!isCraftingInventory(player)) return;
        if (player.getGameMode() == GameMode.SURVIVAL) {
            this.inventoryManager.openGUI(this.plugin, new SurvivalMenu(this.plugin), player);
        } else if (player.getGameMode() == GameMode.CREATIVE) {
            resetCreativeInventory(player);
            this.inventoryManager.openGUI(this.plugin, new SurvivalMenu(this.plugin), player);
        }
    }

    /**
     * Handles players dropping the survival menu item to open the GUI.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!isSurvivalMenuItem(event.getItemDrop().getItemStack())) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        this.inventoryManager.openGUI(plugin, new SurvivalMenu(this.plugin), player);

        if (player.getOpenInventory().getType() == InventoryType.CREATIVE) {
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                player.getInventory().remove(this.survivalMenuItem);
                player.getInventory().setItem(8, this.survivalMenuItem);
            });
        }
    }

    /**
     * Handles interactions with the survival menu item to open the GUI.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (isSurvivalMenuItem(event.getItem())) {
            event.setCancelled(true);
            this.inventoryManager.openGUI(plugin, new SurvivalMenu(this.plugin), event.getPlayer());
        }
    }

    /**
     * Creates the survival menu item with predefined properties.
     */
    private ItemStack createSurvivalMenuItem() {
        ItemBuilderAPI itemBuilder = new ItemBuilderAPI(Material.NETHER_STAR);
        itemBuilder.setDisplayName(Languages.getDefaultItem_item_name(), true);
        itemBuilder.setLores(Languages.getDefaultItem_item_lore());
        itemBuilder.setId("SURVIVAL_MENU");
        return itemBuilder
                .setUnclassified(true)
                .setUnique(true)
                .setAllFlags()
                .build();
    }

    /**
     * Checks if the given item is the survival menu item.
     */
    private boolean isSurvivalMenuItem(ItemStack item) {
        return item != null && item.getType() != Material.AIR &&
                new ItemBuilderAPI(item).getId().equals("SURVIVAL_MENU");
    }

    /**
     * Checks if the player is in a crafting inventory.
     */
    private boolean isCraftingInventory(Player player) {
        return player.getOpenInventory().getTopInventory().getType() == InventoryType.CRAFTING;
    }

    /**
     * Resets the inventory state for players in creative mode.
     */
    private void resetCreativeInventory(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.getInventory().setItem(8, this.survivalMenuItem);
            player.setItemOnCursor(new ItemStack(Material.AIR));
            player.updateInventory();
        });
    }

    private void registerCommand() {
        new CommandAPICommand("survival-menu")
                .withAliases("svmenu", "viewsvmenu")
                .executesPlayer((player, args) -> {
                    this.inventoryManager.openGUI(this.plugin, new SurvivalMenu(this.plugin), player);
                })
                .register();
    }
}
