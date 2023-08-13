package sprucegoose.avatarmc.abilities;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.bukkit.Bukkit;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.*;

public class EarthBend extends Ability implements Listener
{
    private final Map<UUID, Boolean> explosionOccurred = new HashMap<>();
    private final Map<UUID, Integer> activeBends = new HashMap<>();
    private int maxNumBends = 3;

    private final List<Material> ALLOWED_BLOCK_TYPES = Arrays.asList(
            Material.GRASS_BLOCK, Material.DIRT, Material.STONE, Material.COBBLESTONE,
            Material.SANDSTONE, Material.PODZOL, Material.DIRT_PATH, Material.COARSE_DIRT,
            Material.ROOTED_DIRT, Material.FARMLAND, Material.CLAY, Material.RED_SANDSTONE,
            Material.ANDESITE, Material.DIORITE, Material.GRANITE, Material.DEEPSLATE,
            Material.WARPED_NYLIUM, Material.CRIMSON_NYLIUM, Material.CALCITE, Material.TUFF
    );

    public EarthBend(JavaPlugin plugin)
    {
        super(plugin);
        setCooldown(1);
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        UUID playerUUID = player.getUniqueId();
        EquipmentSlot slot = e.getHand();
        ItemStack item = e.getItem();

        if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }

        // Check if the player is holding the Earth item
        if (    slot != null && item != null &&
                e.getAction().equals(Action.RIGHT_CLICK_BLOCK) &&
                (slot.equals(EquipmentSlot.HAND) || slot.equals(EquipmentSlot.OFF_HAND)) &&
                AvatarIDs.itemStackHasAvatarID(plugin,item, EarthBend.class.getSimpleName()) &&
                PlayerIDs.itemStackHasPlayerID(plugin, item, player))
        {

            Block clickedBlock = e.getClickedBlock();
            if (clickedBlock == null)
            {
                e.setCancelled(true);
                return;
            }

            if (clickedBlock.hasMetadata("isClone")) // If this is a cloned block
            {
                launchBlock(player, clickedBlock);
            }
            else // Player is trying to bend a new block
            {
                activeBends.putIfAbsent(playerUUID, 0);
                if ((activeBends.get(playerUUID) < maxNumBends) && !onCooldown(player)) // If a cloned block doesn't exist for player
                {
                    if (isAllowedBlockType(clickedBlock.getType())) // if block is of allowed type
                    {
                        // Check if the target block location is air
                        Block destinationBlock = clickedBlock.getLocation().add(0, 2, 0).getBlock();
                        if (destinationBlock.getType() == Material.AIR && !destinationBlock.hasMetadata("isClone")) {
                            // Clone the block
                            cloneBlock(player, clickedBlock);
                            addCooldown(player, item);
                        }
                        else
                        {
                            //player.sendMessage(ChatColor.RED + "There is no room to bend!");
                        }
                    }
                    else
                    {
                        //player.sendMessage(ChatColor.RED + "You are unable to bend with " + clickedBlock.getType() + "!");
                    }
                }
                else
                {
                    //player.sendMessage(ChatColor.BLUE + "You can only clone one block at a time!");
                }

            }
            e.setCancelled(true);
        }
    }

    private void cloneBlock(Player player, Block clickedBlock) {
        Block destinationBlock = clickedBlock.getLocation().add(0, 2, 0).getBlock();
        UUID uuid = player.getUniqueId();
        //plugin.getLogger().info("Cloning block: " + clickedBlock.getType() + " at location: " + destinationBlock.getLocation());
        destinationBlock.setType(clickedBlock.getType());
        destinationBlock.setMetadata("isClone", new FixedMetadataValue(plugin, true));

        scheduleCloneBlockRemoval(player, destinationBlock);
        activeBends.put(uuid,activeBends.get(uuid)+1);
        //System.out.println("count increased to " + activeBends.get(uuid));
    }

    private void launchBlock(Player player, Block block)
    {
        //plugin.getLogger().info("Launching block.");

        UUID playerUUID = player.getUniqueId(); // Get the player's UUID
        Location blockLocation = block.getLocation();

        explosionOccurred.put(player.getUniqueId(), false);

        //plugin.getLogger().info("Launching block: " + block.getType() + " at location: " + blockLocation);

        // Calculate direction
        double pitch = Math.toRadians(player.getLocation().getPitch());
        double yaw = Math.toRadians(player.getLocation().getYaw());
        double x = Math.sin(-yaw) * Math.cos(pitch);
        double y = Math.sin(-pitch);
        double z = Math.cos(-yaw) * Math.cos(pitch);
        Vector direction = new Vector(x, y, z).normalize();
        direction.multiply(2);
        FallingBlock fallingBlock = block.getWorld().spawnFallingBlock(block.getLocation().add(0.5, 0, 0.5), block.getBlockData());
        removeCloneBlock(player, block);
        fallingBlock.setDropItem(false); // Set the drop item property to false to prevent it from dropping items
        fallingBlock.setVelocity(direction);

        Bukkit.getScheduler().runTaskLater(plugin, () -> checkForCollision(fallingBlock, player), 1L);
    }

    public void removeCloneBlock(Player player, Block block) {
        UUID uuid = player.getUniqueId();

        if (block != null && block.hasMetadata("isClone"))
        {
            block.removeMetadata("isClone", plugin);
            block.setType(Material.AIR);
            activeBends.put(uuid,activeBends.get(uuid)-1);
            //System.out.println("count decreased to " + activeBends.get(uuid));
            //plugin.getLogger().info("Removed cloned block.");
        }
        else
        {
            //plugin.getLogger().info("Unable to remove block: " + block);
        }
    }

    public void checkForCollision(FallingBlock fallingBlock, Player player) {
        Location blockLocation = fallingBlock.getLocation();
        UUID uuid = player.getUniqueId();
        if (explosionOccurred.get(uuid)) {
            return;
        }

        if (fallingBlock.isOnGround() || fallingBlock.getVelocity().lengthSquared() < 0.7) {
            //plugin.getLogger().info("Collision detected with ground!");
            customExplosion(blockLocation, 1.0f, player);
            performCleanup(fallingBlock, blockLocation, player);
        } else {
            List<Entity> nearbyEntities = fallingBlock.getNearbyEntities(0.5, 0.5, 0.5);
            for (Entity entity : nearbyEntities) {
                if (entity instanceof LivingEntity && entity.getUniqueId() != uuid) {
                    //plugin.getLogger().info("Collision detected with entity: " + entity.getType());
                    customExplosion(blockLocation, 1.0f, player); // Apply the explosion for entities
                    performCleanup(fallingBlock, blockLocation, player);
                    return;
                }
            }
            if (!explosionOccurred.get(uuid)) {
                // Reschedule collision check
                Bukkit.getScheduler().runTaskLater(plugin, () -> checkForCollision(fallingBlock, player), 1L);
            }
        }
    }

    private void performCleanup(FallingBlock fallingBlock, Location blockLocation, Player player) {
        fallingBlock.remove();
        blockLocation.getBlock().setType(Material.AIR);
        explosionOccurred.put(player.getUniqueId(), true);
    }

    public void customExplosion(Location location, float power, Player player) {
        if (explosionOccurred.get(player.getUniqueId())) {
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
            if (entity instanceof LivingEntity) {
                // Apply the fixed damage to the entity
                ((LivingEntity) entity).damage(fixedDamage);

            }
        }
        explosionOccurred.put(player.getUniqueId(), true);
    }


    public boolean isAllowedBlockType(Material material) {
        return ALLOWED_BLOCK_TYPES.contains(material);
    }

    private void scheduleCloneBlockRemoval(Player player, Block clonedBlock) {

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                removeCloneBlock(player, clonedBlock);
        }, 40L); // 2 seconds delay
    }

    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        ItemStack skill = new ItemStack(Material.STICK, 1);
        ItemMeta skill_meta = skill.getItemMeta();
        skill_meta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Boulder Toss");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "(Soulbound)");
        lore.add(ChatColor.GRAY + "Boulder go brrrrrrr");
        skill_meta.setCustomModelData(1);
        skill_meta.setLore(lore);
        skill.setItemMeta(skill_meta);

        AvatarIDs.setItemStackAvatarID(plugin, skill, EarthBend.class.getSimpleName());
        PlayerIDs.setItemStackPlayerID(plugin, skill, player);

        return skill;
    }
}
