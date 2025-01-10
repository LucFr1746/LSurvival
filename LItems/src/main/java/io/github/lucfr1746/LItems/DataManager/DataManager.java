package io.github.lucfr1746.LItems.DataManager;

import io.github.lucfr1746.LItems.DataManager.PluginFiles.Config;
import io.github.lucfr1746.LItems.DataManager.PluginFiles.Languages;
import io.github.lucfr1746.LItems.LItems;

public class DataManager {

    public DataManager(LItems plugin) {
        plugin.getLogger().info("Registering and initializing files...");
            new Config(plugin);
            new Languages(plugin);
        plugin.getLogger().info("Completed registering and initializing files!");
    }
}
