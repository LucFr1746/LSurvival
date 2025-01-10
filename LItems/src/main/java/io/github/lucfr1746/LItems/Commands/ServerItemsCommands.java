package io.github.lucfr1746.LItems.Commands;

import io.github.lucfr1746.LItems.LItems;
import io.github.lucfr1746.LItems.ServerItems.ServerItemsGUI;
import io.github.lucfr1746.LItems.ServerItems.ServerItemsLoader;
import io.github.lucfr1746.LSurvivalLib.Inventory.InventoryListener;
import io.github.lucfr1746.LSurvivalLib.Inventory.InventoryManager;
import io.github.lucfr1746.Shade.commandapi.CommandAPICommand;
import io.github.lucfr1746.Shade.commandapi.arguments.IntegerArgument;
import io.github.lucfr1746.Shade.commandapi.arguments.PlayerArgument;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ServerItemsCommands {

    private final LItems plugin;
    private final InventoryManager inventoryManager;

    public ServerItemsCommands(LItems plugin) {
        this.plugin = plugin;
        this.inventoryManager = new InventoryManager();
        InventoryListener guiListener = new InventoryListener(inventoryManager);
        Bukkit.getPluginManager().registerEvents(guiListener, this.plugin);
        registerCommands();
    }

    private void registerCommands() {
        new CommandAPICommand("server-items")
                .withPermission("lsurvival.litems.server-items.use")
                .withOptionalArguments(new IntegerArgument("page", 1, (int) Math.ceil((double) ServerItemsLoader.getItemsList().size() / 45)))
                .withOptionalArguments(new PlayerArgument("target"))
                .executesPlayer((player, args) -> {
                    int targetPage = (int) args.getOrDefault("page", () -> 1);
                    Player targetPlayer = (Player) args.getOrDefault("target", () -> player);
                    this.inventoryManager.openGUI(this.plugin, new ServerItemsGUI(this.plugin, targetPage), targetPlayer);
                })
                .register();
    }
}
