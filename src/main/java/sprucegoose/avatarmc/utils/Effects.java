package sprucegoose.avatarmc.utils;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class Effects
{
    public static void testEffect()
    {

    }

    public static void createWater(JavaPlugin plugin, PlayerInteractEvent event)
    {
        Block targetBlockLocation = event.getPlayer().getTargetBlock(null, 100).getRelative(event.getBlockFace());
        targetBlockLocation.setType(Material.WATER);
        targetBlockLocation.getWorld().playSound(targetBlockLocation.getLocation(), Sound.ENTITY_DOLPHIN_SPLASH, 0.5f, 1.0f);

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

    public static void airBlast(Player player) {

        double maxRange = 300.0; // Adjust the maximum range as desired
        double knockbackStrength = 3; // Adjust the knockback strength as needed
        double damage = 1;

        Location location = player.getEyeLocation();
        Vector direction = location.getDirection().normalize();

        double stepSize = 0.1;
        double distance = 0.0;

        while (distance < maxRange) {
            Location stepLocation = location.clone().add(direction.clone().multiply(distance));
            Block block = stepLocation.getBlock();

            if (block.getType().isSolid()) {
                break;
            }

            RayTraceResult result = player.getWorld().rayTraceEntities(location, direction, distance, e -> e instanceof LivingEntity && e != player);
            if (result != null) {
                Entity hitEntity = result.getHitEntity();
                if (hitEntity instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity) hitEntity;
                    livingEntity.damage(damage); // Adjust the damage as needed

                    Location hitLocation = livingEntity.getLocation();
                    hitLocation.getWorld().spawnParticle(Particle.SMOKE_NORMAL, hitLocation, 1);
                    hitLocation.getWorld().playSound(hitLocation, Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.0f);

                    Vector knockbackDirection = hitLocation.toVector().subtract(location.toVector()).normalize();
                    knockbackDirection.setY(0.15); // Adjust the vertical knockback as needed
                    livingEntity.setVelocity(knockbackDirection.multiply(knockbackStrength));
                }
            }

            distance += stepSize;
        }
    }

}
