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
import org.checkerframework.framework.qual.NoQualifierParameter;
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

            if (doAbilityAsPlayer(plugin, player))
            {
                addCooldown(player, item);
            }
            e.setCancelled(true);
        }
    }

    @Override
    public void doHostileAbilityAsMob(LivingEntity caster, LivingEntity target)
    {
        Vector directionVector = target.getLocation().clone().subtract(caster.getLocation().clone()).toVector().normalize();
        doAbility(plugin, caster, directionVector);
    }

    public boolean doAbilityAsPlayer(JavaPlugin plugin, Player player)
    {
        Vector playerEyeVector = player.getEyeLocation().clone().getDirection();
        Vector directionVector = new Vector(playerEyeVector.getX(),0,playerEyeVector.getZ()).normalize();

        return doAbility(plugin, player, directionVector);
    }

    private boolean doAbility(JavaPlugin plugin, LivingEntity caster, Vector directionVector)
    {
        final double maxDistance = 16;
        final double stepSize = 0.5;
        final double shockRadius = 2.0;

        // don't execute ability if entity is in a PVP disabled zone
        if (!regProtManager.isLocationPVPEnabled(caster, caster.getLocation()))
        {
            return false;
        }

        Location skillLocation = caster.getLocation().clone();
        skillLocation.add(directionVector.clone().multiply(1.5)); // check this value
        Location castLocation = skillLocation.clone();

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                double distance = castLocation.distance(skillLocation);
                if (distance < maxDistance)
                {
                    if (!checkLocUpDown(skillLocation))
                    {
                        this.cancel();
                        return;
                    }

                    Block shockBlock = skillLocation.getBlock();

                    if (blockCanBeShocked(shockBlock)) // && is not in protected region
                    {
                        if (regProtManager.isLocationPVPEnabled(caster, shockBlock.getLocation()))
                        {
                            shockBlock(shockBlock, caster);
                        }
                        else // cancel skill execution
                        {
                            if (!this.isCancelled())
                            {
                                this.cancel();
                                return;
                            }
                        }
                    }

                    // shock other blocks along that axis if possible
                    double theta = skillLocation.getYaw();

                    // left side
                    Location leftShockLoc = skillLocation.clone();
                    Vector leftStepVector = new Vector(1*stepSize*Math.cos(theta*Math.PI/180),0, 1*stepSize*Math.sin(theta*Math.PI/180));
                    for (double ii = 0.0; ii < shockRadius; ii = ii + stepSize)
                    {
                        leftShockLoc.add(leftStepVector);
                        checkLocUpDown(leftShockLoc);
                        Block leftShockBlock = leftShockLoc.getBlock();


                        if (blockCanBeShocked(leftShockBlock)) // && is not in protected region
                        {
                            if (regProtManager.isLocationPVPEnabled(caster, leftShockBlock.getLocation()))
                            {
                                shockBlock(leftShockBlock, caster);
                            }
                            else // cancel skill execution
                            {
                                if (!this.isCancelled())
                                {
                                    this.cancel();
                                    return;
                                }
                            }
                        }
                    }

                    // right side
                    Location rightShockLoc = skillLocation.clone();
                    Vector rightStepVector = new Vector(-1*stepSize*Math.cos(theta*Math.PI/180),0, -1*stepSize*Math.sin(theta*Math.PI/180));
                    for (double ii = 0.0; ii < shockRadius; ii = ii + stepSize)
                    {
                        rightShockLoc.add(rightStepVector);
                        checkLocUpDown(rightShockLoc);
                        Block rightShockBlock = rightShockLoc.getBlock();

                        if (blockCanBeShocked(rightShockBlock)) // && is not in protected region
                        {
                            if (regProtManager.isLocationPVPEnabled(caster, rightShockBlock.getLocation()))
                            {
                                shockBlock(rightShockBlock, caster);
                            }
                            else // cancel skill execution
                            {
                                if (!this.isCancelled())
                                {
                                    this.cancel();
                                    return;
                                }
                            }

                        }
                    }
                        skillLocation.add(directionVector);
                } else
                {
                    //System.out.println("End of Shock at distance = " + distance);
                    if (!this.isCancelled())
                    {
                        this.cancel();
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 2L);

        return true;
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

    private void shockBlock(Block block, LivingEntity caster)
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
            if (!(entity.getUniqueId() == caster.getUniqueId()))
            {
                if (entity instanceof LivingEntity livingEntity &&
                        ((EntityUtil.getTimeElapsedEntityMeta(entity, shockTimeKey) > shockTimeCooldown) ||
                        (EntityUtil.getTimeElapsedEntityMeta(entity, shockTimeKey) < 0)) &&
                        regProtManager.isLocationPVPEnabled(caster, livingEntity.getLocation()))
                {
                    livingEntity.damage(8, caster);
                    livingEntity.setVelocity(new Vector(0,1,0).multiply(1));
                    livingEntity.setNoDamageTicks(8);
                    EntityUtil.setTimeStampedEntityMeta(plugin, shockTimeKey, livingEntity);
                }
            }
        }
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
