package sprucegoose.avatarmc.abilities.earth;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.BlockUtil;
import sprucegoose.avatarmc.utils.EntityUtil;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.*;

public class ShockWave extends Ability implements Listener
{

    // To do: check interaction between two shockwaves

    private final String shockTimeKey = "ShockTimeKey";

    public ShockWave(JavaPlugin plugin, RegionProtectionManager regProtMan)
    {
        super(plugin, regProtMan, ELEMENT_TYPE.earth, ABILITY_LEVEL.expert);
        setCooldown(5000);
    }

    private final List<Material> ALLOWED_BLOCK_TYPES = Arrays.asList(
            Material.GRASS_BLOCK, Material.DIRT, Material.STONE, Material.COBBLESTONE,
            Material.SANDSTONE, Material.PODZOL, Material.DIRT_PATH, Material.COARSE_DIRT,
            Material.ROOTED_DIRT, Material.FARMLAND, Material.CLAY, Material.RED_SANDSTONE,
            Material.ANDESITE, Material.DIORITE, Material.GRANITE, Material.DEEPSLATE,
            Material.WARPED_NYLIUM, Material.CRIMSON_NYLIUM, Material.CALCITE, Material.TUFF
    );

    private Map<UUID, BukkitTask> activeAbilities = new HashMap<>();
    private final String blockDataKey = "ShockWave";

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e)
    {
        EquipmentSlot slot = e.getHand();
        ItemStack item = e.getItem();
        Player player = e.getPlayer();

        if (    slot != null && item != null &&
                (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) &&
                (slot.equals(EquipmentSlot.HAND) || slot.equals(EquipmentSlot.OFF_HAND)) &&
                AvatarIDs.itemStackHasAvatarID(plugin,item, this.getAbilityID()) &&
                PlayerIDs.itemStackHasPlayerID(plugin, item, player) && !onCooldown(player)
        )
        {
            addCooldown(player, item);
            doAbility(plugin, player);
            e.setCancelled(true);
        }
    }

    private void doAbility(JavaPlugin plugin, Player player)
    {
        final double maxDistance = 16;
        final double stepSize = 0.5;
        final double shockRadius = 2.0;

        Vector directionVector = player.getEyeLocation().clone().getDirection();
        Vector newDirectionVector = new Vector(directionVector.getX(),0,directionVector.getZ()).normalize();
        Location loc = player.getLocation().clone();
        loc.add(directionVector.clone().multiply(2));
        Location originalLocation = loc.clone();


        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                double distance = originalLocation.distance(loc);
                if (distance < maxDistance)
                {
                    if (!checkLocUpDown(loc))
                    {
                        this.cancel();
                    }

                    Block shockBlock = loc.getBlock();


                    //System.out.println("can be shocked? "+ blockCanBeShocked(shockBlock));
                    if (blockCanBeShocked(shockBlock)) // && is not in protected region
                    {
                        shockBlock(shockBlock, player);
                    }

                    // shock other blocks along that axis if possible
                    double theta = loc.getYaw();

                    Location leftShockLoc = loc.clone();
                    Vector leftStepVector = new Vector(1*stepSize*Math.cos(theta*Math.PI/180),0, 1*stepSize*Math.sin(theta*Math.PI/180));
                    for (double ii = 0.0; ii < shockRadius; ii = ii + stepSize)
                    {
                        leftShockLoc.add(leftStepVector);
                        checkLocUpDown(leftShockLoc);
                        Block leftShockBlock = leftShockLoc.getBlock();

                        if (blockCanBeShocked(leftShockBlock)) // && is not in protected region
                        {
                            shockBlock(leftShockBlock, player);
                        }
                    }

                    // right side
                    Location rightShockLoc = loc.clone();
                    Vector rightStepVector = new Vector(-1*stepSize*Math.cos(theta*Math.PI/180),0, -1*stepSize*Math.sin(theta*Math.PI/180));
                    for (double ii = 0.0; ii < shockRadius; ii = ii + stepSize)
                    {
                        rightShockLoc.add(rightStepVector);
                        checkLocUpDown(rightShockLoc);
                        Block rightShockBlock = rightShockLoc.getBlock();

                        if (blockCanBeShocked(rightShockBlock)) // && is not in protected region
                        {
                            shockBlock(rightShockBlock, player);
                        }
                    }
                        loc.add(newDirectionVector);
                } else
                {
                    //System.out.println("End of Shock at distance = " + distance);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 2L);
    }

    // returns whether a location is a valid place for a shock, and modifies the location of the block in place
    private boolean checkLocUpDown(Location loc)
    {
        if (BlockUtil.blockHasMeta(loc.getBlock(),blockDataKey) ||
                (!loc.getBlock().isEmpty() && loc.clone().add(0, 1, 0).getBlock().isPassable()))
        {
            return true;
        }
        else if (BlockUtil.blockHasMeta(loc.subtract(0,1,0).getBlock(),blockDataKey) ||
                (!loc.getBlock().isEmpty() && loc.clone().add(0, 1, 0).getBlock().isPassable()))
        {
            return true;
        }
        else if ((BlockUtil.blockHasMeta(loc.add(0,2,0).getBlock(),blockDataKey) ||
                (!loc.getBlock().isEmpty() && loc.clone().add(0, 1, 0).getBlock().isPassable())))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private void shockBlock(Block block, Player player)
    {
        // Constants
        final double shockTimeCooldown = 2.0;

        Block destinationBlock = block.getLocation().clone().add(0, 1, 0).getBlock();
        destinationBlock.setType(block.getType());
        destinationBlock.getWorld().spawnParticle(Particle.BLOCK_DUST,
                destinationBlock.getLocation().clone().add(0.5,1,0.5),5, block.getBlockData());
        String bendTime = BlockUtil.setTimeStampedBlockMeta(plugin, blockDataKey, destinationBlock);

        // Schedule removal of the cloned block
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if(BlockUtil.blockHasMetaAtTime(destinationBlock, blockDataKey, bendTime))
            {
                unShockBlock(destinationBlock);
            }
        }, 10L); // 1 second delay

        Location loc = block.getLocation();
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, 1, 2, 1))
        {
            if (!(entity instanceof Player && entity.getUniqueId() == player.getUniqueId()))
            {
                if (entity instanceof LivingEntity livingEntity &&
                        ((EntityUtil.getTimeElapsedEntityMeta(entity, shockTimeKey) > shockTimeCooldown) ||
                        (EntityUtil.getTimeElapsedEntityMeta(entity, shockTimeKey) < 0)))
                {
                    livingEntity.damage(8, player);
                    livingEntity.setVelocity(new Vector(0,1,0).multiply(1));
                    livingEntity.setNoDamageTicks(8);
                    System.out.println("damaging: "+ livingEntity.getEntityId());
                    EntityUtil.setTimeStampedEntityMeta(plugin, shockTimeKey, livingEntity);
                }
            }
        }
        //System.out.println("count increased to " + activeBends.get(uuid));
    }

    private boolean blockCanBeShocked(Block block)
    {
        Block destinationBlock = block.getLocation().clone().add(0, 1, 0).getBlock();
        return (isAllowedBlockType(block.getType()) && !BlockUtil.blockHasMeta(block, blockDataKey) &&
                (destinationBlock.getType() == Material.AIR || destinationBlock.getType() == Material.CAVE_AIR ));
    }

    public boolean isAllowedBlockType(Material material)
    {
        return ALLOWED_BLOCK_TYPES.contains(material);
    }

    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Stompy stomp");

        return getAbilityItem(plugin, player, lore, 1);
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event)
    {
        if (BlockUtil.blockHasMeta(event.getBlock(), blockDataKey))
        {
            event.setCancelled(true);
        }
    }

    public void unShockBlock(Block block) {

        if (block != null && BlockUtil.blockHasMeta(block, blockDataKey))
        {
            BlockUtil.removeTimeStampedBlockMeta(plugin, blockDataKey, block);
            block.setType(Material.AIR);
        }
    }
}
