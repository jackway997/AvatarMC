package sprucegoose.avatarmc.abilities.fire;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.*;

public class Firebolt extends Ability
{
    private final Map<UUID, BukkitRunnable> activeBends = new HashMap<>();
    private final Map<UUID, BukkitRunnable> cancelTasks = new HashMap<>();

    public Firebolt(JavaPlugin plugin, RegionProtectionManager regProtManager)
    {
        super(plugin, regProtManager, ELEMENT_TYPE.fire, ABILITY_LEVEL.adept);
        setCooldown(2000);
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
                PlayerIDs.itemStackHasPlayerID(plugin, item, player))
        {
            if (activeBends.containsKey(playerUUID))
            {
                launchFireBoltAsPlayer(player);
            }
            else if (!onCooldown(player))
            {
                if (summonHandFlame(player))
                {
                    addCooldown(player, item);
                }
            }
            e.setCancelled(true);
        }
    }

    @Override
    public void doHostileAbilityAsMob(LivingEntity caster, LivingEntity target)
    {
        Vector direction = target.getLocation().clone().subtract(caster.getLocation().clone()).toVector().normalize();
        launchFirebolt(caster, direction);
    }

    public void launchFireBoltAsPlayer(Player caster)
    {
        Vector direction = caster.getEyeLocation().getDirection().normalize();
        launchFirebolt(caster, direction);
    }

    private void launchFirebolt(LivingEntity caster, Vector direction)
    {
        UUID playerUUID = caster.getUniqueId();

        double maxRange = 50.0; // Adjust the maximum range as desired
        double stepSize = 1;
        Location location = getFireLoc(caster);
        Location originalLocation = location.clone();

        BukkitRunnable animationTask = activeBends.get(playerUUID);
        if (animationTask != null && !animationTask.isCancelled())
        {
            animationTask.cancel();
            activeBends.remove(playerUUID);
        }

        BukkitRunnable cancelTask = cancelTasks.get(playerUUID);
        if(cancelTask != null && !cancelTask.isCancelled())
        {
            cancelTask.cancel();
            cancelTasks.remove(playerUUID);
        }

        if (!regProtManager.isLocationPVPEnabled(caster, caster.getLocation()))
        {
            return;
        }

        BukkitRunnable scheduler = new BukkitRunnable() {
            @Override
            public void run() {
                double distance = originalLocation.distance(location);
                if (distance < maxRange)
                {
                    location.add(direction.clone().multiply(stepSize));
                    Block block = location.getBlock();
                    if (!block.isPassable())
                    {
                        this.cancel();
                    } else
                    {
                        Collection<Entity> entitiesHit = location.getWorld().getNearbyEntities(location, 1, 1, 1);
                        entitiesHit.remove(caster);
                        if (!entitiesHit.isEmpty())
                        {
                            for (Entity entity : entitiesHit)
                            {
                                if (entity instanceof LivingEntity)
                                {
                                    if (regProtManager.isLocationPVPEnabled(caster, entity.getLocation()))
                                    {
                                        LivingEntity livingEntity = (LivingEntity) entity;

                                        // do something to each entity
                                        livingEntity.setFireTicks(3 * 20);
                                        livingEntity.damage(6, caster);
                                        entity.getWorld().spawnParticle(Particle.CRIT_MAGIC,
                                                entity.getLocation(), 1, 0, 0, 0, 0);

                                        if (!this.isCancelled())
                                        {
                                            this.cancel();
                                        }
                                        return;
                                    }
                                    else
                                    {
                                        if (!this.isCancelled())
                                        {
                                            this.cancel();
                                        }
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    caster.getWorld().spawnParticle(Particle.FLAME, location, 2, 0, 0, 0, 0);
                    caster.getWorld().spawnParticle(Particle.SMOKE_NORMAL, location, 1, 0, 0, 0, 0);

                }
                else
                {
                    if (!this.isCancelled())
                    {
                        this.cancel();
                    }
                }

            }
        };
        scheduler.runTaskTimer(plugin, 0, 1L);
    }

    public ItemStack getAbilityItem (JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Practice makes perfect");
        return getAbilityItem(plugin, player, lore, 2);
    }


    public Location getFireLoc (LivingEntity player)
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

    private boolean summonHandFlame(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!regProtManager.isLocationPVPEnabled(player, player.getLocation())) {
            return false;
        }

        // Schedule Task to animate flame
        BukkitRunnable task1 = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Location fireLoc = getFireLoc(player);

                player.getWorld().spawnParticle(Particle.FLAME, fireLoc, 2, 0, 0, 0, 0);
                player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, fireLoc, 1, 0, 0, 0, 0);
            }
        };
        task1.runTaskTimer(plugin,0L, 1L);
        activeBends.put(playerUUID, task1);

        // With BukkitScheduler
        BukkitRunnable task2 = new BukkitRunnable() {
            @Override
            public void run() {
            if (!task1.isCancelled())
                {
                    task1.cancel();
                    activeBends.remove(playerUUID);
                    cancelTasks.remove(playerUUID);
                }
            }
        };
        task2.runTaskLater(plugin, 5L * 20L);
        cancelTasks.put(playerUUID, task2);
        return true;
    }
}


