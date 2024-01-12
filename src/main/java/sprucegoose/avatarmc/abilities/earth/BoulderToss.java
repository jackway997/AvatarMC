package sprucegoose.avatarmc.abilities.earth;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.bukkit.Bukkit;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.BlockUtil;
import sprucegoose.avatarmc.utils.ItemMetaTag;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.*;

/*
Notes: bugs: when you toss the same block multiple times, sometimes the
 scheduler will remove a block from a previous toss
 */

public class BoulderToss extends Ability
{
    private final Map<UUID, Boolean> explosionOccurred = new HashMap<>();
    private final Map<UUID, Integer> activeBends = new HashMap<>();
    private final int maxNumBends = 1;
    private final String blockDataKey = "BoulderToss";

    private final List<Material> ALLOWED_BLOCK_TYPES = Arrays.asList(
            Material.GRASS_BLOCK, Material.DIRT, Material.STONE, Material.COBBLESTONE,
            Material.SANDSTONE, Material.PODZOL, Material.DIRT_PATH, Material.COARSE_DIRT,
            Material.ROOTED_DIRT, Material.FARMLAND, Material.CLAY, Material.RED_SANDSTONE,
            Material.ANDESITE, Material.DIORITE, Material.GRANITE, Material.DEEPSLATE,
            Material.WARPED_NYLIUM, Material.CRIMSON_NYLIUM, Material.CALCITE, Material.TUFF
    );

    public BoulderToss(JavaPlugin plugin, RegionProtectionManager regProtManager)
    {
        super(plugin, regProtManager, ELEMENT_TYPE.earth, ABILITY_LEVEL.expert);
        setCooldown(1);
    }



    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        UUID playerUUID = player.getUniqueId();
        EquipmentSlot slot = e.getHand();
        ItemStack item = e.getItem();

