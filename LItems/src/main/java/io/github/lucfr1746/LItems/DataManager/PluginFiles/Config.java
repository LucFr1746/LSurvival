package io.github.lucfr1746.LItems.DataManager.PluginFiles;

import io.github.lucfr1746.LItems.LItems;
import io.github.lucfr1746.LSurvivalLib.Utils.APIs.ConfigAPI;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    private final LItems plugin;
    private FileConfiguration config;

    private static String language = "en_US";

    public Config(LItems plugin) {
        this.plugin = plugin;
        initializeConfig();
        plugin.getLogger().info("  |- Config file done!");
    }

    public static String getLanguage() {
        return language;
    }

    private void initializeConfig() {
        config = new ConfigAPI(plugin).createDefaultYamlFileConfiguration(plugin.getDataFolder().getPath(), "", "config.yml");
        readConfig();
    }

    private void readConfig() {
        language = config.getString("language", "en_US");
        this.plugin.getLogger().info("  |- " + language + " language file selected!");
    }
}
