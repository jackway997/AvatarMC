package sprucegoose.avatarmc.abilities.fire;

import de.slikey.effectlib.util.ParticleEffect;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.BlockUtil;
import sprucegoose.avatarmc.utils.ImageParticles;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.*;

public class Firebolt extends Ability
{
    private final Map<UUID, Boolean> explosionOccurred = new HashMap<>();
    private Map<UUID, BukkitTask> activeBends = new HashMap<>();
    private Map<UUID, BukkitTask> flyingBends = new HashMap<>();
    private Map<UUID, BukkitTask> cancelTasks = new HashMap<>();

    public Firebolt(JavaPlugin plugin, RegionProtectionManager regProtManager)
    {
        super(plugin, regProtManager, ELEMENT_TYPE.fire, ABILITY_LEVEL.adept);
        setCooldown(3);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        Player player = e.getPlayer();
        UUID playerUUID = player.getUniqueId();
        EquipmentSlot slot = e.getHand();
        ItemStack item = e.getItem();

        // Check if the player is holding the skill item
        if (slot != null && item != null &&
                (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) &&
                (slot.equals(EquipmentSlot.HAND) || slot.equals(EquipmentSlot.OFF_HAND)) &&
                AvatarIDs.itemStackHasAvatarID(plugin, item, this.getAbilityID()) &&
                PlayerIDs.itemStackHasPlayerID(plugin, item, player) && !flyingBends.containsKey(playerUUID))
        {
            addCooldown(player, item);
            doAbility(plugin, player);
            e.setCancelled(true);
        }
    }

    public ItemStack getAbilityItem (JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Practice makes perfect");
        return getAbilityItem(plugin, player, lore, 2);
    }

    public Location getFireLoc (Player player)
    {
        double sideScaleFactor = 0.4;
        double fireDownScaleFactor = 0.2;

        Location fireLoc = player.getEyeLocation();
        Vector sideWayOffset = player.getLocation().getDirection().crossProduct(new Vector(0, 1, 0)).normalize().multiply(sideScaleFactor);
        Vector fireDownOffset = player.getLocation().getDirection().crossProduct(sideWayOffset).normalize().multiply(fireDownScaleFactor);

        //Add offsets
        fireLoc.add(fireLoc.getDirection().multiply(1));
        fireLoc.add(sideWayOffset);
        fireLoc.add(fireDownOffset);

        return fireLoc;
    }

    private void doAbility(JavaPlugin plugin, Player player)
    {
        UUID playerUUID = player.getUniqueId();

        if (!activeBends.containsKey(playerUUID)) // If not currently bending flame
        {

            // Schedule Task to animate flame
            BukkitScheduler scheduler = Bukkit.getScheduler();
            BukkitTask task1 = scheduler.runTaskTimer(plugin, () ->
            {
                Location fireLoc = getFireLoc(player);

                player.getWorld().spawnParticle(Particle.FLAME, fireLoc, 2, 0, 0, 0, 0);
                player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, fireLoc, 1, 0, 0, 0, 0);
            }, 0, 1L);
            activeBends.put(playerUUID, task1);

            // With BukkitScheduler
            BukkitTask task2 = scheduler.runTaskLater(plugin, () ->
            {
                BukkitTask animationTask = activeBends.get(playerUUID);
                if (animationTask != null)
                {
                    animationTask.cancel();
                    activeBends.remove(playerUUID);
                    cancelTasks.remove(playerUUID);
                }

            }, 5L * 20L);
            cancelTasks.put(playerUUID, task2);
        }
        else // launch flame
        {
            double maxRange = 24.0; // Adjust the maximum range as desired
            double stepSize = 1;
            Location location = getFireLoc(player);
            Location originalLocation = location.clone();
            Vector direction = player.getEyeLocation().getDirection().normalize();

            BukkitTask animationTask = activeBends.get(playerUUID);
            if (animationTask != null)
            {
                animationTask.cancel();
                activeBends.remove(playerUUID);
            }

            BukkitTask cancelTask = cancelTasks.get(playerUUID);
            if(cancelTask != null)
            {
                cancelTask.cancel();
                cancelTasks.remove(playerUUID);
            }

            BukkitScheduler scheduler = Bukkit.getScheduler();
            BukkitTask task1 = scheduler.runTaskTimer(plugin, () ->
            {
                double distance = originalLocation.distance(location);
                if (distance < maxRange)
                {
                    location.add(direction.clone().multiply(stepSize));
                    Block block = location.getBlock();
                    if (!block.isPassable())
                    {
                        flyingBends.get(playerUUID).cancel();
                        flyingBends.remove(playerUUID);
                    } else
                    {
                        Collection<Entity> entitiesHit = location.getWorld().getNearbyEntities(location, 1, 1, 1);
                        entitiesHit.remove(player);
                        if (!entitiesHit.isEmpty())
                        {
                            boolean targetHit = false;
                            for (Entity entity : entitiesHit)
                            {
                                if (entity instanceof LivingEntity)
                                {
                                    LivingEntity livingEntity = (LivingEntity)entity;
                                    targetHit = true;

                                    // do something to each entity
                                    livingEntity.setFireTicks(3 * 20);
                                    livingEntity.damage(6, player);
                                    entity.getWorld().spawnParticle(Particle.CRIT_MAGIC,
                                            entity.getLocation(), 1, 0, 0, 0, 0);
                                    flyingBends.get(playerUUID).cancel();
                                    flyingBends.remove(playerUUID);
                                    break;
                                }
                            }
                        }
                    }
                    player.getWorld().spawnParticle(Particle.FLAME, location, 2, 0, 0, 0, 0);
                    player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, location, 1, 0, 0, 0, 0);

                } else
                {
                    flyingBends.get(playerUUID).cancel();
                    flyingBends.remove(playerUUID);
                }
            } , 0, 1L);
            flyingBends.put(playerUUID, task1);
        }
    }
}