        // Check if the player is holding the Earth item
        if (    slot != null && item != null &&
                e.getAction().equals(Action.RIGHT_CLICK_BLOCK) &&
                (slot.equals(EquipmentSlot.HAND) || slot.equals(EquipmentSlot.OFF_HAND)) &&
                AvatarIDs.itemStackHasAvatarID(plugin,item, this.getAbilityID()) &&
                PlayerIDs.itemStackHasPlayerID(plugin, item, player))
        {

            Block clickedBlock = e.getClickedBlock();
            if (clickedBlock == null)
            {
                e.setCancelled(true);
                return;
            }

            if (BlockUtil.blockHasMeta(clickedBlock, blockDataKey)) // If this is a cloned block
            {
                launchBlock(player, clickedBlock);
            }
            else // Player is trying to bend a new block
            {
                if (!onCooldown(player)) {
                    if (createNewBoulder(player, clickedBlock))
                    {
                        addCooldown(player, item);
                    }
                }
            }
            e.setCancelled(true);
        }
    }

    @Override
    public void doHostileAbilityAsMob(LivingEntity caster, LivingEntity target)
    {
        Block block = caster.getLocation().clone().add(0,-1,0).getBlock();
        launchBlock(caster,block);
    }

    private boolean createNewBoulder(LivingEntity caster, Block block)
    {
        UUID playerUUID = caster.getUniqueId();

        if (    !regProtManager.isLocationPVPEnabled(caster, caster.getLocation()) ||
                !regProtManager.isLocationPVPEnabled(caster, block.getLocation()))
        {
            return false;
        }

        activeBends.putIfAbsent(playerUUID, 0);
        if ((activeBends.get(playerUUID) < maxNumBends)) // If a cloned block doesn't exist for player
        {
            if (isAllowedBlockType(block.getType())) // if block is of allowed type
            {
                // Check if the target block location is air
                Block destinationBlock = block.getLocation().add(0, 2, 0).getBlock();
                if (destinationBlock.getType() == Material.AIR && !BlockUtil.blockHasMeta(destinationBlock, blockDataKey))
                {
                    // Clone the block
                    cloneBlock(caster, block);
                    return true;
                }
                else
                {
                    //player.sendMessage(ChatColor.RED + "There is no room to bend!");
                    return false;
                }
            }
            else
            {
                //player.sendMessage(ChatColor.RED + "You are unable to bend with " + clickedBlock.getType() + "!");
                return false;
            }
        }
        else
        {
            //player.sendMessage(ChatColor.BLUE + "You can only clone one block at a time!");
            return false;
        }
    }

    @EventHandler
    private void onEntityChangeBlockEvent(EntityChangeBlockEvent event)
    {
        if (event.getEntityType() == EntityType.FALLING_BLOCK &&
                !BlockUtil.blockHasMeta(event.getBlock(),blockDataKey) &&
                BlockUtil.blockHasMeta((FallingBlock)event.getEntity(), blockDataKey))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event)
    {
        if (BlockUtil.blockHasMeta(event.getBlock(), blockDataKey))
        {
            event.setCancelled(true);
        }
    }

    private void cloneBlock(LivingEntity caster, Block clickedBlock)
    {
        Block destinationBlock = clickedBlock.getLocation().add(0, 2, 0).getBlock();
        UUID uuid = caster.getUniqueId();
        //plugin.getLogger().info("Cloning block: " + clickedBlock.getType() + " at location: " + destinationBlock.getLocation());
        destinationBlock.setType(clickedBlock.getType());
        String bendTime = BlockUtil.setTimeStampedBlockMeta(plugin, blockDataKey, destinationBlock);

        // Schedule removal of the cloned block
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if(BlockUtil.blockHasMetaAtTime(destinationBlock, blockDataKey, bendTime))
            {
                removeCloneBlock(caster, destinationBlock);
            }
        }, 40L); // 2 seconds delay

        activeBends.put(uuid,activeBends.get(uuid)+1);
        //System.out.println("count increased to " + activeBends.get(uuid));
    }

    private void launchBlock(LivingEntity caster, Block block)
    {
        explosionOccurred.put(caster.getUniqueId(), false);

        // Calculate direction
        double pitch = Math.toRadians(caster.getLocation().getPitch());
        double yaw = Math.toRadians(caster.getLocation().getYaw());
        double x = Math.sin(-yaw) * Math.cos(pitch);
        double y = Math.sin(-pitch);
        double z = Math.cos(-yaw) * Math.cos(pitch);
        Vector direction = new Vector(x, y, z).normalize();
        direction.multiply(2);

        FallingBlock fallingBlock;

        if(caster instanceof Player)
        {
            fallingBlock = block.getWorld().spawnFallingBlock(block.getLocation().add(0.5, 0, 0.5), block.getBlockData());
            removeCloneBlock(caster, block);
        }
        else
        {
            fallingBlock = block.getWorld().spawnFallingBlock(block.getLocation().clone().add(0.5,2,0.5),block.getBlockData());
        }

        BlockUtil.setTimeStampedBlockMeta(plugin, blockDataKey, fallingBlock);
        fallingBlock.setDropItem(false); // Set the drop item property to false to prevent it from dropping items
        fallingBlock.setVelocity(direction);

        Bukkit.getScheduler().runTaskLater(plugin, () -> checkForCollision(fallingBlock, caster), 1L);
    }

    public void removeCloneBlock(LivingEntity caster, Block block) {
        UUID uuid = caster.getUniqueId();

        if (block != null && BlockUtil.blockHasMeta(block, blockDataKey))
        {
            BlockUtil.removeTimeStampedBlockMeta(plugin, blockDataKey, block);
            block.setType(Material.AIR);
            activeBends.put(uuid,activeBends.get(uuid)-1);
        }
    }

    public void checkForCollision(FallingBlock fallingBlock, LivingEntity caster) {
        Location blockLocation = fallingBlock.getLocation();
        UUID uuid = caster.getUniqueId();
        if (explosionOccurred.get(uuid)) {
            return;
        }

        if (fallingBlock.isOnGround() || fallingBlock.getVelocity().lengthSquared() < 0.7) {
            //plugin.getLogger().info("Collision detected with ground!");
            customExplosion(blockLocation, 1.0f, caster);
            performCleanup(fallingBlock, blockLocation, caster);
        } else {
            List<Entity> nearbyEntities = fallingBlock.getNearbyEntities(0.5, 0.5, 0.5);
            for (Entity entity : nearbyEntities) {
                if (entity instanceof LivingEntity && entity.getUniqueId() != uuid) {
                    //plugin.getLogger().info("Collision detected with entity: " + entity.getType());
                    customExplosion(blockLocation, 1.0f, caster); // Apply the explosion for entities
                    performCleanup(fallingBlock, blockLocation, caster);
                    return;
                }
            }
            if (!explosionOccurred.get(uuid)) {
                // Reschedule collision check
                Bukkit.getScheduler().runTaskLater(plugin, () -> checkForCollision(fallingBlock, caster), 1L);
            }
        }
    }

    private void performCleanup(FallingBlock fallingBlock, Location blockLocation, LivingEntity caster) {
        fallingBlock.remove();
        //blockLocation.getBlock().setType(Material.AIR);
        explosionOccurred.put(caster.getUniqueId(), true);
    }

    public void customExplosion(Location location, float power, LivingEntity caster) {
        if (explosionOccurred.get(caster.getUniqueId())) {
            return;
        }
        location.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location, 25);
        location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

        // Get nearby entities within the explosion radius
        double explosionRadius = 3; // Adjust this value to define the explosion radius
        Collection<Entity> nearbyEntitiesCollection = location.getWorld().getNearbyEntities(location, explosionRadius, explosionRadius, explosionRadius);
        List<Entity> nearbyEntities = new ArrayList<>(nearbyEntitiesCollection);

        // Calculate the fixed amount of damage to be applied to each nearby entity
        double fixedDamage = 5.0; // Adjust this value to set the amount of damage

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity le && entity.getUniqueId() != caster.getUniqueId())
            {
                if (regProtManager.isLocationPVPEnabled(caster, entity.getLocation()))
                {
                    // Apply the fixed damage to the entity
                    regProtManager.tagEntity(le, caster);
                    ((LivingEntity) entity).damage(fixedDamage);
                }
            }
        }
        explosionOccurred.put(caster.getUniqueId(), true);
    }

    public boolean isAllowedBlockType(Material material) {
        return ALLOWED_BLOCK_TYPES.contains(material);
    }

    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Boulder go brrrrrrr");
        return getAbilityItem(plugin, player, lore, 2);
    }
}
