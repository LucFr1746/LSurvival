package io.github.lucfr1746.LEnchants.Anvil;

import io.github.lucfr1746.Shade.commandapi.CommandAPICommand;
import io.github.lucfr1746.Shade.commandapi.arguments.PlayerArgument;
import org.bukkit.entity.Player;

public class AnvilCommands {

    public AnvilCommands() {
        registerCommands();
    }

    private void registerCommands() {
        new CommandAPICommand("anvil")
                .withAliases("av", "anvilmenu")
                .withPermission("lsurvival.lenchants.anvil.use")
                .withOptionalArguments(new PlayerArgument("target"))
                .executesPlayer((player, args) -> {
                    Player target = (Player) args.getOrDefault("target", () -> player);
                    AnvilAPI.openAnvilGUI(target);
                })
                .register();
    }
}
