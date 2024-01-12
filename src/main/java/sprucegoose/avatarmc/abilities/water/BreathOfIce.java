package sprucegoose.avatarmc.abilities.water;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.*;


// To do: spiders do not freeze

public class BreathOfIce extends WaterAbility
{
    public BreathOfIce(JavaPlugin plugin, RegionProtectionManager regProtManager, WaterEffects waterEffects)
    {
        super(plugin, regProtManager, waterEffects, ABILITY_LEVEL.expert);
        setCooldown(5000);
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent e)
    {
        Player player = e.getPlayer();
        ItemStack mainItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();

        if (    AvatarIDs.itemStackHasAvatarID(plugin,mainItem, this.getAbilityID()) &&
                PlayerIDs.itemStackHasPlayerID(plugin, mainItem, player) && !onCooldown(player) && e.isSneaking())
        {
            if (!onCooldown(player))
            {
                addCooldown(player, mainItem);
                doAbility(player);
            }
        }
        else if (AvatarIDs.itemStackHasAvatarID(plugin, offHandItem, this.getAbilityID()) &&
                        PlayerIDs.itemStackHasPlayerID(plugin, offHandItem, player) && !onCooldown(player) && e.isSneaking())

        {
            if (!onCooldown(player))
            {
                addCooldown(player, offHandItem);
                doAbility(player);
            }
        }
    }

    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Chill out bro.");
        return getAbilityItem(plugin, player, lore, 2);
    }


    @Override
    public void doHostileAbilityAsMob(LivingEntity caster, LivingEntity target)
    {
        doAbility(caster);
    }

    private void doAbility(LivingEntity caster)
    {
        // Constants
        final double range = 7;
        final double spreadAngle = 0.45;
        final float angleSpreadDeg = (float) (spreadAngle * 180 / Math.PI);
        final int chillDuration = 3;
        final long breathDuration = 5L * 20L;
        final long freezeTime = 2L * 20;
        final long taskPeriod = 2L;
        final long freezeTicks = freezeTime / taskPeriod;
        final double damagePerTick = 1;

        // clear tagged entites map for player
        //taggedEntities.put(player.getUniqueId(), new HashSet<>());
        final HashMap<UUID, Integer> taggedEntityList = new HashMap<>();
        final HashSet<UUID> lastScan = new HashSet<>();
        final HashSet<UUID> thisScan = new HashSet<>();


        BukkitRunnable breathTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if ((caster instanceof Player player && !player.isSneaking()) ||
                        !regProtManager.isLocationPVPEnabled(caster, caster.getLocation()))
                {
                    if(!this.isCancelled())
                    {
                        this.cancel();
                    }
                    return;
                }

                Location playerLoc = caster.getEyeLocation();
                //get all mobs in radius
                Collection<Entity> nearbyEntities = playerLoc.getWorld().getNearbyEntities(playerLoc, range, range, range);
                nearbyEntities.remove(caster);

                // assign thisScan to lastScan and clear lastScan
                lastScan.clear();
                lastScan.addAll(thisScan);
                thisScan.clear();

                // for each nearby entity, calculate angle deviation
                for (Entity entity : nearbyEntities)
                {
                    if (entity instanceof LivingEntity lEntity )
                    {
                        if (!regProtManager.isLocationPVPEnabled(caster, entity.getLocation()))
                        {
                            if (!this.isCancelled())
                            {
                                this.cancel();
                            }
                            return;
                        }

                        UUID entityID = lEntity.getUniqueId();
                        thisScan.add(entityID);

                        // make the angle more forgiving when the target is close to the player
                        double distance = caster.getLocation().distance(lEntity.getLocation());
                        double effectiveSpreadAngle;
                        if (distance < 3.0 )
                            effectiveSpreadAngle = ((spreadAngle - (Math.PI/2.0)) * distance/3.0) + (Math.PI/2.0);
                        else
                            effectiveSpreadAngle = spreadAngle;

                        //plugin.getLogger().info("entity in spread ? "+ entityInSpread(caster, lEntity, effectiveSpreadAngle));

                        // Only affect entities within the breath attack
                        if (entityInSpread(caster, lEntity, effectiveSpreadAngle)) // TO DO: and check line of sight
                        {
                            regProtManager.tagEntity(lEntity, caster);
                            if (!taggedEntityList.containsKey(entityID) || !lastScan.contains(entityID)) // TO DO: or is frozen
                            {

                                taggedEntityList.put(entityID, 0); // reset ticks if not caught in breath
                            } else
                            {
                                taggedEntityList.put(entity.getUniqueId(), taggedEntityList.get(entityID) + 1);
                            }
                            if (!waterEffects.entityIsFrozen(lEntity))
                            {
                                lEntity.damage(damagePerTick);
                            }

                            waterEffects.chillEntity(lEntity, chillDuration);

                            // if entity
                            if (taggedEntityList.get(entityID) >= freezeTicks)
                            {
                                waterEffects.freezeEntity(lEntity, 5 * 20);
                                taggedEntityList.put(entityID, 0);
                            }
                        }
                    }
                }

                // Do breath animation
                generateFrostParticle(caster, 0, 0, 1);
                generateFrostParticle(caster, 0, -angleSpreadDeg, 1);
                generateFrostParticle(caster, 0, angleSpreadDeg, 1);
                generateFrostParticle(caster, angleSpreadDeg, 0, 1);
                generateFrostParticle(caster, -angleSpreadDeg, 0, 1);

            }
        };
        breathTask.runTaskTimer(plugin, 0L, taskPeriod);

        BukkitRunnable cancelTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (!breathTask.isCancelled())
                {
                    breathTask.cancel();
                }
            }
        };
        cancelTask.runTaskLater(plugin, breathDuration);
    }


    private void generateFrostParticle(LivingEntity caster, float pitch, float yaw, double speed)
    {
        Location playerLoc = caster.getEyeLocation();
        Location animationLocation = playerLoc.clone();
        animationLocation.setYaw(animationLocation.getYaw() + yaw);
        animationLocation.setPitch(animationLocation.getPitch() + pitch);
        animationLocation.add(animationLocation.getDirection().normalize());
        Vector animationVector2 = animationLocation.getDirection().normalize();

        caster.getWorld().spawnParticle(
                Particle.SPIT,
                animationLocation,
                0,
                animationVector2.getX(),
                animationVector2.getY(),
                animationVector2.getZ(),
                speed);
    }

    private boolean entityInSpread(LivingEntity caster, LivingEntity entity, double spreadAngle)
    {
        // get angle representing how accurately player is looking at entity
        Location target = entity.getLocation();
        Location source = caster.getEyeLocation();
        Vector inBetween = target.clone().subtract(source).toVector();
        Vector forward = source.getDirection();
        double angle = forward.angle(inBetween);
        System.out.println("angle: ["+ angle +"/"+spreadAngle+"]");
        System.out.println("satisfied: "+ (angle < spreadAngle));
        // TO DO: and check line of sight
        return angle < spreadAngle;
    }
}


