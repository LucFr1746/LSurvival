package io.github.lucfr1746.LItems.ServerItems;

import com.google.gson.*;
import io.github.lucfr1746.LItems.LItems;
import io.github.lucfr1746.LItems.Utils.ItemJsonLoaderAPI;
import io.github.lucfr1746.LSurvivalLib.ItemStack.ItemBuilderAPI;
import io.github.lucfr1746.LSurvivalLib.Utils.APIs.ConfigAPI;
import io.github.lucfr1746.LSurvivalLib.Utils.APIs.FileAPI;
import io.github.lucfr1746.LSurvivalLib.Utils.APIs.LoggerAPI;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ServerItemsLoader {

    private final LItems plugin;
    private final LoggerAPI logger;
    private static final Map<String, ItemStack> itemsMap = new HashMap<>();
    private static final List<ItemStack> itemsList = new ArrayList<>();

    public ServerItemsLoader(LItems plugin) {
        this.plugin = plugin;
        this.logger = new LoggerAPI(this.plugin);
        loadItems();
    }

    private void loadItems() {
        File itemsJson = new FileAPI(this.plugin).createDefaultFile(plugin.getDataFolder().getPath(), "", "items.json");
        try (FileReader reader = new FileReader(itemsJson)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

            JsonArray itemsArray = jsonObject.getAsJsonArray("items");
            for (JsonElement item : itemsArray) {
                readItems(item.getAsJsonObject());
            }
            this.logger.success("Loaded " + itemsList.size() + " items.");
        } catch (IOException e) {
            this.plugin.getLogger().warning("There is an error while loading items!");
        }
    }

    private void readItems(JsonObject itemInJson) {
        String itemPrettyJson = new GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString(itemInJson.getAsJsonObject().toString()));

        ItemJsonLoaderAPI itemJsonLoaderAPI = new ItemJsonLoaderAPI(itemInJson);
        if (!itemJsonLoaderAPI.getExceptionList().isEmpty()) {
            for (Exception exception : itemJsonLoaderAPI.getExceptionList()) {
                this.logger.warning(exception.getMessage());
            }
            if (!itemJsonLoaderAPI.isValidItem()) {
                this.logger.warning("Recheck in LItems/items.json: \n" + itemPrettyJson);
                this.logger.warning("Skipping this items...");
                return;
            }
        }

        ItemBuilderAPI itemBuilderAPI = new ItemBuilderAPI(itemJsonLoaderAPI.getMaterial());
                       itemBuilderAPI.setItemName(itemJsonLoaderAPI.getName());
                       itemBuilderAPI.setId(itemJsonLoaderAPI.getId());
                       itemBuilderAPI.setNPCSellPrice(itemJsonLoaderAPI.getNpcSellPrice());
                       itemBuilderAPI.setCategory(itemJsonLoaderAPI.getCategory());
                       itemBuilderAPI.setTier(itemJsonLoaderAPI.getTier());
                       itemBuilderAPI.setDescription(itemJsonLoaderAPI.getDescription());
                       itemBuilderAPI.setFireworkPower(itemJsonLoaderAPI.getFireworkPower());
        loadItemAttributes(itemInJson, itemBuilderAPI);
        loadItemStats(itemInJson, itemBuilderAPI);
        ItemStack finalItem = itemBuilderAPI.build();
        if (!itemsMap.containsKey(itemInJson.get("id").getAsString())) {
            itemsMap.put(itemInJson.get("id").getAsString(), finalItem);
            itemsList.add(finalItem);
        } else {
            this.logger.warning("There is duplicated key: " + itemInJson.get("id").getAsString());
        }
    }

    private void loadItemAttributes(JsonObject itemInJson, ItemBuilderAPI itemBuilderAPI) {
        JsonElement attributesElement = itemInJson.get("attributes");
        if (attributesElement != null) {
            JsonObject attributeObject;
            if (attributesElement.isJsonArray()) {
                for (JsonElement stat : attributesElement.getAsJsonArray()) {
                    attributeObject = stat.getAsJsonObject();
                    for (Map.Entry<String, JsonElement> entry : attributeObject.entrySet()) {
                        String attributeName = entry.getKey();
                        JsonElement attributeValueElement = entry.getValue();
                        itemBuilderAPI.setAttribute(attributeName, attributeValueElement.getAsDouble());
                    }
                }
            } else if (attributesElement.isJsonObject()) {
                attributeObject = attributesElement.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : attributeObject.entrySet()) {
                    String attributeName = entry.getKey();
                    JsonElement attributeValueElement = entry.getValue();
                    itemBuilderAPI.setAttribute(attributeName, attributeValueElement.getAsDouble());
                }
            }
        }

    }

    private void loadItemStats(JsonObject itemInJson, ItemBuilderAPI itemBuilderAPI) {
        Map<String, BiConsumer<ItemBuilderAPI, Double>> statSetters = new HashMap<>();
        statSetters.put("HEALTH", ItemBuilderAPI::setHealth);
        statSetters.put("DEFENSE", ItemBuilderAPI::setDefense);
        statSetters.put("STRENGTH", ItemBuilderAPI::setStrength);
        statSetters.put("INTELLIGENCE", ItemBuilderAPI::setIntelligence);
        statSetters.put("CRIT_CHANCE", ItemBuilderAPI::setCritChance);
        statSetters.put("CRIT_DAMAGE", ItemBuilderAPI::setCritDamage);
        statSetters.put("BONUS_ATTACK_SPEED", ItemBuilderAPI::setBonusAttackSpeed);
        statSetters.put("TRUE_DEFENSE", ItemBuilderAPI::setTrueDefense);
        statSetters.put("FEROCITY", ItemBuilderAPI::setFerocity);
        statSetters.put("HEALTH_REGEN", ItemBuilderAPI::setHealthRegen);
        statSetters.put("VITALITY", ItemBuilderAPI::setVitality);
        statSetters.put("SWING_RANGE", ItemBuilderAPI::setSwingRange);

        statSetters.put("MINING_SPEED", ItemBuilderAPI::setMiningSpeed);
        statSetters.put("MINING_FORTUNE", ItemBuilderAPI::setMiningFortune);
        statSetters.put("MINING_SPREAD", ItemBuilderAPI::setMiningSpread);
        statSetters.put("FARMING_FORTUNE", ItemBuilderAPI::setFarmingFortune);
        statSetters.put("FORAGING_FORTUNE", ItemBuilderAPI::setForagingFortune);
        statSetters.put("BREAKING_POWER", ItemBuilderAPI::setBreakingPower);

        statSetters.put("ALCHEMY_WISDOM", ItemBuilderAPI::setAlchemyWisdom);
        statSetters.put("CARPENTRY_WISDOM", ItemBuilderAPI::setCarpentryWisdom);
        statSetters.put("COMBAT_WISDOM", ItemBuilderAPI::setCombatWisdom);
        statSetters.put("ENCHANTING_WISDOM", ItemBuilderAPI::setEnchantingWisdom);
        statSetters.put("FARMING_WISDOM", ItemBuilderAPI::setFarmingWisdom);
        statSetters.put("FISHING_WISDOM", ItemBuilderAPI::setFishingWisdom);
        statSetters.put("FORAGING_WISDOM", ItemBuilderAPI::setForagingWisdom);
        statSetters.put("MINING_WISDOM", ItemBuilderAPI::setMiningWisdom);

        statSetters.put("WALK_SPEED", ItemBuilderAPI::setWalkSpeed);
        statSetters.put("MAGIC_FIND", ItemBuilderAPI::setMagicFind);

        statSetters.put("ABSORPTION", ItemBuilderAPI::setAbsorption);
        statSetters.put("DAMAGE", ItemBuilderAPI::setDamage);
        statSetters.put("TRUE_DAMAGE", ItemBuilderAPI::setTrueDamage);

        JsonElement statsElement = itemInJson.get("stats");
        if (statsElement != null) {
            JsonObject statObject;
            if (statsElement.isJsonArray()) {
                for (JsonElement stat : statsElement.getAsJsonArray()) {
                    statObject = stat.getAsJsonObject();
                    processStatObject(statObject, statSetters, itemBuilderAPI);
                }
            } else if (statsElement.isJsonObject()) {
                statObject = statsElement.getAsJsonObject();
                processStatObject(statObject, statSetters, itemBuilderAPI);
            }
        }
    }

    private void processStatObject(JsonObject statObject, Map<String, BiConsumer<ItemBuilderAPI, Double>> statSetters, ItemBuilderAPI itemBuilderAPI) {
        for (Map.Entry<String, JsonElement> entry : statObject.entrySet()) {
            String statName = entry.getKey();
            JsonElement statValueElement = entry.getValue();

            if (statSetters.containsKey(statName)) {
                if (statValueElement != null) {
                    double statValue = statValueElement.getAsDouble();
                    if (statValue != 0) {
                        statSetters.get(statName).accept(itemBuilderAPI, statValue);
                    }
                }
            } else {
                this.logger.warning("There is no stat named: " + statName);
            }
        }
    }

    public static Map<String, ItemStack> getItemsMap() {
        return itemsMap;
    }

    public static List<ItemStack> getItemsList() {
        return itemsList;
    }
}
