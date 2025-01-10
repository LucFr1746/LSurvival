package io.github.lucfr1746.LEnchants.EnchantingTable;

import io.github.lucfr1746.LEnchants.LEnchants;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class EnchantingTableInteract implements Listener {

    public EnchantingTableInteract(LEnchants plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEnchantingTableInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.ENCHANTING_TABLE && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            EnchantingTableManager.openEnchantingTable(event.getPlayer(), countBookshelves(event.getClickedBlock()));
        }
    }

    public int countBookshelves(Block centerBlock) {
        int count = 0;
        World world = centerBlock.getWorld();
        int x = centerBlock.getX();
        int y = centerBlock.getY();
        int z = centerBlock.getZ();

        // Iterate through a 3x3x2 area (y to y+1) around the block
        for (int dx = -2; dx <= 2; dx++) {  // Expands the X range to -2 to +2
            for (int dz = -2; dz <= 2; dz++) {  // Expands the Z range to -2 to +2
                for (int dy = 0; dy <= 1; dy++) {  // Y range remains the same (y to y+1)
                    Block block = world.getBlockAt(x + dx, y + dy, z + dz);
                    if (block.getType() == Material.BOOKSHELF) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
}
