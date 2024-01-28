
package sprucegoose.avatarmc.abilities.water;

import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.ArrayList;
import java.util.Collection;

public class Blizzard extends Ability implements Listener
{

    public Blizzard(JavaPlugin plugin, RegionProtectionManager regProtMan)
    {
        super(plugin, regProtMan, ELEMENT_TYPE.water, ABILITY_LEVEL.adept);
        setCooldown(20000);
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        EquipmentSlot slot = e.getHand();
        ItemStack item = e.getItem();
        Player player = e.getPlayer();

        if (slot != null && item != null &&
                (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) &&

                (slot.equals(EquipmentSlot.HAND) || slot.equals(EquipmentSlot.OFF_HAND)) &&
                abilityChecks(player, item) && !onCooldown(player) /*&&
                !regProtManager.isRegionProtected(player, e.getClickedBlock().getLocation(),this)*/
        ) {
            if (blizzard(player)) {
                addCooldown(player, item);


            }
            e.setCancelled(true);
        }
    }

    @Override
    public void doHostileAbilityAsMob(LivingEntity caster, LivingEntity target)
    {
        blizzard(caster);
    }




public boolean blizzard(LivingEntity caster) {

    if (caster.hasLineOfSight(caster.getLocation().add(0, 16, 0)) && areaEffect(caster))  {
        caster.sendMessage(ChatColor.BLUE + "You have cast Blizzard.");
        caster.setMetadata("blizzard", new FixedMetadataValue(plugin, true));
        cloudAnimation(caster);
        cloudDrops(caster);
        removeLightningImmunity(caster);


        return true;


    }
    return false;

}





    public boolean areaEffect(LivingEntity p)
    {
       Location startLoc  = p.getLocation().add(0, 15, 0);
       p.sendMessage("location acquired");
        if (
            //check if any points of corners of the area are in a protected zone like cloning area 5,0,5 etc.
                !regProtManager.isLocationPVPEnabled(p, startLoc.clone().add(5, 0, 5)) ||
                        !regProtManager.isLocationPVPEnabled(p, startLoc.clone().add(-5, 0, 5)) ||
                        !regProtManager.isLocationPVPEnabled(p, startLoc.clone().add(5, 0, -5)) ||
                        !regProtManager.isLocationPVPEnabled(p, startLoc.clone().add(-5, 0, -5))

        )
        {
            p.sendMessage(ChatColor.RED + "You cannot cast Blizzard here.");
            return false;
        }


        ArmorStand area = (ArmorStand) p.getWorld().spawnEntity(p.getLocation().add(0, 15, 0), EntityType.ARMOR_STAND);
        area.setCustomName("Area");
        area.setArms(false);
        area.setVisible(false);
        area.setGravity(false);
        area.setSmall(true);
        area.setMarker(true);
        area.setInvulnerable(true);





        BukkitRunnable damageTick = new BukkitRunnable() {
            @Override
            public void run() {
                if (!area.isDead()) {
                    for (Entity entity : area.getNearbyEntities(5, 15, 5)) {
                        if (entity instanceof LivingEntity le) {
                            if (le.hasLineOfSight(area)) {
                                if (!le.hasMetadata("blizzard")) {
                                    le.damage(5);
                                    regProtManager.tagEntity(le, p);
                                }
                            }
                        }
                    }
                }
            }
        };damageTick.runTaskTimer(plugin, 0L, 20L);

        BukkitRunnable removeArea = new BukkitRunnable() {
            @Override
            public void run() {
                if (!area.isDead()) {
                    area.remove();
                }
            }
        };removeArea.runTaskLater(plugin, 300L);

return true;
    }

