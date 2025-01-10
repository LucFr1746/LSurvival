package io.github.lucfr1746.LItems.Utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.lucfr1746.LSurvivalLib.ItemStack.Category.Category;
import io.github.lucfr1746.LSurvivalLib.ItemStack.ItemBuilderAPI;
import io.github.lucfr1746.LSurvivalLib.ItemStack.Tier.Tier;
import io.github.lucfr1746.LSurvivalLib.Utils.APIs.TextAPI;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class ItemJsonLoaderAPI {

    private Material material;
    private String name;
    private String id;
    private double npcSellPrice;
    private Category category;
    private Tier tier;
    private String description;

    private int fireworkPower;

    private boolean validItem = true;
    private final List<Exception> exceptions = new ArrayList<>();

    public Material getMaterial() {
        return this.material;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }

    public double getNpcSellPrice() {
        return this.npcSellPrice;
    }

    public Category getCategory() {
        return this.category;
    }

    public Tier getTier() {
        return this.tier;
    }

    public String getDescription() {
        return this.description;
    }

    public int getFireworkPower() {
        return this.fireworkPower;
    }

    public boolean isValidItem() {
        return this.validItem;
    }

    public List<Exception> getExceptionList() {
        return this.exceptions;
    }

    public ItemJsonLoaderAPI(JsonObject itemInJson) {
        loadMaterial(itemInJson);
        loadName(itemInJson);
        loadId(itemInJson);
        loadNpcSellPrice(itemInJson);
        loadCategory(itemInJson);
        loadTier(itemInJson);
        loadDescription(itemInJson);
        loadFireworkPower(itemInJson);
    }

    private void loadMaterial(JsonObject itemInJson) {
        try {
            this.material = parseEnumValue(Material.class, itemInJson, "material");
        } catch (IllegalArgumentException e) {
            invalidateItem(new IllegalArgumentException("Invalid material: " + parseStringValue(itemInJson, "material")));
        } catch (NullPointerException e) {
            invalidateItem(new NullPointerException("Missing key: material"));
        }
    }

    private void loadName(JsonObject itemInJson) {
        if (this.material != null) {
            try {
                this.name = parseStringValue(itemInJson, "name");
            } catch (NullPointerException e) {
                this.name = getDefaultItemName();
                addWarning(new IllegalArgumentException("Missing key: name, using default: " + this.name));
            }
        }
    }

    private void loadId(JsonObject itemInJson) {
        if (this.material != null) {
            try {
                this.id = parseStringValue(itemInJson, "id");
            } catch (NullPointerException e) {
                this.id = generateDefaultItemId();
                addWarning(new NullPointerException("Missing key: id, using default: " + this.id));
            }
        }
    }

    private void loadNpcSellPrice(JsonObject itemInJson) {
        try {
            this.npcSellPrice = parseDoubleValue(itemInJson, "npc_sell_price");
        } catch (NullPointerException e) {
            this.npcSellPrice = 0.0;
        }
    }

    private void loadCategory(JsonObject itemInJson) {
        try {
            this.category = parseEnumValue(Category.class, itemInJson, "category");
        } catch (IllegalArgumentException | NullPointerException e) {
            this.category = Category.NONE;
        }
    }

    private void loadTier(JsonObject itemInJson) {
        try {
            this.tier = parseEnumValue(Tier.class, itemInJson, "tier");
        } catch (IllegalArgumentException | NullPointerException e) {
            this.tier = Tier.COMMON;
        }
    }

    private void loadDescription(JsonObject itemInJson) {
        try {
            this.description = parseStringValue(itemInJson, "description");
        } catch (NullPointerException e) {
            this.description = "";
        }
    }

    private void loadFireworkPower(JsonObject itemInJson) {
        try {
            this.fireworkPower = parseIntValue(itemInJson, "firework_power");
        } catch (NullPointerException e) {
            this.fireworkPower = 0;
        }
    }

    private String parseStringValue(JsonObject itemInJson, String key) {
        return parseValue(itemInJson, key, String.class);
    }

    private int parseIntValue(JsonObject itemInJson, String key) {
        return parseValue(itemInJson, key, Integer.class);
    }

    private double parseDoubleValue(JsonObject itemInJson, String key) {
        return parseValue(itemInJson, key, Double.class);
    }

    private <T> T parseValue(JsonObject itemInJson, String key, Class<T> type) {
        if (itemInJson == null || !itemInJson.has(key)) {
            throw new NullPointerException("Missing key: " + key);
        }

        try {
            JsonElement element = itemInJson.get(key);
            if (type == String.class) {
                return type.cast(element.getAsString());
            } else if (type == Double.class) {
                return type.cast(element.getAsDouble());
            } else if (type == Integer.class) {
                return type.cast(element.getAsInt());
            } else if (type == Boolean.class) {
                return type.cast(element.getAsBoolean());
            } else {
                throw new IllegalArgumentException("Unsupported type: " + type);
            }
        } catch (ClassCastException | IllegalStateException e) {
            throw new IllegalArgumentException("Key '" + key + "' has invalid type: expected " + type.getSimpleName(), e);
        }
    }

    private <T extends Enum<T>> T parseEnumValue(Class<T> enumClass, JsonObject itemJson, String key) {
        String value = parseStringValue(itemJson, key);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Empty value for key: " + key);
        }

        try {
            return Enum.valueOf(enumClass, formatEnumValue(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid enum value '" + value + "' for " + enumClass.getSimpleName(), e);
        }
    }

    private String formatEnumValue(String value) {
        return value.trim().toUpperCase().replace(" ", "_");
    }

    private String getDefaultItemName() {
        return new ItemBuilderAPI(material).getDefaultName();
    }

    private String generateDefaultItemId() {
        return new TextAPI(new ItemBuilderAPI(material).getDefaultName()).convertToEnumStringFormat().build();
    }

    private void invalidateItem(Exception exception) {
        this.validItem = false;
        this.exceptions.add(exception);
    }

    private void addWarning(Exception exception) {
        this.exceptions.add(exception);
    }
}
