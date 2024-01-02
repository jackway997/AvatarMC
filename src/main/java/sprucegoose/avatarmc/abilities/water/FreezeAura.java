package sprucegoose.avatarmc.abilities.water;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
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

public class FreezeAura extends Ability implements Listener
{

    public FreezeAura(JavaPlugin plugin, RegionProtectionManager regProtMan)
    {
        super(plugin, regProtMan, ELEMENT_TYPE.water, ABILITY_LEVEL.adept);
        setCooldown(20000);
    }

    @EventHandler
    public boolean onPlayerInteractEvent(PlayerInteractEvent e)
    {
        EquipmentSlot slot = e.getHand();
        ItemStack item = e.getItem();
        Player player = e.getPlayer();

        if (    slot != null && item != null &&
                (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) &&

                (slot.equals(EquipmentSlot.HAND) || slot.equals(EquipmentSlot.OFF_HAND)) &&
                AvatarIDs.itemStackHasAvatarID(plugin,item, this.getAbilityID()) &&
                PlayerIDs.itemStackHasPlayerID(plugin, item, player) && !onCooldown(player) /*&&
                !regProtManager.isRegionProtected(player, e.getClickedBlock().getLocation(),this)*/
            )
    {
        addCooldown(player, item);


            ArmorStand missile = (ArmorStand) player.getWorld().spawnEntity(player.getLocation().add(0, -3, 0), EntityType.ARMOR_STAND);
        missile.setArms(true);
        missile.setVisible(false);
        missile.setGravity(false);
        missile.setSmall(true);
        missile.setMarker(true);
        missile.setItemInHand(new ItemStack(Material.LAPIS_LAZULI));
        missile.setRightArmPose(new EulerAngle(Math.toRadians(90), Math.toRadians(0), Math.toRadians(0)));

        new BukkitRunnable() {





            //this is max distance the missile can travel before it is removed
            final int distance = 500;
            //acts as a timer and is checked against distance to see if the missile has travelled the max distance
            int i = 0;

            double radius = 2.0; // Set your desired radius for the circle
            double speed = 0.2; // Adjust the speed of circling as needed
            double angle = 0.0; // Initialize the angle

            public void run() {
                EulerAngle rot = missile.getRightArmPose();
                EulerAngle rotnew = rot.add(0.1, 0.1, 0);
                missile.setRightArmPose(rotnew);

                double x = player.getLocation().getX() + (radius * Math.cos(angle));
                double z = player.getLocation().getZ() + (radius * Math.sin(angle));

                missile.teleport(new Location(player.getWorld(), x, player.getLocation().getY() + 0.5, z));

                angle += speed;
                if (angle >= 2 * Math.PI) {
                    angle = 0.0;
                }
                for (Entity entity : missile.getLocation().getChunk().getEntities()) {
                    if (!missile.isDead()) {
                        //radius of the aura is now from the player NOT the missile
                        if (player.getLocation().distanceSquared(entity.getLocation()) <= 200) {
                            if (entity != player && entity != missile) {
                                //this is an easy way of assigning Living entity, it uses java 16 (Living Entity le) casts it to a living entity
                                if (entity instanceof LivingEntity le) {
                                    PotionEffect slowness = new PotionEffect(org.bukkit.potion.PotionEffectType.SLOW, 40, 1);
                                    le.addPotionEffect(slowness);

                                }
                            }
                        }
                    }
                }
                if (i > distance) {
                    if (!missile.isDead()) {
                        missile.remove();
                    }
                    cancel();
                }
                i++;
            }

            }.runTaskTimer(plugin, 0L, 1L);

            e.setCancelled(true);

    }
        return true;
}










    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "you become really ");
        lore.add(ChatColor.GRAY + "CHILL, bro");
        return getAbilityItem(plugin, player, lore, 3);
    }


}
