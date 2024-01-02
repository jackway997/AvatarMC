package sprucegoose.avatarmc.abilities.air;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.*;

public class Cyclone extends Ability implements Listener {

    public Cyclone(JavaPlugin plugin, RegionProtectionManager regProtManager) {
        super(plugin, regProtManager, ELEMENT_TYPE.air, ABILITY_LEVEL.master);
        setCooldown(10000);
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        EquipmentSlot slot = e.getHand();
        ItemStack item = e.getItem();
        Player player = e.getPlayer();
        Action action = e.getAction();

        if (slot != null && item != null &&
                (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) &&
                (slot.equals(EquipmentSlot.HAND) || slot.equals(EquipmentSlot.OFF_HAND)) &&
                AvatarIDs.itemStackHasAvatarID(plugin, item, this.getAbilityID()) &&
                PlayerIDs.itemStackHasPlayerID(plugin, item, e.getPlayer()) && !onCooldown(player)
        ) {
            addCooldown(player, item);
            Missile missile = new Missile(player);
            e.setCancelled(true);
            BukkitRunnable task1 = new BukkitRunnable() {

                @Override
                public void run() {


                    missile.spawnMissile();

                }
            };
            task1.runTaskTimer(plugin, 0L, 3);

            BukkitRunnable task2 = new BukkitRunnable() {
                @Override
                public void run() {


                    task1.cancel();
                }
            };
            task2.runTaskLater(plugin, 31);

            BukkitRunnable task3 = new BukkitRunnable() {
                @Override
                public void run() {

                    missile.removeMissle();
                }
            };
            task3.runTaskLater(plugin, 400);
        }

    }




    public class Missile {
        private Set<ArmorStand> missileSet;

        private Player p;


        public void spawnMissile() {

            Material[] materials = {Material.FEATHER, Material.STRING, Material.IRON_NUGGET, Material.GRASS};
            ArmorStand missile = (ArmorStand) p.getWorld().spawnEntity(p.getLocation().add(0, -3, 0), EntityType.ARMOR_STAND);

            missile.setCustomName("Missile");
            missile.setArms(true);
            missile.setVisible(false);
            missile.setGravity(false);
            missile.setSmall(true);
            missile.setMarker(true);
            Random random = new Random();
            int randomIndex = random.nextInt(materials.length);
            Material selectedMaterial = materials[randomIndex];
            missile.setItemInHand(new ItemStack(selectedMaterial));
            missile.setRightArmPose(new EulerAngle(Math.toRadians(90), Math.toRadians(0), Math.toRadians(0)));

            missileSet.add(missile);

            BukkitRunnable task4 = new BukkitRunnable() {

                double radius = 5; // Set your desired radius for the circle
                double speed = 0.4; // Adjust the speed of circling as needed
                double angle = 0.0; // Initialize the angle
                Random random = new Random();

                double randomY = -1 + (random.nextDouble() * 4); // Generate random Y between -1 and 1


                @Override
                public void run() {
                    EulerAngle rot = missile.getRightArmPose();
                    EulerAngle rotnew = rot.add(0.1, 0.1, 0);
                    missile.setRightArmPose(rotnew);
                    double x = p.getLocation().getX() + (radius * Math.cos(angle));
                    double z = p.getLocation().getZ() + (radius * Math.sin(angle));

                    missile.teleport(new Location(p.getWorld(), x, p.getLocation().getY() + randomY, z));
                    angle += speed;
                    if (angle >= 2 * Math.PI) {
                        angle = 0.0;
                    }
                    if (!missile.isDead() && p.isOnline()) {
                        Collection<Entity> nearbyEntities = p.getWorld().getNearbyEntities(p.getLocation(), 5.5, 2, 5.5);
                        for (Entity entity : nearbyEntities) {
                            if (entity != null && entity != p && entity instanceof LivingEntity le && p.hasLineOfSight(entity)) {
                                le.damage(3);
                                le.setVelocity(p.getLocation().getDirection().multiply(1));
                            }
                        }
                    }

                    if (!p.isOnline()) {
                        removeMissle();
                    }


                }
            };
            task4.runTaskTimer(plugin, 0L, 1);


        }

        public Missile(Player p) {
            this.p = p;
            this.missileSet = new HashSet<>();
            // this passes the player from the listener class to our private variable Player which we can use in line 18


        }

        public void removeMissle() {
            for (ArmorStand m : missileSet) {
                m.remove();
            }
        }

    }
    @Override
    public ItemStack getAbilityItem(JavaPlugin plugin, Player player) {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Glorified Panic Button");
        return getAbilityItem(plugin, player, lore, 2);
    }
}




