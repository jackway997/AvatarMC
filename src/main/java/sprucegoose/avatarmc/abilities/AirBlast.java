package sprucegoose.avatarmc.abilities;

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
import sprucegoose.avatarmc.Ability;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.ArrayList;

public class AirBlast extends Ability implements Listener
{


    public AirBlast(JavaPlugin plugin)
    {
        super(plugin);
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
                AvatarIDs.itemStackHasAvatarID(plugin,item, AirBlast.class.getSimpleName()) &&
                PlayerIDs.itemStackHasPlayerID(plugin, item, e.getPlayer()) && !onCooldown(player)
            )
        {
            addCooldown(player, item);
            airBlast(player);
            e.setCancelled(true);
        }
    }

    private static void airBlast(Player player) {

        double maxRange = 300.0; // Adjust the maximum range as desired
        double knockbackStrength = 3; // Adjust the knockback strength as needed
        double damage = 1;

        Location location = player.getEyeLocation();
        Vector direction = location.getDirection().normalize();

        double stepSize = 0.1;
        double distance = 0.0;

        while (distance < maxRange) {
            Location stepLocation = location.clone().add(direction.clone().multiply(distance));
            Block block = stepLocation.getBlock();

            if (block.getType().isSolid()) {
                break;
            }

            RayTraceResult result = player.getWorld().rayTraceEntities(location, direction, distance, e -> e instanceof LivingEntity && e != player);
            if (result != null) {
                Entity hitEntity = result.getHitEntity();
                if (hitEntity instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity) hitEntity;
                    livingEntity.damage(damage); // Adjust the damage as needed

                    Location hitLocation = livingEntity.getLocation();
                    hitLocation.getWorld().spawnParticle(Particle.SMOKE_NORMAL, hitLocation, 1);
                    hitLocation.getWorld().playSound(hitLocation, Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.0f);

                    Vector knockbackDirection = hitLocation.toVector().subtract(location.toVector()).normalize();
                    knockbackDirection.setY(0.15); // Adjust the vertical knockback as needed
                    livingEntity.setVelocity(knockbackDirection.multiply(knockbackStrength));
                }
            }

            distance += stepSize;
        }
    }

    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        ItemStack skill = new ItemStack(Material.BLAZE_ROD, 1);

        ItemMeta skill_meta = skill.getItemMeta();
        skill_meta.setDisplayName(ChatColor.GRAY + "" + ChatColor.BOLD + "Air Blast");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "(Soulbound)");
        lore.add(ChatColor.GRAY + "yeet");
        skill_meta.setLore(lore);
        skill.setItemMeta(skill_meta);

        AvatarIDs.setItemStackAvatarID(plugin, skill, AirBlast.class.getSimpleName());
        PlayerIDs.setItemStackPlayerID(plugin, skill, player);

        return skill;
    }
}
