package sprucegoose.avatarmc.abilities.air;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
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
import org.bukkit.scheduler.BukkitRunnable;
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

    private Map<UUID, BukkitRunnable> activeAbilities = new HashMap<>();

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

    @Override
    public void doHostileAbilityAsMob(LivingEntity caster, LivingEntity target)
    {
        //Vector targetVector = (target.getLocation().clone().toVector().subtract(
        ///        caster.getLocation().clone().toVector())).normalize();
        //Location tempLoc = caster.getLocation().clone().setDirection(targetVector);
        Location loc = caster.getEyeLocation().clone();
        loc.setPitch(-25);
        //tempLoc.setPitch(-45);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                doAbility(caster, loc.getDirection().normalize());
            }
        };
        task.runTaskLater(plugin, 40L);
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
                abilityChecks(player, item) && !onCooldown(player) && !activeAbilities.containsKey(player.getUniqueId())
        )
        {
            addCooldown(player, item);
            doAbilityAsPlayer(e.getPlayer());
            e.setCancelled(true);
        }
    }

    private void doAbilityAsPlayer(Player player)
    {
        Location location = player.getEyeLocation();

        if (location.getPitch() >= -10)
        {
            location.setPitch(-10);
        }
        Vector direction = location.getDirection().normalize();

        doAbility(player, direction);
    }
    private boolean doAbility(LivingEntity caster, Vector direction)
    {
        UUID uuid = caster.getUniqueId();

        // play sound
        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 10f, 0.5f);

        // boost player
        caster.setVelocity(direction.multiply(jumpStrength));

        // give slowfall
        caster.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 30, 0, false, false));

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (caster != null && !caster.isDead())
                {
                    if (caster.getLocation().getBlock().getRelative(BlockFace.DOWN, 2).isEmpty()
                            && caster.getLocation().getBlock().getRelative(BlockFace.DOWN, 1).isEmpty()) {
                        // create particle ring
                        Location cloudLoc = caster.getLocation();
                        cloudLoc.setPitch(-90);
                        ImageParticles.renderImage(plugin, cloudLoc, "graphics/cloud_ring.png");
                    }
                    else
                    {
                        if (!this.isCancelled())
                        {
                            this.cancel();
                            activeAbilities.remove(uuid);
                        }
                        caster.removePotionEffect(PotionEffectType.SLOW_FALLING);
                    }
                }
                else
                {
                    if (!this.isCancelled())
                    {
                        this.cancel();
                        activeAbilities.remove(uuid);
                    }
                }
            }
        };
        activeAbilities.put(uuid, task);
        task.runTaskTimer(plugin, 20L * 2L, 20L);

        Location takeOffLoc = caster.getLocation();
        takeOffLoc.setPitch(-90);
        BukkitRunnable task2 = new BukkitRunnable() {
            @Override
            public void run() {
                // create particle ring
                if(caster != null && !caster.isDead()) {
                    ImageParticles.renderImage(plugin, takeOffLoc, "graphics/cloud_ring.png");
                }
                else
                {
                    if(!this.isCancelled())
                    {
                        this.cancel();
                    }
                }
            }
        };
        task2.runTaskTimer(plugin, 0L,2L);

        BukkitRunnable cancelGroundTask = new BukkitRunnable()
        {
            @Override
            public void run() {
                if (!task2.isCancelled())
                {
                    task2.cancel();
                }
            }
        };
        cancelGroundTask.runTaskLater(plugin, 20L);

        return true;
    }

    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Whhheeeeeee!!!!");

        return getAbilityItem(plugin, player, lore, 3);
    }
}