    public void cloudAnimation(LivingEntity p) {
        //spawn a radius of particles in a circle 30 blocks above the player
        int maxCircles = 15; // Number of concentric circles
        int particleCount = 150; // Number of particles to create per circle
        int initialRadius = 5; // Initial radius
        int particleHeight = 15; // Height above the player to spawn particles
        double additionalLayers = 9; // Number of additional layers above inner circles


        for (int circle = 0; circle < maxCircles; circle++) {
            double radius = initialRadius - ((double) circle / 3); // Decrease radius for each circle

            // Calculate the angle between each particle
            double angleInterval = 2 * Math.PI / particleCount;

            // Spawn particles in a circle
            for (int i = 0; i < particleCount; i++) {
                double angle = i * angleInterval;
                double x = radius * Math.cos(angle);
                double z = radius * Math.sin(angle);

                // Vary the height of the particles
                double yOffset = Math.random() * 1; // Adjust this range as needed

                // Spawn the particle at an offset from the player's location
                p.getWorld().spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, p.getLocation().add(x, particleHeight + yOffset, z), 0);

                if (circle < additionalLayers) {
                    double aboveHeight = particleHeight + ((double) circle / 1.5); // Increase height for each layer
                    p.getWorld().spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, p.getLocation().add(x, aboveHeight, z), 0);
                }
            }
        }

        BukkitRunnable circleAnimation = new BukkitRunnable() {
            double angle = 0; // Initialize angle
            final double circleRadius = 5.2; // Adjust as needed
            final Location loc = p.getLocation().clone().add(0, 14, 0);

            @Override
            public void run() {


                // Calculate the position around the outer circle
                // double x = circleRadius * Math.cos(angle);
                // double z = circleRadius * Math.sin(angle);
                // p.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, p.getLocation().add(x, particleHeight, z), 0);
                double randomAngle = Math.random() * 2 * Math.PI;
                double randomRadius = Math.sqrt(Math.random()) * circleRadius; // Ensures randomness within the circle

                // Calculate the random position within the circle
                double x = randomRadius * Math.cos(randomAngle);
                double z = randomRadius * Math.sin(randomAngle);

                p.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, loc.clone().add(x, 0, z), 0);
                // Update angle for next position (adjust the speed as needed)
                // angle += Math.PI / 6; // Increase or decrease speed by changing the divisor

                // Reset angle when it completes a full circle
                // if (angle >= 2 * Math.PI) {
                //   angle = 0;
                // }
            }
        };
        circleAnimation.runTaskTimer(plugin, 0L, 3L);

        BukkitRunnable cloudCircleCancel = new BukkitRunnable() {
            @Override
            public void run() {
                circleAnimation.cancel();
            }
        };
        cloudCircleCancel.runTaskLater(plugin, 300L);
    }

    public void cloudDrops(LivingEntity p) {
        BukkitRunnable cloudDrops = new BukkitRunnable() {
            final Location loc = p.getLocation().clone().add(0, 0, 0);
            final double circleRadius = 4.2; // Adjust as needed


            @Override
            public void run() {


                double randomAngle = Math.random() * Math.PI * 2; // Random angle in radians
                double randomRadius = Math.sqrt(Math.random()) * circleRadius;

                double x = randomRadius * Math.cos(randomAngle);
                double z = randomRadius * Math.sin(randomAngle);

                double randomY = Math.random() * 15; // Random Y coordinate between 0 and 30
                Location newDropLoc = loc.clone().add(x, randomY, z); // Create drop location

                Location dropLoc = loc.clone().add(x, 30, z); // Create drop location

                // Spawn the raindrop particle at the drop location
                p.getWorld().spawnParticle(Particle.FALLING_WATER, dropLoc, 1);
                p.getWorld().spawnParticle(Particle.FALLING_WATER, dropLoc, 1);
                p.getWorld().spawnParticle(Particle.FALLING_WATER, dropLoc, 1);
                p.getWorld().spawnParticle(Particle.SNOWBALL, newDropLoc, 1);


            }
        };

        cloudDrops.runTaskTimer(plugin, 0L, 1L);

        BukkitRunnable cloudDropsCancel = new BukkitRunnable() {
            @Override
            public void run() {
                cloudDrops.cancel();
            }
        };
        cloudDropsCancel.runTaskLater(plugin, 300L);

        BukkitRunnable Lightning = new BukkitRunnable() {
            final Location loc = p.getLocation().clone().add(0, 0, 0);

            @Override
            public void run() {
                Entity lightning = LightningStrike.class.cast(p.getWorld().spawnEntity(loc, org.bukkit.entity.EntityType.LIGHTNING));
                lightning.setMetadata("customLightning", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
                lightningMetaDataRemover(lightning);


            }
        };
        int randomDelay = (int) (Math.random() * (200 - 20 + 1)) + 10;
        Lightning.runTaskLater(plugin, randomDelay);

    }
    public void lightningMetaDataRemover(Entity e) {
        BukkitRunnable lightningMetaDataRemover = new BukkitRunnable() {
            @Override
            public void run() {
                if (e.hasMetadata("customLightning")) {
                    e.removeMetadata("customLightning", plugin);
                }
            }
        };
        lightningMetaDataRemover.runTaskLater(plugin, 500L);
    }
    @EventHandler
    public void lightningListener(EntityDamageByEntityEvent e) {
        Entity entity = e.getDamager();
        Entity affected = e.getEntity();
        if (entity instanceof LightningStrike) {
            if (affected.hasMetadata("blizzard")) {
                e.setCancelled(true);
            } else {
                affected.setMetadata("struckByLightning", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
                removeLightningStun(affected);
                if (affected instanceof Player) {

                    affected.sendMessage(ChatColor.DARK_GREEN + "You have been stunned by lightning.");
                }

            }
        }
    }
    @EventHandler
    public void onEntityMove(EntityMoveEvent e) {
        Entity entity = e.getEntity();
        if (entity instanceof LivingEntity le) {
            if (entity.hasMetadata("blizzard")) {
                e.setCancelled(true);


            }
        }
    }
    @EventHandler
    public void onPlayerMove(EntityMoveEvent e){
        Entity f = e.getEntity();
        if (f.hasMetadata("struckByLightning")) {
            e.setCancelled(true);
            f.sendMessage(ChatColor.DARK_RED + "You cannot move as you have been stunned by lightning.");
        }
    }
    public void removeLightningStun(Entity e) {
        BukkitRunnable removeLightningStun = new BukkitRunnable() {
            @Override
            public void run() {
                if (e.hasMetadata("struckByLightning")) {
                    e.removeMetadata("struckByLightning", plugin);
                }
            }
        };
        removeLightningStun.runTaskLater(plugin, 60L);
    }
    @EventHandler
    public void combustListener(EntityCombustEvent e) {
        Entity entity =  e.getEntity();

        if (entity.hasMetadata("blizzard")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void blockCombustEvent(BlockIgniteEvent e) {
        if (e.getCause() == BlockIgniteEvent.IgniteCause.LIGHTNING) {
            if (e.getIgnitingEntity() instanceof LightningStrike) {
                Entity entity = e.getIgnitingEntity();
                if (entity.hasMetadata("customLightning")) {
                    e.setCancelled(true);
                }
            }
        }
    }


    @EventHandler
    public void onLightningCombust(LightningStrikeEvent e) {
        Entity entity = e.getLightning();
        if (entity.hasMetadata("customLightning")) {
            entity.setFireTicks(0);
        }

    }
    public void removeLightningImmunity(LivingEntity p) {
        BukkitRunnable removeLightningImmunity = new BukkitRunnable() {
            @Override
            public void run() {
                if (p.hasMetadata("blizzard")) {


                    p.removeMetadata("blizzard", plugin);
                }
            }
        };
        removeLightningImmunity.runTaskLater(plugin, 500L);
    }
    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Sad boy");
        lore.add(ChatColor.GRAY + "hours");
        return getAbilityItem(plugin, player, lore, 3);
    }


}
