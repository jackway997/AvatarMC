package sprucegoose.avatarmc.abilities;

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
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.BlockUtil;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.*;

public class ShockWave extends Ability implements Listener
{

    // To do: check interaction between two shockwaves

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
    private final String blockDataKey = "Shockwave";

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
        Vector directionVector = player.getEyeLocation().clone().getDirection();
        Vector newDirectionVector = new Vector(directionVector.getX(),0,directionVector.getZ()).normalize();
        Location loc = player.getLocation().clone();
        loc.add(directionVector.clone().multiply(2));
        Location originalLocation = loc.clone();
        double maxDistance = 16;
        double stepSize = 0.5;
        Set<Integer> affectedEntities = new HashSet<>();

        // Schedule Task to animate flame
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                double distance = originalLocation.distance(loc);
                if (distance < maxDistance)
                {
                    if (BlockUtil.blockHasMeta(loc.getBlock(),blockDataKey) ||
                        (!loc.getBlock().isEmpty() && loc.clone().add(0, 1, 0).getBlock().isPassable()))
                    {
                        //System.out.println("block across valid");
                    }
                    else if (BlockUtil.blockHasMeta(loc.subtract(0,1,0).getBlock(),blockDataKey) ||
                            (!loc.getBlock().isEmpty() && loc.clone().add(0, 1, 0).getBlock().isPassable()))
                    {
                        //System.out.println("block down valid");
                    }
                    else if ((BlockUtil.blockHasMeta(loc.add(0,2,0).getBlock(),blockDataKey) ||
                            (!loc.getBlock().isEmpty() && loc.clone().add(0, 1, 0).getBlock().isPassable())))
                    {
                        //System.out.println("block up valid");
                    }
                    else
                    {
                        //System.out.println("End of Shock at distance = " + distance);
                        this.cancel();
                    }

                    Block shockBlock = loc.getBlock();
                    //System.out.println("can be shocked? "+ blockCanBeShocked(shockBlock));
                    if (blockCanBeShocked(shockBlock)) // && is not in protected region
                    {
                        shockBlock(shockBlock);
                    }
                    for (Entity entity : loc.getWorld().getNearbyEntities(loc, 1, 2, 1))
                    {
                        if (!(entity instanceof Player && ((Player)entity).getUniqueId() == player.getUniqueId()))
                        {
                            if (entity instanceof LivingEntity && !affectedEntities.contains(entity.getEntityId()))
                            {
                                LivingEntity livingEntity = (LivingEntity) entity;
                                livingEntity.damage(8, player);
                                livingEntity.setVelocity(new Vector(0,1,0).multiply(1));
                                livingEntity.setNoDamageTicks(8);
                                //System.out.println("damaging: "+ livingEntity.getEntityId());
                                affectedEntities.add(livingEntity.getEntityId());
                            }
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

    private void shockBlock(Block block)
    {
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
