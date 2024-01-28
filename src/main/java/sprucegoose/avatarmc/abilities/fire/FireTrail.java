package sprucegoose.avatarmc.abilities.fire;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.BlockUtil;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.*;

public class FireTrail extends Ability implements Listener {
    private final Map<UUID, List<Location>> fireTrailBlocks = new HashMap<>();
    private final Set<UUID> fireTrailEnabledEntities = new HashSet<>();
    private final Set<UUID> fireTrailProtectionEntities = new HashSet<>();
    private final int fireTrailDuration = 3 * 20; // 3 seconds in ticks (20 ticks per second)
    private final int cleanupDelay = 20;
    private final String NOSPREAD_KEY = "nospread";

    public FireTrail(JavaPlugin plugin, RegionProtectionManager regProtMan)
    {
        super(plugin, regProtMan, ELEMENT_TYPE.fire, ABILITY_LEVEL.adept);
        setCooldown(2000);
    }
    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e)
    {
        EquipmentSlot slot = e.getHand();
        ItemStack item = e.getItem();
        Player player = e.getPlayer();

        if (    slot != null && item != null &&
                (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) &&
                (slot.equals(EquipmentSlot.HAND) || slot.equals(EquipmentSlot.OFF_HAND)) &&
                abilityChecks(player, item) && !onCooldown(player)
                && !fireTrailEnabledEntities.contains(player.getUniqueId())
                && !fireTrailProtectionEntities.contains(player.getUniqueId())
        )
        {
            addCooldown(player, item);
            startTrail(plugin, player);
            e.setCancelled(true);
        }
    }

    public void doHostileAbilityAsMob(LivingEntity caster, LivingEntity target)
    {
        startTrail(plugin, caster);
    }

    public boolean startTrail(JavaPlugin plugin, LivingEntity caster)
    {
        if (!regProtManager.isLocationPVPEnabled(caster, caster.getLocation()))
        {
            return false;
        }

        UUID casterUUID = caster.getUniqueId();
        fireTrailEnabledEntities.add(casterUUID);
        fireTrailProtectionEntities.add(casterUUID);

        // Apply speed boost when enabling the fire trail
        caster.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, fireTrailDuration, 3));

        //player.sendMessage(ChatColor.DARK_RED + "Fire trail enabled. You now move faster.");

        BukkitRunnable burnTask = new BukkitRunnable() {
            @Override
            public void run() {
                burnBehindBlock(caster);
            }
        };
        burnTask.runTaskTimer(plugin, 0L,1L);

        BukkitRunnable cancelTask = new BukkitRunnable() {
            @Override
            public void run() {
                fireTrailEnabledEntities.remove(casterUUID);
                if(!burnTask.isCancelled())
                {
                    burnTask.cancel();
                }
            }
        };
        cancelTask.runTaskLater(plugin, fireTrailDuration);

        BukkitRunnable removeProtectionTask = new BukkitRunnable() {
            @Override
            public void run() {
                fireTrailProtectionEntities.remove(casterUUID);
            }
        };
        removeProtectionTask.runTaskLater(plugin, fireTrailDuration + cleanupDelay + 2);

        return true;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        EntityDamageEvent.DamageCause cause = event.getCause();

        if ((cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.FIRE_TICK)
                && fireTrailProtectionEntities.contains(entity.getUniqueId()))
        {
            event.setCancelled(true); // Prevent damage to entities with fire trail enabled.
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event)
    {
        if(BlockUtil.blockHasMeta(event.getBlock(),NOSPREAD_KEY))
        {
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent e) {
        Location fireLoc = e.getSource().getLocation();
        World w = fireLoc.getWorld();
        if(BlockUtil.blockHasMeta(w.getBlockAt(fireLoc.subtract(0, 1, 0)), NOSPREAD_KEY)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityCombustEvent(EntityCombustEvent event)
    {
        Entity entity = event.getEntity();

        if (fireTrailProtectionEntities.contains(entity.getUniqueId()))
        {
            event.setCancelled(true);
        }
    }

    public void burnBehindBlock(LivingEntity entity)
    {
        if (fireTrailEnabledEntities.contains(entity.getUniqueId()))
        {
            Vector direction = entity.getLocation().getDirection().normalize().multiply(-1);
            Location blockBehind = entity.getLocation().add(direction);
            Location blockBehindBelow = blockBehind.clone().add(0,-1,0);

            if (    !regProtManager.isLocationPVPEnabled(entity, entity.getLocation()) ||
                    !regProtManager.isLocationPVPEnabled(entity, blockBehind))
            { // end fire trail if player enters protected area
                fireTrailProtectionEntities.remove(entity.getUniqueId());
                return;
            }
            if (isPassableBlock(blockBehind.getBlock()))
            {
                Block fireBlock = blockBehind.getBlock();
                Block fireBlockBase = blockBehindBelow.getBlock();
                fireBlock.setType(Material.FIRE);
                setNoSpread(fireBlockBase);

                fireTrailBlocks.computeIfAbsent(entity.getUniqueId(), k -> new ArrayList<>()).add(blockBehind); // Store fire block location

                // Schedule the removal of the fire block after a short delay
                BukkitRunnable cleanupTask = new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        if (fireTrailBlocks.containsKey(entity.getUniqueId()))
                        {
                            fireBlock.setType(Material.AIR);
                            fireTrailBlocks.get(entity.getUniqueId()).remove(blockBehind); // Remove the location from the list
                        }
                    }
                };

                cleanupTask.runTaskLater(plugin, cleanupDelay);
            }
        }
    }

    // Helper method to check if a block is passable for fire trail placement
    private boolean isPassableBlock(Block block)
    {
        Material blockType = block.getType();
        return blockType == Material.AIR || blockType == Material.CAVE_AIR || blockType == Material.VOID_AIR;
    }

    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Blaze it bro");
        return getAbilityItem(plugin, player, lore, 1);
    }

    private void setNoSpread(Block block)
    {
        String time = BlockUtil.setTimeStampedBlockMeta(plugin,NOSPREAD_KEY, block);
        Bukkit.getScheduler().runTaskLater(plugin, () ->
        {
            if (BlockUtil.blockHasMetaAtTime(block,NOSPREAD_KEY,time))
            {
                BlockUtil.removeTimeStampedBlockMeta(plugin,NOSPREAD_KEY,block);
            }
        }, cleanupDelay + 20);
    }
}