package sprucegoose.avatarmc.abilities;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.BlockUtil;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.*;

public class FireTrail extends Ability implements Listener {
    private final Map<UUID, List<Location>> fireTrailBlocks = new HashMap<>();
    private final Set<UUID> fireTrailEnabledPlayers = new HashSet<>();
    private final Set<UUID> fireTrailProtectionPlayers = new HashSet<>();
    private final int fireTrailDuration = 3 * 20; // 3 seconds in ticks (20 ticks per second)
    private final int cleanupDelay = 5;
    private static final String NOSPREAD_KEY = "nospread";


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
                AvatarIDs.itemStackHasAvatarID(plugin,item, this.getAbilityID()) &&
                PlayerIDs.itemStackHasPlayerID(plugin, item, player) && !onCooldown(player)
                && !fireTrailEnabledPlayers.contains(player.getUniqueId())
                && !fireTrailProtectionPlayers.contains(player.getUniqueId())
        )
        {
            addCooldown(player, item);
            startTrail(plugin, player);
            e.setCancelled(true);
        }
    }

    public boolean startTrail(JavaPlugin plugin, Player player)
    {
        UUID playerUUID = player.getUniqueId();
        fireTrailEnabledPlayers.add(playerUUID);
        fireTrailProtectionPlayers.add(playerUUID);


        // Apply speed boost when enabling the fire trail
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, fireTrailDuration, 3));

        //player.sendMessage(ChatColor.DARK_RED + "Fire trail enabled. You now move faster.");
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            fireTrailEnabledPlayers.remove(playerUUID);
        }, fireTrailDuration);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            fireTrailProtectionPlayers.remove(playerUUID);
        }, fireTrailDuration + cleanupDelay + 2);

        return true;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (fireTrailEnabledPlayers.contains(playerUUID)) {
            Vector direction = player.getLocation().getDirection().normalize().multiply(-1);
            Location blockBehind = player.getLocation().add(direction);
            Location blockBehindBelow = blockBehind.add(0,-1,0);

            if (isPassableBlock(blockBehind.getBlock()))
            {
                Block fireBlock = blockBehind.getBlock();
                Block fireBlockBase = blockBehindBelow.getBlock();
                fireBlock.setType(Material.FIRE);
                //setNoSpread(fireBlockBase);

                fireTrailBlocks.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(blockBehind); // Store fire block location

                // Schedule the removal of the fire block after a short delay
                BukkitRunnable cleanupTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (fireTrailBlocks.containsKey(playerUUID)) {
                            fireBlock.setType(Material.AIR);
                            fireTrailBlocks.get(playerUUID).remove(blockBehind); // Remove the location from the list
                        }
                    }
                };

                cleanupTask.runTaskLater(plugin, cleanupDelay);
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            Player player = (Player) event.getEntity();
            UUID playerUUID = player.getUniqueId();
            EntityDamageEvent.DamageCause cause = event.getCause();
            if ((cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.FIRE_TICK)
                    && fireTrailProtectionPlayers.contains(playerUUID)) {
                event.setCancelled(true); // Prevent damage to players with fire trail enabled.
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityCombustEvent(EntityCombustEvent event){
        Entity entity = event.getEntity();

        if(entity instanceof Player){
            Player player = (Player) entity;
            if (fireTrailProtectionPlayers.contains(player.getUniqueId()))
            {
                event.setCancelled(true);
            }
        }
    }

    // Helper method to check if a block is passable for fire trail placement
    private boolean isPassableBlock(Block block) {
        Material blockType = block.getType();
        return blockType == Material.AIR || blockType == Material.CAVE_AIR || blockType == Material.VOID_AIR;
    }

    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Blaze it bro");
        return getAbilityItem(plugin, player, lore, 1);
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent e) {
        Location fireLoc = e.getSource().getLocation();
        World w = fireLoc.getWorld();
        if(w.getBlockAt(fireLoc.subtract(0, 1, 0)).hasMetadata(NOSPREAD_KEY)) {
            //e.setCancelled(true);
        }
    }

    private void setNoSpread(Block block) {
        String time = BlockUtil.setTimeStampedBlockMeta(plugin,NOSPREAD_KEY, block);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (BlockUtil.blockHasMetaAtTime(block,NOSPREAD_KEY,time))
            {
                BlockUtil.removeTimeStampedBlockMeta(plugin,NOSPREAD_KEY,block);
            }
        }, cleanupDelay + 20);
    }
}