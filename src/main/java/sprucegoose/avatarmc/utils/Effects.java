package sprucegoose.avatarmc.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Effects
{
    public static void testEffect()
    {

    }

    public static void createWater(JavaPlugin plugin, PlayerInteractEvent event)
    {
        Block targetBlockLocation = event.getPlayer().getTargetBlock(null, 100).getRelative(event.getBlockFace());
        targetBlockLocation.setType(Material.WATER);

        // With BukkitScheduler
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskLater(plugin, () ->
        {
            if (targetBlockLocation.getType().equals(Material.WATER))
            {
                targetBlockLocation.setType(Material.AIR);
            }
        }, 20L * 1L);
    }
}
