package io.github.lucfr1746.LEnchants;

import io.github.lucfr1746.LEnchants.Anvil.AnvilAPI;
import io.github.lucfr1746.LEnchants.Anvil.AnvilCommands;
import io.github.lucfr1746.LEnchants.Anvil.AnvilInteract;
import io.github.lucfr1746.LEnchants.EnchantingTable.EnchantingTableCommands;
import io.github.lucfr1746.LEnchants.EnchantingTable.EnchantingTableInteract;
import io.github.lucfr1746.LEnchants.EnchantingTable.EnchantingTableManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class LEnchants extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        prepareEnchanting();
        prepareAnvil();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void prepareEnchanting() {
        new EnchantingTableManager(this);
        new EnchantingTableInteract(this);
        new EnchantingTableCommands();
    }

    private void prepareAnvil() {
        new AnvilAPI(this).registerInventory();
        new AnvilInteract(this);
        new AnvilCommands();
    }
}
