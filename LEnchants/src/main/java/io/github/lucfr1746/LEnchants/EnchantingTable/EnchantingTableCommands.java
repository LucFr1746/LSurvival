package io.github.lucfr1746.LEnchants.EnchantingTable;

import io.github.lucfr1746.Shade.commandapi.CommandAPICommand;
import io.github.lucfr1746.Shade.commandapi.arguments.PlayerArgument;
import org.bukkit.entity.Player;

public class EnchantingTableCommands {

    public EnchantingTableCommands() {
        registerCommands();
    }

    private void registerCommands() {
        new CommandAPICommand("etable")
                .withAliases("et")
                .withPermission("lsurvival.lenchants.enchanting.use-table")
                .withOptionalArguments(new PlayerArgument("target"))
                .executesPlayer((player, args) -> {
                    Player targetPlayer = (Player) args.getOrDefault("target", () -> player);
                    EnchantingTableManager.openEnchantingTable(targetPlayer, 32);
                })
                .register();
    }
}
