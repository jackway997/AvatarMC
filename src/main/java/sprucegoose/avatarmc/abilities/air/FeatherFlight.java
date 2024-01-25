package sprucegoose.avatarmc.abilities.air;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import org.bukkit.util.Vector;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.ImageParticles;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FeatherFlight extends Ability implements Listener {

    private long cooldown;
    private double jumpStrength;

    private Map<UUID, BukkitTask> activeAbilities = new HashMap<>();
    private Map<UUID, BukkitTask> groundAnimations = new HashMap<>();

    public FeatherFlight(JavaPlugin plugin, RegionProtectionManager regProtMan)
    {
        super(plugin, regProtMan, ELEMENT_TYPE.air, ABILITY_LEVEL.expert);
        setCooldown(cooldown * 1000);
    }

    @Override
    public void loadProperties()
    {
        this.cooldown = getConfig().getLong("Abilities.Air.FeatherFlight.Cooldown");
        this.jumpStrength = getConfig().getDouble("Abilities.Air.FeatherFlight.JumpStrength");
    }


    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e)
    {
        EquipmentSlot slot = e.getHand();
        ItemStack item = e.getItem();
        Player player = e.getPlayer();

        if (    slot != null && item != null &&
                e.getAction().equals(Action.RIGHT_CLICK_AIR) &&
                (slot.equals(EquipmentSlot.HAND) || slot.equals(EquipmentSlot.OFF_HAND)) &&
                AvatarIDs.itemStackHasAvatarID(plugin,item, this.getAbilityID()) &&
                PlayerIDs.itemStackHasPlayerID(plugin, item, player) && !onCooldown(player)
                && !activeAbilities.containsKey(player.getUniqueId())
        )
        {
            addCooldown(player, item);
            doAbility(plugin, e);
            e.setCancelled(true);
        }
    }

    private boolean doAbility(JavaPlugin plugin, PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Location location = player.getEyeLocation();

        if (location.getPitch() >= -10)
        {
            location.setPitch(-10);
        }
        Vector direction = location.getDirection().normalize();

        // play sound
        location.getWorld().playSound(location, Sound.BLOCK_FIRE_EXTINGUISH, 10f, 0.5f);

        // boost player
        player.setVelocity(direction.multiply(jumpStrength));

        // give slowfall
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 30, 0, false, false));

        // With BukkitScheduler
        BukkitScheduler scheduler = Bukkit.getScheduler();
        BukkitTask task = scheduler.runTaskTimer(plugin, () ->
        {
            if (player.getLocation().getBlock().getRelative(BlockFace.DOWN, 2).isEmpty()
               && player.getLocation().getBlock().getRelative(BlockFace.DOWN, 1).isEmpty()) {
                // create particle ring
                Location cloudLoc = player.getLocation();
                cloudLoc.setPitch(-90);
                ImageParticles.renderImage(plugin, cloudLoc, "graphics/cloud_ring.png");
            }
            else
            {
                activeAbilities.get(uuid).cancel();
                activeAbilities.remove(uuid);
                player.removePotionEffect(PotionEffectType.SLOW_FALLING);
            }
        }, 20L * 2L, 20L);
        activeAbilities.put(uuid, task);

        Location takeOffLoc = player.getLocation();
        takeOffLoc.setPitch(-90);
        BukkitTask task2 = scheduler.runTaskTimer(plugin, () ->
        {
            // create particle ring
            ImageParticles.renderImage(plugin, takeOffLoc, "graphics/cloud_ring.png");
        },  0,  2L);
        groundAnimations.put(uuid, task2);

        // With BukkitScheduler
        BukkitTask task3 = scheduler.runTaskLater(plugin, () ->
        {
            groundAnimations.get(uuid).cancel();
            groundAnimations.remove(uuid);

        }, 20L);
        return true;
    }

    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Whhheeeeeee!!!!");

        return getAbilityItem(plugin, player, lore, 3);
    }
}