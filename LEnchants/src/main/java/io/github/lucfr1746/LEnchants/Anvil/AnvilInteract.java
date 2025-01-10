package io.github.lucfr1746.LEnchants.Anvil;

import io.github.lucfr1746.LEnchants.LEnchants;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class AnvilInteract implements Listener {

    public AnvilInteract(LEnchants plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onAnvilInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && (event.getClickedBlock().getType() == Material.ANVIL ||
                event.getClickedBlock().getType() == Material.CHIPPED_ANVIL || event.getClickedBlock().getType() == Material.DAMAGED_ANVIL)) {
            event.setCancelled(true);
            AnvilAPI.openAnvilGUI(event.getPlayer());
        }
    }
}
