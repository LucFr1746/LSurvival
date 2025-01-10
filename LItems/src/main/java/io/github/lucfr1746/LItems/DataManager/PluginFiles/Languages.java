package io.github.lucfr1746.LItems.DataManager.PluginFiles;

import io.github.lucfr1746.LItems.LItems;
import io.github.lucfr1746.LSurvivalLib.Utils.APIs.ConfigAPI;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Languages {

    private final LItems plugin;
    private FileConfiguration langConfig;

    private static boolean   defaultItem_item_enabled = true;
    private static String       defaultItem_item_name = "&aSurvival Menu &7(Click)";
    private static List<String> defaultItem_item_lore = List.of("&7View all of your Survival progress,", "&7including your Skills, Collections,", "&7Recipes, and more!", "", "&eClick to open!");
    private static String      defaultItem_menu_title = "Survival Menu";

    public Languages(LItems plugin) {
        this.plugin = plugin;
        initializeLanguages();
        plugin.getLogger().info("  |- Languages files done!");
    }

    public static boolean isDefaultItem_item_enabled() {
        return defaultItem_item_enabled;
    }

    public static String getDefaultItem_item_name() {
        return defaultItem_item_name;
    }

    public static List<String> getDefaultItem_item_lore() {
        return defaultItem_item_lore;
    }

    public static String getDefaultItem_menu_title() {
        return defaultItem_menu_title;
    }

    private void initializeLanguages() {
        File languagesFolder = new ConfigAPI(plugin).createFolder(plugin.getDataFolder().getPath(), "languages");
        new ConfigAPI(plugin).createDefaultYamlFileConfiguration(languagesFolder.getPath(), "languages/", "en_US.yml");

        langConfig = new ConfigAPI(plugin).getYamlConfiguration(languagesFolder.getPath(), Config.getLanguage() + ".yml");
        readLanguageConfig();
    }

    private void readLanguageConfig() {
        configureDefaultItem();
    }
    private void configureDefaultItem() {
        defaultItem_item_enabled = this.langConfig.getBoolean("default-item.item.enabled");
        String defaultItem_itemName = this.langConfig.getString("default-item.item.name");
        defaultItem_item_name = "!none!".equals(defaultItem_itemName) ? " " : defaultItem_itemName;

        if (this.langConfig.isList("default-item.item.lore")) {
            defaultItem_item_lore = this.langConfig.getStringList("default-item.item.lore");
        } else {
            String lockedSlotLore = this.langConfig.getString("default-item.item.lore");
            defaultItem_item_lore = "!none!".equals(lockedSlotLore)
                    ? new ArrayList<>()
                    : Collections.singletonList(lockedSlotLore);
        }

        String defaultItem_menuTitle = this.langConfig.getString("default-item.menu.title");
        defaultItem_menu_title = "!none!".equals(defaultItem_menuTitle) ? " " : defaultItem_menuTitle;
    }
}
