package sprucegoose.avatarmc.abilities.water;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.ArrayList;
import java.util.Collection;

public class Heal extends Ability implements Listener
{

    public Heal(JavaPlugin plugin, RegionProtectionManager regProtMan)
    {
        super(plugin, regProtMan, ELEMENT_TYPE.water, ABILITY_LEVEL.adept);
        setCooldown(10000);
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
                abilityChecks(player, item) && !onCooldown(player) /*&&
                !regProtManager.isRegionProtected(player, e.getClickedBlock().getLocation(),this)*/
            )

        {
            if (player.isSneaking())
            {
                enchantAnimation(player);
                addCooldown(player, item);

            }
            else
            {
                if (getEntityHit(player))
                {
                    addCooldown(player, item);
                }
            }

            e.setCancelled(true);
        }
    }

    public void enchantAnimation(Player p)
    {
        p.setHealth(20);

        BukkitRunnable task4 = new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {


                Location center = p.getLocation();
                World world = p.getWorld();

                double radius = 1; // Define the radius of the helix
                int particleCount = 100; // Number of particles in the circle
                double waveFrequency = 0.5; // Adjust frequency of the sine wave
                double amplitude = 0.062; // Adjust the amplitude of the sine wave
                int iterations = 20; // Number of iterations


                if (step >= iterations) {
                    this.cancel();
                    return;
                }


                double angle = 2 * Math.PI * ((double) step / iterations);

                double x = center.getX() + (radius * Math.cos(angle));
                double y = center.getY() + 2;
                double z = center.getZ() + (radius * Math.sin(angle));

                Location particleLoc = new Location(world, x, y, z);
                world.spawnParticle(Particle.HEART, particleLoc, 1);

                step++; // Increment the step to move to the next particle position


            }
        };
        task4.runTaskTimer(plugin, 0L, 1L);
    }

    public boolean getEntityHit(Player p) {

        int iterations = 40;
        Location eyeLocation = p.getLocation();
        for (int i = 0; i < iterations; i++) {
            double x = i;

            Vector direction = eyeLocation.getDirection();
            Vector rayPos = direction.clone().multiply(x);

            Location rayLoc = new Location(
                    eyeLocation.getWorld(),
                    eyeLocation.getX() + rayPos.getX(),
                    eyeLocation.getY() + rayPos.getY() + 2,
                    eyeLocation.getZ() + rayPos.getZ()
            );

            Collection<Entity> nearbyEntities = rayLoc.getWorld().getNearbyEntities(rayLoc, 0.65, 1, 0.65);
            for (Entity entity : nearbyEntities) {
                if (entity != null && entity != p && entity instanceof Player pe && p.hasLineOfSight(entity)) {
                    pe.setHealth(20);
                    pe.sendMessage(ChatColor.WHITE + "" + p.getDisplayName() + ChatColor.GREEN + " has healed you");
                    enchantAnimation(pe);
                    return true;
                }


            }
        }
        return false;
    }





    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "heals ");
        lore.add(ChatColor.GRAY + "WITH LOVE");
        return getAbilityItem(plugin, player, lore, 2);
    }


}
