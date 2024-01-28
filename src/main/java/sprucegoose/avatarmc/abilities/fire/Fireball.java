package sprucegoose.avatarmc.abilities.fire;

import org.bukkit.Bukkit;
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
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.*;

// bug fix needed - cancel the cancel task for the hand flame animation

public class Fireball extends Ability
{
    private Map<UUID, BukkitRunnable> activeBends = new HashMap<>();

    public Fireball(JavaPlugin plugin, RegionProtectionManager regProtManager)
    {
        super(plugin, regProtManager, ELEMENT_TYPE.fire, ABILITY_LEVEL.expert);
        setCooldown(5000);
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
                abilityChecks(player, item))
        {
            e.setCancelled(true);

            if (activeBends.containsKey(playerUUID))
            {
                launchFireBallAsPlayer(player);
            }
            else if (!onCooldown(player))
            {
                if (summonHandFlame(player))
                {
                    addCooldown(player, item);
                }

            }
        }
    }

    @Override
    public void doHostileAbilityAsMob(LivingEntity caster, LivingEntity target)
    {
        Vector direction = target.getLocation().clone().subtract(caster.getLocation().clone()).toVector().normalize();
        launchFireball(caster, direction);
    }

    public void launchFireBallAsPlayer(Player caster)
    {
        Vector direction = caster.getEyeLocation().getDirection().normalize();
        launchFireball(caster, direction);
    }

    public ItemStack getAbilityItem (JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Bring the oven");
        lore.add(ChatColor.GRAY + "to them.");
        return getAbilityItem(plugin, player, lore, 2);
    }

    public Location getFireLoc (LivingEntity caster)
    {
        double sideScaleFactor = 0.55;
        double fireDownScaleFactor = 0.25;

        Location fireLoc = caster.getEyeLocation();
        Vector sideWayOffset = caster.getLocation().getDirection().crossProduct(new Vector(0, 1, 0)).normalize().multiply(sideScaleFactor);
        Vector fireDownOffset = caster.getLocation().getDirection().crossProduct(sideWayOffset).normalize().multiply(fireDownScaleFactor);

        //Add offsets
        fireLoc.add(fireLoc.getDirection().multiply(1));
        fireLoc.add(sideWayOffset);
        fireLoc.add(fireDownOffset);

        return fireLoc;
    }

    private boolean summonHandFlame(Player player)
    {
        UUID playerUUID = player.getUniqueId();

        if ( !regProtManager.isLocationPVPEnabled(player, player.getLocation()))
        {
            return false;
        }

        // Schedule Task to animate flame
        BukkitRunnable handFlame = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Location fireLoc = getFireLoc(player);
                player.getWorld().spawnParticle(Particle.FLAME, fireLoc, 2, 0, 0, 0, 0);
                player.getWorld().spawnParticle(Particle.SMOKE_LARGE, fireLoc, 1, 0, 0, 0, 0);
            }
        };
        handFlame.runTaskTimer(plugin,0 ,1L);
        activeBends.put(playerUUID, handFlame);

        // Schedule task to cancel hand flame animation
        BukkitRunnable cancelTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (!handFlame.isCancelled())
                {
                    handFlame.cancel();
                    activeBends.remove(playerUUID);
                }
            }
        };
        cancelTask.runTaskLater(plugin,5L * 20L);
        return true;
    }

    private void launchFireball(LivingEntity caster, Vector direction)
    {
        UUID playerUUID = caster.getUniqueId();

        double maxRange = 50.0; // Adjust the maximum range as desired
        double stepSize = 1;
        Location location = getFireLoc(caster);
        Location originalLocation = location.clone();

        // Remove Static flame animation //////////

        if (activeBends.containsKey(playerUUID))
        {
            BukkitRunnable handFlame = activeBends.get(playerUUID);
            if (!handFlame.isCancelled())
            {
                handFlame.cancel();
            }
            activeBends.remove(playerUUID);
        }

        if ( !regProtManager.isLocationPVPEnabled(caster, caster.getLocation()))
        {
            return;
        }

        // Animate Directional Flame
        spawnFireball(caster, location, direction, stepSize);

        BukkitRunnable fireballTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                double distance = originalLocation.distance(location);
                if (distance < maxRange)
                {
                    location.add(direction.clone().multiply(stepSize));
                    Block block = location.getBlock();
                    if (!block.isPassable()) // if fireball hits something solid
                    {
                        this.cancel();
                        explodeFireball(location, caster, 3);

                    } else
                    {
                        // Check to collision with entity (radius of 1)
                        Collection<Entity> entitiesHit = location.getWorld().getNearbyEntities(location, 1, 1, 1);
                        entitiesHit.remove(caster);
                        if (!entitiesHit.isEmpty()) // if entity hit
                        {
                            this.cancel();
                            explodeFireball(location, caster, 3);
                        } else
                        {
                            caster.getWorld().spawnParticle(Particle.SMOKE_LARGE, location, 1, 0, 0, 0, 0);
                        }
                    }

                } else
                {
                    this.cancel();
                }
            }
        };
        fireballTask.runTaskTimer(plugin, 0, 1L);
    }

    public void explodeFireball(Location location, LivingEntity caster, int blastRadius)
    {
        location.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, location,
                1, 0, 0, 0, 0);

        // get all entities in blast radius
        Collection<Entity> entitiesHit = location.getWorld().getNearbyEntities(location, blastRadius, blastRadius, blastRadius);
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
                        regProtManager.tagEntity(livingEntity, caster);
                        livingEntity.setFireTicks(3 * 20);
                        livingEntity.damage(12, caster);
                    }
                }
            }
        }
    }

    private void spawnFireball(LivingEntity caster, Location location, Vector direction, double stepSize)
    {
        double spacing = 0.1;
        double start = -0.1;
        double end = 0.1;
        double x = direction.getX();
        double y = direction.getY();
        double z = direction.getZ();
        stepSize *= 1.6;

        for (double ii = start; ii <= end; ii+=spacing)
            for (double jj = start; jj <= end; jj+=spacing)
                for (double kk = start; kk <= end; kk+=spacing)
                {
                    caster.getWorld().spawnParticle(Particle.FLAME, location.clone().add(ii,jj,kk),0, x,y,z, stepSize);
                }
    }
}


