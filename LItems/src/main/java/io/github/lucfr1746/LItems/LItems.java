package io.github.lucfr1746.LItems;

import io.github.lucfr1746.LItems.Commands.ServerItemsCommands;
import io.github.lucfr1746.LItems.DataManager.DataManager;
import io.github.lucfr1746.LItems.DataManager.PluginFiles.Languages;
import io.github.lucfr1746.LItems.ServerItems.ServerItemsLoader;
import io.github.lucfr1746.LItems.ServerItems.MenuItem.SurvivalMenuItem;
import org.bukkit.plugin.java.JavaPlugin;

public final class LItems extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        registerFiles();
        loadItems();
        registerCommands();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerFiles() {
        new DataManager(this);
    }

    private void loadItems() {
        new ServerItemsLoader(this);
        if (Languages.isDefaultItem_item_enabled()) {
            new SurvivalMenuItem(this);
        }
    }

    private void registerCommands() {
        // server-items
            new ServerItemsCommands(this);
    }
}
