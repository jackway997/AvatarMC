 package sprucegoose.avatarmc.abilities.earth;

 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.ArmorStand;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.EquipmentSlot;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.bukkit.scheduler.BukkitTask;
 import org.bukkit.util.EulerAngle;
 import sprucegoose.avatarmc.abilities.Ability;
 import sprucegoose.avatarmc.region.RegionProtectionManager;
 import sprucegoose.avatarmc.utils.AvatarIDs;
 import sprucegoose.avatarmc.utils.PlayerIDs;

 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;

 import static org.bukkit.Bukkit.getServer;

/*
Notes: bugs: when you toss the same block multiple times, sometimes the
 scheduler will remove a block from a previous toss
 */

 public class RockSolid extends Ability {


     public RockSolid(JavaPlugin plugin, RegionProtectionManager regProtManager) {
         super(plugin, regProtManager, ELEMENT_TYPE.earth, ABILITY_LEVEL.adept);
         setCooldown(3000);
     }
/*
     public void rockAura(Player p) {
         ArmorStand missile = (ArmorStand) p.getWorld().spawnEntity(p.getLocation().add(0, -3, 0), EntityType.ARMOR_STAND);

         missile.setArms(true);
         missile.setVisible(false);
         missile.setGravity(false);
         missile.setSmall(true);
         //makes collision box smaller
         missile.setMarker(true);
         missile.setItemInHand(new ItemStack(Material.STONE));
         //holding something at 90 degrees
         missile.setRightArmPose(new EulerAngle(Math.toRadians(90), Math.toRadians(0), Math.toRadians(0)));


         BukkitRunnable task1 = new BukkitRunnable() {
             final int distance = 500;
             int i = 0;
             double radius = 2.0;
             double speed = 0.2;
             double angle = 0.0;
             boolean isDead = false;

             @Override
             public void run() {
                 EulerAngle rot = missile.getRightArmPose();
                 EulerAngle rotnew = rot.add(0.1, 0.1, 0);
                 missile.setRightArmPose(rotnew);

                 double x = p.getLocation().getX() + (radius * Math.cos(angle));
                 double z = p.getLocation().getZ() + (radius * Math.sin(angle));

                 missile.teleport(new Location(p.getWorld(), x, p.getLocation().getY() + 0.5, z));
                 angle += speed;
                 if (angle >= 2 * Math.PI) {
                     angle = 0.0;
                 }
                 if (i > distance) {
                     if (!missile.isDead()) {
                         missile.remove();

                     }
                     cancel();
                     missile.remove();


                 }
                 i++;

                 if (!missile.isDead()) {
                     p.setMetadata("rockAura", new FixedMetadataValue(plugin, true));


                 }
                 if (missile.isDead() && p.hasMetadata("rockAura")) {
                     p.removeMetadata("rockAura", plugin);
                 }
                 if (!p.isOnline()) {
                     missile.remove();
                     p.removeMetadata("rockAura", plugin);
                     cancel();
                 }
             }


         };
         task1.runTaskTimer(plugin, 0L, 1L);

     }


     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent e) {
         Player player = e.getPlayer();
         EquipmentSlot slot = e.getHand();
         ItemStack item = e.getItem();
         ItemStack coal = new ItemStack(Material.COAL);

         // Check if the player is holding the Earth item
         if (slot != null && item != null &&
                 (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock() != null) &&
                 (slot.equals(EquipmentSlot.HAND) || slot.equals(EquipmentSlot.OFF_HAND)) &&
                 AvatarIDs.itemStackHasAvatarID(plugin, item, this.getAbilityID()) &&
                 PlayerIDs.itemStackHasPlayerID(plugin, item, player)) {
             rockAura(player);
         }
     }

     @EventHandler
     public void onEntityHit(EntityDamageByEntityEvent e) {
         Entity entity = e.getEntity();
         Player p = (Player) entity;


         if (p.hasMetadata("rockAura")) {
             p.sendMessage("rock aura registered");
             p.setVelocity(p.getVelocity().multiply(500));
             p.removeMetadata("rockAura", plugin);


         }
     }

*/
         public ItemStack getAbilityItem (JavaPlugin plugin, Player player)
         {
             ArrayList<String> lore = new ArrayList<String>();
             lore.add(ChatColor.GRAY + "That rock is");
             lore.add(ChatColor.GRAY + "looking at me funny");
             return getAbilityItem(plugin, player, lore, 2);
         }

 }
