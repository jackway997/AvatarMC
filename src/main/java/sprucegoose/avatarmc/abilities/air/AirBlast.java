package sprucegoose.avatarmc.abilities.air;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.abilities.AbilityUtil;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.ItemMetaTag;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.ArrayList;

public class AirBlast extends Ability implements Listener
{

    public AirBlast(JavaPlugin plugin, RegionProtectionManager regProtManager)
    {
        super(plugin, regProtManager, ELEMENT_TYPE.air, ABILITY_LEVEL.adept);
        setCooldown(3000);
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e)
    {
        EquipmentSlot slot = e.getHand();
        ItemStack item = e.getItem();
        Player player = e.getPlayer();
        Action action = e.getAction();

        if (    slot != null && item != null &&
                (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) &&
                (slot.equals(EquipmentSlot.HAND) || slot.equals(EquipmentSlot.OFF_HAND)) &&
                AvatarIDs.itemStackHasAvatarID(plugin,item, this.getAbilityID()) &&
                PlayerIDs.itemStackHasPlayerID(plugin, item, e.getPlayer()) && !onCooldown(player)
            )
        {
            if (airBlast(player)) {
                addCooldown(player, item);
            }
            e.setCancelled(true);
        }
    }

    private boolean airBlast(Player player)
    {
        double maxRange = 8.0; // Adjust the maximum range as desired
        double knockbackStrength = 3; // Adjust the knockback strength as needed
        double damage = 2;

        Location location = player.getEyeLocation();
        Vector direction = location.getDirection().normalize();

        double stepSize = 0.1;
        double distance = 0.0;

        LivingEntity target = AbilityUtil.getHostileLOS(player,8, 0.5);

        if (target == null || !regProtManager.isLocationPVPEnabled(player, player.getLocation()) ||
                        !regProtManager.isLocationPVPEnabled(player, target.getLocation()))
        {
            return false;
        }

        player.getWorld().playSound(player.getLocation().add(direction), Sound.BLOCK_FIRE_EXTINGUISH, 10f, 0.7f);
        player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, player.getLocation().add(direction).add(0,1,0), 25);

        target.damage(damage); // Adjust the damage as needed

        Location hitLocation = target.getLocation();
        hitLocation.getWorld().spawnParticle(Particle.SMOKE_NORMAL, hitLocation, 25);

        Vector knockbackDirection = hitLocation.toVector().subtract(location.toVector()).normalize();
        knockbackDirection.setY(0.15); // Adjust the vertical knockback as needed
        target.setVelocity(knockbackDirection.multiply(knockbackStrength));

        return true;
    }

    @Override
    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "yeet");
        return getAbilityItem(plugin, player, lore, 2);
    }
}
