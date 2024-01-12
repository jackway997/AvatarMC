package sprucegoose.avatarmc.abilities.water;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import sprucegoose.avatarmc.utils.EntityUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

// To do: cancel all player actions when frozen
// Note: all timing should be done in ticks

public class WaterEffects implements Listener
{
    // keys
    private final String chillTimeStampKey = "chill_time";
    private final String chillDurationKey = "chill_dur";

    private final String freezeTimeStampKey = "freeze_time";
    private final String freezeDurationKey = "freeze_dur";
    private final String freezeDamageKey = "freeze_dmg";

    private final double freezeDmgThreshold = 5.0;
    JavaPlugin plugin;

    HashSet<UUID> frozenPlayers = new HashSet<>();
    HashMap<UUID,Double> frozenMobs = new HashMap();

    public WaterEffects(JavaPlugin plugin)
    {
        this.plugin = plugin;
    }

    public void freezeEntity(LivingEntity entity, int tickDuration) // in ticks
    {
        unChillEntity(entity);
        if (!entityIsFrozen(entity))
        {
            // don't set potion effect if the entity is already frozen for a longer time period. - to do
            //if ((EntityUtil.getTimeElapsedEntityMeta(entity, freezeTimeStampKey) -
            //        Integer.ParseInt(EntityUtil.getEntityTag(entity, freezeDurationKey)) > duration)
            //    return;
            startFreezeAnimation(entity, tickDuration);
            EntityUtil.setTimeStampedEntityMeta(plugin, freezeTimeStampKey, entity);
            EntityUtil.setEntityMeta(plugin, entity, freezeDurationKey, String.valueOf(tickDuration));
            EntityUtil.setEntityMeta(plugin, entity, freezeDamageKey, String.valueOf(0));
            if (entity instanceof Player player)
            {
                freezePlayer(player, tickDuration);
            }
            else
            {
                freezeMob(entity, tickDuration);
            }
        }
    }

