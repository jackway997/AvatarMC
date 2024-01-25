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

    private long cooldown;
    private double range;
    private double hitRadius;
    private double knockbackStrength;
    private double damage;

    public AirBlast(JavaPlugin plugin, RegionProtectionManager regProtManager)
    {
        super(plugin, regProtManager, ELEMENT_TYPE.air, ABILITY_LEVEL.adept);
        setCooldown(cooldown * 1000);
    }

    @Override
    public void loadProperties()
    {
        this.cooldown = getConfig().getLong("Abilities.Air.AirBlast.Cooldown");
        this.range = getConfig().getDouble("Abilities.Air.AirBlast.Range");
        this.hitRadius = getConfig().getDouble("Abilities.Air.AirBlast.HitRadius");
        this.knockbackStrength = getConfig().getDouble("Abilities.Air.AirBlast.KnockbackStrength");
        this.damage = getConfig().getDouble("Abilities.Air.AirBlast.Damage");
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
            if (doAbilityAsPlayer(player)) {
                addCooldown(player, item);
            }
            e.setCancelled(true);
        }
    }

    @Override
    public void doHostileAbilityAsMob(LivingEntity caster, LivingEntity target)
    {
        airBlast(caster, target);
    }

    public boolean doAbilityAsPlayer(Player player)
    {
        LivingEntity target = AbilityUtil.getHostileLOS(player,range, hitRadius);
        return airBlast(player, target);
    }

    private boolean airBlast(LivingEntity caster, LivingEntity target)
    {
        Location location = caster.getEyeLocation();
        Vector direction = location.getDirection().normalize();

        if (target == null || !regProtManager.isLocationPVPEnabled(caster, caster.getLocation()) ||
                        !regProtManager.isLocationPVPEnabled(caster, target.getLocation()))
        {
            return false;
        }

        caster.getWorld().playSound(caster.getLocation().add(direction), Sound.BLOCK_FIRE_EXTINGUISH, 10f, 0.7f);
        caster.getWorld().spawnParticle(Particle.SMOKE_NORMAL, caster.getLocation().add(direction).add(0,1,0), 25);

        target.damage(damage); // Adjust the damage as needed
        regProtManager.tagEntity(target, caster);

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
