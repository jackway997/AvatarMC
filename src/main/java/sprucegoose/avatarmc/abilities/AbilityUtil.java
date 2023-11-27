package sprucegoose.avatarmc.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class AbilityUtil
{
    private static final List<Material> REPLACEABLE_BLOCK_TYPES = Arrays.asList(
            Material.AIR, Material.GRASS, Material.TALL_GRASS, Material.DANDELION, Material.POPPY, Material.OXEYE_DAISY,
            Material.CORNFLOWER
    );

    private static final List<Material> ALLOWED_BLOCK_TYPES = Arrays.asList(
            Material.GRASS_BLOCK, Material.DIRT, Material.STONE, Material.COBBLESTONE,
            Material.SANDSTONE, Material.PODZOL, Material.DIRT_PATH, Material.COARSE_DIRT,
            Material.ROOTED_DIRT, Material.FARMLAND, Material.CLAY, Material.RED_SANDSTONE,
            Material.ANDESITE, Material.DIORITE, Material.GRANITE, Material.DEEPSLATE,
            Material.WARPED_NYLIUM, Material.CRIMSON_NYLIUM, Material.CALCITE, Material.TUFF
    );

    public static boolean blockReplaceable(Block block)
    {
        return REPLACEABLE_BLOCK_TYPES.contains(block.getType());
    }

    public static LivingEntity getHostileLOS(Player player, double range, double hitRadius)
    {
        double stepSize = 0.5;

        Location eyeLocation = player.getEyeLocation();
        Location currentLocation = eyeLocation.clone();
        Vector direction = currentLocation.getDirection().normalize().multiply(stepSize);

        double distance = 0.0;
        while(distance < range)
        {
            currentLocation.add(direction); // check if this works with locations and not vectors

            Collection<Entity> entitiesHit = currentLocation.getWorld().getNearbyEntities(currentLocation, hitRadius, hitRadius, hitRadius);
            entitiesHit.remove(player);
            if (!entitiesHit.isEmpty()) // if entity hit
            {
                for (Entity entity : entitiesHit)
                {
                    if (entity instanceof LivingEntity livingEntity)
                    {
                        return livingEntity;
                    }
                }
            }
            distance = eyeLocation.distance(currentLocation);
        }
        return null;
    }

    public static boolean getLocationUpDown(Location loc)
    {
        if (ALLOWED_BLOCK_TYPES.contains(loc.getBlock().getType()) &&
                REPLACEABLE_BLOCK_TYPES.contains(loc.clone().add(0, 1, 0).getBlock().getType()))
        {
            System.out.println("location in line");
            return true;
        }
        else if (ALLOWED_BLOCK_TYPES.contains(loc.subtract(0,1,0).getBlock().getType()) &&
                REPLACEABLE_BLOCK_TYPES.contains(loc.clone().add(0, 1, 0).getBlock().getType()))
        {
            System.out.println("location down one");
            return true;
        }
        else if (ALLOWED_BLOCK_TYPES.contains(loc.add(0,2,0).getBlock().getType()) &&
                REPLACEABLE_BLOCK_TYPES.contains(loc.clone().add(0, 1, 0).getBlock().getType()))
        {
            System.out.println("location up one");
            return true;
        }
        else
        {
            loc.add(0,1,0);
            return false;
        }
    }

}