    private void freezePlayer(Player player, int tickDuration)
    {
        frozenPlayers.add(player.getUniqueId());

        BukkitRunnable unFreezeTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                unfreezeEntity(player);
            }
        };
        unFreezeTask.runTaskLater(plugin, tickDuration);
    }

    private void freezeMob(LivingEntity entity, int duration)
    {
        AttributeInstance attribute = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        // check if this causes null pointer exception
        frozenMobs.put(entity.getUniqueId(), attribute.getBaseValue());
        attribute.setBaseValue(0.0);

        BukkitRunnable unFreezeTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                unfreezeEntity(entity);
            }
        };
        unFreezeTask.runTaskLater(plugin, duration);
    }

    public void unfreezeEntity(LivingEntity entity)
    {
        if (entity instanceof Player player)
        {
            frozenPlayers.remove(player.getUniqueId());
        }
        else
        {
            if (frozenMobs.containsKey(entity.getUniqueId()))
            {
                AttributeInstance attribute = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                attribute.setBaseValue(frozenMobs.get(entity.getUniqueId()));
                frozenMobs.remove(entity.getUniqueId());
            }
        }
    }

    public boolean entityIsFrozen(LivingEntity entity)
    {
        if (!EntityUtil.entityHasMeta(entity, freezeDurationKey) || !EntityUtil.entityHasMeta(entity, freezeTimeStampKey))
        {
            return false;
        }

        try
        {
            UUID entityID = entity.getUniqueId();
            return (frozenMobs.containsKey(entityID) || frozenPlayers.contains(entityID)) &&
                    (EntityUtil.getTimeElapsedEntityMeta(entity, freezeTimeStampKey) * 20 <=
                    Integer.parseInt(EntityUtil.getEntityTag(entity,freezeDurationKey)));
        }
        catch (NumberFormatException e)
        {
            plugin.getLogger().warning("number format exception generated from entityIsFrozen");
            return false;
        }
    }

    public void chillEntity(LivingEntity entity, int duration)
    {
        // set metadata on the entity
        EntityUtil.setTimeStampedEntityMeta(plugin,chillTimeStampKey,entity);
        EntityUtil.setEntityMeta(plugin, entity, chillDurationKey, String.valueOf(duration));

        // don't set potion effect if the entity already has a slow from a longer time period.
        if (entity.hasPotionEffect(PotionEffectType.SLOW) && entity.getPotionEffect(PotionEffectType.SLOW).getDuration() > duration)
            return;

        entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, 0, false, false));
    }

    public void unChillEntity(LivingEntity entity)
    {
        if (EntityUtil.entityHasMeta(entity, chillDurationKey))
        {
            EntityUtil.setEntityMeta(plugin, entity, chillDurationKey, String.valueOf(0));
        }
        if (entity.hasPotionEffect(PotionEffectType.SLOW))
        {
            entity.removePotionEffect(PotionEffectType.SLOW);
        }
    }

    public boolean entityIsChilled(LivingEntity entity)
    {
        if (!EntityUtil.entityHasMeta(entity, chillDurationKey) || !EntityUtil.entityHasMeta(entity, chillTimeStampKey))
        {
            return false;
        }

        try
        {
            return EntityUtil.getTimeElapsedEntityMeta(entity, chillTimeStampKey) >
                    Integer.parseInt(EntityUtil.getEntityTag(entity,chillDurationKey));
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent e)
    {
        Player player = e.getPlayer();
        if (frozenPlayers.contains(player.getUniqueId()))
        {
            plugin.getLogger().info("player frozen: "+ entityIsFrozen(player));
            if (entityIsFrozen(player))
            {
                e.setCancelled(true);
            }
            else
            {
                unfreezeEntity(player);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e)
    {
        Player player = e.getPlayer();
        if (frozenPlayers.contains(player.getUniqueId()))
        {
            if (entityIsFrozen(player))
            {
                e.setCancelled(true);
            }
            else
            {
                unfreezeEntity(player);
            }
        }
    }

    // TO DO: add listener for player leave/death and cancel animation
    private void startFreezeAnimation(LivingEntity entity, int tickDuration)
    {
        final int maxSteps = 30;
        int [] step = {1};

        // variables
        // TO DO: get helix animation off internet
        BukkitRunnable animationTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (entityIsFrozen(entity))
                {
                    // do animation
                    step[0] = (step[0] + 1) % maxSteps;
                    spawnDoubleHelix(entity, step[0], maxSteps);
                }
                else
                {
                    this.cancel();
                    unfreezeEntity(entity);
                }
            }
        };
        animationTask.runTaskTimer(plugin, 0L, 1L);

        BukkitRunnable cancelTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (!animationTask.isCancelled())
                {
                    animationTask.cancel();
                }
            }
        };
        cancelTask.runTaskLater(plugin, tickDuration);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e)
    {
        Entity entity = e.getEntity();
        double damage = e.getDamage();

        if (frozenMobs.containsKey(entity.getUniqueId()) || frozenPlayers.contains(entity.getUniqueId()))
        {
            checkFreezeDamage((LivingEntity)entity, damage);
        }
    }

    private void checkFreezeDamage(LivingEntity entity, double damage)
    {
        try
        {
            double newDamage = Double.parseDouble(EntityUtil.getEntityTag(entity, freezeDamageKey)) + damage;
            System.out.println("damage: "+ newDamage);
            if (newDamage >= freezeDmgThreshold)
            {
                unfreezeEntity(entity);
            }
            else
            {
                EntityUtil.setEntityMeta(plugin, entity, freezeDamageKey, String.valueOf(newDamage));
            }
        }
        catch (NumberFormatException | NullPointerException exception)
        {
            System.out.println(exception.getStackTrace());
        }
    }

    public void spawnDoubleHelix(LivingEntity entity, int step, int totalSteps)
    {
        double minY = 0.0;
        double maxY = 2.0;
        double radius = 0.75;

        Location loc = entity.getLocation();

        double y = (maxY - minY)*step/totalSteps;

        double x1 = loc.getX() + radius * Math.cos(4*y);
        double z1 = loc.getZ() + radius * Math.sin(4*y);

        double x2 = loc.getX() + radius * -Math.cos(4*y);
        double z2 = loc.getZ() + radius * -Math.sin(4*y);

        y = y + loc.getY();

        Color color= Color.WHITE;
        Particle.DustOptions options = new Particle.DustOptions(color, 1);
        entity.getWorld().spawnParticle(Particle.REDSTONE,new Location(entity.getWorld(),x1,y,z1),1,options);
        entity.getWorld().spawnParticle(Particle.REDSTONE,new Location(entity.getWorld(),x2,y,z2),1,options);
    }

}
