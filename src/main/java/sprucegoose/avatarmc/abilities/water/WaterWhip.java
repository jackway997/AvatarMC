package sprucegoose.avatarmc.abilities.water;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.ArrayList;
import java.util.Collection;

public class WaterWhip extends Ability implements Listener
{

    public WaterWhip(JavaPlugin plugin, RegionProtectionManager regProtMan)
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
                AvatarIDs.itemStackHasAvatarID(plugin, item, this.getAbilityID()) &&
                PlayerIDs.itemStackHasPlayerID(plugin, item, player) && !onCooldown(player) /*&&
                !regProtManager.isRegionProtected(player, e.getClickedBlock().getLocation(),this)*/
        ) {
            addCooldown(player, item);
            waterWhipAsPlayer(player);
            e.setCancelled(true);


        }
    }

    @Override
    public void doHostileAbilityAsMob(LivingEntity caster, LivingEntity target)
    {
        Vector direction = target.getLocation().clone().subtract(caster.getLocation().clone()).toVector().normalize();
        waterWhip(caster, direction);
    }

    public void waterWhipAsPlayer(Player caster)
    {
        Vector direction = caster.getEyeLocation().getDirection().normalize();
        waterWhip(caster, direction);
    }


    public boolean waterWhip(LivingEntity caster, Vector direction) { //player p comes from the interact event player

            plugin.getLogger().info("water whip called");


                Location eyeLocation = caster.getLocation();
                Location playerLocation = getFireLoc(caster);


                Particle particleType = Particle.WATER_DROP;
                int particleCount = 30;
                double offsetX = 0.1;
                double offsetY = 0.1;
                double offsetZ = 0.1;
                double speed = 0.05;
                int iterations = 30;

                int delayBetweenParticles = 0; // Delay between each particle spawn (ticks)
                int waveFrequency = 3; // Adjust frequency of the sine wave
                int amplitude = 3; // Adjust the amplitude of the sine wave

                final int maxDistance = 1;
                int t = 0;

                for (int i = 0; i < iterations; i++) {
                    //Calculating Particle location
                    double x = i;
                    double y = amplitude * Math.sin(waveFrequency * i); // Calculate the sine wave pattern

                    if (y >= 0) {
                        Vector particlePos = direction.clone().multiply(x).add(new Vector(0, y - 1, 0));

                        //calculate particle location
                        Location particleloc = new Location(
                                playerLocation.getWorld(),
                                playerLocation.getX() + particlePos.getX(),
                                playerLocation.getY() + particlePos.getY(),
                                playerLocation.getZ() + particlePos.getZ()
                        );


                        //Calculate Ray trace location
                        Vector rayPos = direction.clone().multiply(x);


                        Location rayLoc = new Location(
                                eyeLocation.getWorld(),
                                eyeLocation.getX() + rayPos.getX(),
                                eyeLocation.getY() + rayPos.getY() + 2,
                                eyeLocation.getZ() + rayPos.getZ()
                        );

                        //spawning the particles
                        playerLocation.getWorld().spawnParticle(
                                particleType,
                                particleloc,
                                particleCount,
                                offsetX,
                                offsetY,
                                offsetZ,
                                speed
                        );
                        Block block = rayLoc.getBlock();
                        if (block.getType().isSolid()) {

                            return false;
                        }
                        Collection<Entity> nearbyEntities = rayLoc.getWorld().getNearbyEntities(rayLoc, 0.65, 1, 0.65);

                        for (Entity entity : nearbyEntities) {
                            if (entity != null && entity != caster && entity instanceof LivingEntity le && caster.hasLineOfSight(entity)) {


                                le.sendMessage("you have been hit by a water whip");
                                caster.sendMessage("you have hit " + le);
                                le.damage(10);
                                le.setMetadata("entityHit", new FixedMetadataValue(plugin, true));
                                //delete the last particle and somehow cancel the particles from spawning
                                return true;

                            }

                        }
                    }


                }
               return false;
            }





    public Location getFireLoc(LivingEntity player) {
        double sideScaleFactor = 0.55;
        double fireDownScaleFactor = 0.25;

        Location fireLoc = player.getEyeLocation();
        Vector sideWayOffset = player.getLocation().getDirection().crossProduct(new Vector(0, 1, 0)).normalize().multiply(sideScaleFactor);
        Vector fireDownOffset = player.getLocation().getDirection().crossProduct(sideWayOffset).normalize().multiply(fireDownScaleFactor);

        //Add offsets
        fireLoc.add(fireLoc.getDirection().multiply(1));
        fireLoc.add(sideWayOffset);
        fireLoc.add(fireDownOffset);

        return fireLoc;
    }

    @EventHandler
    public void onDamageEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager().hasMetadata("entityHit")) {
            e.getDamager().sendMessage("you cannot attack as you have been cursed");
            e.setCancelled(true);
            curseTimer(e.getDamager());


        }
    }

    public void curseTimer(Entity ent) {
        BukkitRunnable task2 = new BukkitRunnable() {
            @Override
            public void run() {
                ent.removeMetadata("entityHit", plugin);



            }

        };task2.runTaskLater(plugin, 40L);

    }







    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Bit kinky");
        lore.add(ChatColor.GRAY + "for a water bender");
        return getAbilityItem(plugin, player, lore, 3);
    }


}
