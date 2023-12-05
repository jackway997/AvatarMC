package sprucegoose.avatarmc.abilities.fire;

import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.*;

public class MeteorStrike extends Ability implements Listener
{
    // To do: add particle circle on the ground while charging
    // cancel task and clear fallingSet on player leave game / die

    public enum CHARGE_STATE {stage1, stage2, stage3, stage4}


    public MeteorStrike(JavaPlugin plugin, RegionProtectionManager regProtMan)
    {

        super(plugin, regProtMan, ELEMENT_TYPE.fire, ABILITY_LEVEL.master);
        setCooldown(5000);
    }

    private Set<UUID> fallingSet = new HashSet<>();

    @EventHandler
    public void onPlayerInteractEvent(PlayerToggleSneakEvent e)
    {
        Player player = e.getPlayer();
        ItemStack mainItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();

        if (    ((mainItem != null && AvatarIDs.itemStackHasAvatarID(plugin,mainItem, this.getAbilityID()) &&
                    PlayerIDs.itemStackHasPlayerID(plugin, mainItem, player))
                ||
                (offHandItem != null && AvatarIDs.itemStackHasAvatarID(plugin, offHandItem, this.getAbilityID()) &&
                        PlayerIDs.itemStackHasPlayerID(plugin, offHandItem, player)))
                && !onCooldown(player) && e.isSneaking())
        {

            // put in arrays to wrap into objects
            CHARGE_STATE[] state = {CHARGE_STATE.stage1};
            int[] timer = {0};
            final int stage1 = 50, stage2 = 100, stage3 = 150;

            BukkitRunnable chargeUpTask = new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if (player.isSneaking()) // and on ground
                    {
                        timer[0] += 1;
                        if (timer[0] >= stage3)
                        {
                            state[0] = CHARGE_STATE.stage4;
                        } else if (timer[0] >= stage2)
                        {
                            state[0] = CHARGE_STATE.stage3;
                        } else if (timer[0] >= stage1)
                        {
                            state[0] = CHARGE_STATE.stage2;
                        }

                        animateCharge(player, state[0]);
                    }
                    else
                    {
                        if (state[0] != CHARGE_STATE.stage1)
                        {
                            launchMeteor(player, state[0]);
                        }                        // or do meteor strike
                        this.cancel();

                    }
                }
            };
            chargeUpTask.runTaskTimer(plugin, 0L, 2L);

//            addCooldown(player, item);
//            createSymbol(plugin, e);
//            e.setCancelled(true);
        }
    }

    private void animateCharge(Player player, CHARGE_STATE state)
    {
        double travelDistance = 3.0;
        double speed = 1.0;

        // generate a random direction
        Random r = new Random();
        double randomX = -1 + ((2) * r.nextDouble());
        double randomY = 1 * r.nextDouble();
        double randomZ = -1 + ((2) * r.nextDouble());

        // Calculate particle location and speed
        Vector particleDirection = new Vector(randomX, randomY, randomZ).normalize().multiply(travelDistance);
        Location startLoc = player.getLocation().clone().add(particleDirection);
        particleDirection = particleDirection.normalize().multiply(-1.0);
        double xSpeed = particleDirection.getX();
        double ySpeed = particleDirection.getY();
        double zSpeed = particleDirection.getZ();

        Particle particle;
        if (state == CHARGE_STATE.stage1)
        {
            particle = Particle.FLAME;
        }
        else
        {
            particle = Particle.SOUL_FIRE_FLAME;
        }

        player.getWorld().spawnParticle(particle, startLoc,0, xSpeed, ySpeed, zSpeed, speed);

    }

    private void launchMeteor(Player player, CHARGE_STATE state)
    {
        System.out.println("launching");
        Vector direction = new Vector(0,1,0).normalize();
        player.setVelocity(direction.multiply(4));
        fallingSet.add(player.getUniqueId());

        // start meteor tracking task
        BukkitRunnable meteorTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Vector velocity = player.getVelocity();

                if (velocity.getY() < 0 ) // when player is falling
                {

                    // boost players X and Z speed based on look direction
                    Location lookDirection = player.getEyeLocation().clone();
                    if (lookDirection.getPitch() <= -10) // prevent player from flying up
                    {
                            lookDirection.setPitch(-10);
                    }
                    Vector vectorDirection = lookDirection.getDirection().normalize().multiply(2);
                    vectorDirection.setY(velocity.getY());
                    player.setVelocity(vectorDirection);

                    // spawn particles
                    player.getWorld().spawnParticle(Particle.SMOKE_LARGE, player.getLocation(), 1, 0, 0, 0, 0);
                    player.getWorld().spawnParticle(Particle.SMOKE_LARGE, player.getLocation().clone().add(1,0,0), 1, 0, 0, 0, 0);
                    player.getWorld().spawnParticle(Particle.SMOKE_LARGE, player.getLocation().clone().add(-1,0,0), 1, 0, 0, 0, 0);
                    player.getWorld().spawnParticle(Particle.SMOKE_LARGE, player.getLocation().clone().add(0,0,1), 1, 0, 0, 0, 0);
                    player.getWorld().spawnParticle(Particle.SMOKE_LARGE, player.getLocation().clone().add(0,0,-1), 1, 0, 0, 0, 0);
                }
                if (!(player.getLocation().getBlock().getRelative(BlockFace.DOWN, 2).isEmpty()
                    && player.getLocation().getBlock().getRelative(BlockFace.DOWN, 1).isEmpty()))
                {
                    System.out.println("Cancelling");
                    this.cancel();
                    Location location = player.getLocation();
                    location.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, location,
                            1, 0, 0, 0, 0);

                    // get all entities in blast radius
                    double blastRadius = 5;
                    Collection<Entity> entitiesHit = location.getWorld().getNearbyEntities(location, blastRadius, blastRadius, blastRadius);
                    entitiesHit.remove(player);
                    if (!entitiesHit.isEmpty())
                    {
                        for (Entity entity : entitiesHit)
                        {
                            if (entity instanceof LivingEntity)
                            {
                                LivingEntity livingEntity = (LivingEntity)entity;

                                // do something to each entity
                                livingEntity.setFireTicks(3 * 20);
                                livingEntity.damage(15, player);
                            }
                        }
                    }
                }
            }
        };
        meteorTask.runTaskTimer(plugin, 20L, 1L);
    }

    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "I came in like a ");
        lore.add(ChatColor.GRAY + "wrecking ball.");
        return getAbilityItem(plugin, player, lore, 1);
    }

    @EventHandler
    public void onPlayerFall(EntityDamageEvent e) {
        Player player = (Player) e.getEntity();
        if (fallingSet.contains(player.getUniqueId()) && e.getCause() == EntityDamageEvent.DamageCause.FALL)
        {
            fallingSet.remove(player.getUniqueId());
            e.setCancelled(true);
        }
    }
}