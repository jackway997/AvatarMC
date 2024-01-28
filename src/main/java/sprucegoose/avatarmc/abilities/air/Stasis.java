package sprucegoose.avatarmc.abilities.air;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.abilities.AbilityUtil;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.EntityUtil;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;


// Notes:
// target is captured in a particle sphere, fixed in the air, connected by a wavering beam of particles

// players look direction + the fixed distance the target was away when casted defines the target
// destination each tick.

// The target has a maximum movement speed per tick so they don't "Teleport"
// if the player tries to move the target into a solid block, maintain the targets previous position until
// a valid position is moved to by the caster.
// if released before the end of timer, they stay suspended in position until the timer ends.

// Consider making damage to target cancel the effect (or a threshold similar to freeze)


public class Stasis extends Ability implements Listener
{
    private long cooldown; //7
    private double maxTravelStep; // 0.3
    private long duration; //5L * 20L
    private double range; // 10
    private double hitRadius; // 0.5

    public Stasis(JavaPlugin plugin, RegionProtectionManager regProtManager)
    {
        super(plugin, regProtManager, ELEMENT_TYPE.air, ABILITY_LEVEL.master);
        setCooldown(cooldown * 1000);
    }

    @Override
    public void loadProperties()
    {
        this.cooldown = getConfig().getLong("Abilities.Air.Stasis.Cooldown");
        this.maxTravelStep = getConfig().getDouble("Abilities.Air.Stasis.MaxTravelStep");
        this.duration = getConfig().getLong("Abilities.Air.Stasis.Duration");
        this.range = getConfig().getDouble("Abilities.Air.Stasis.Range");
        this.hitRadius = getConfig().getDouble("Abilities.Air.Stasis.HitRadius");
    }

    private final String activeCastKey = "CastStasisKey";

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
                abilityChecks(player, item)
            )
        {
            // update the timestamp that the player last registered a right click event.
            EntityUtil.setTimeStampedEntityMeta(plugin, activeCastKey, player);
            e.setCancelled(true);

            if (!onCooldown(player))
            {
                if (doAbility(player))
                {
                    addCooldown(player, item);
                }
            }
        }
    }

    private boolean doAbility(Player player)
    {
        // Constants
        final double interactEventTimeWindow = 1; // determine what this number needs to be

        LivingEntity target = AbilityUtil.getHostileLOS(player, range, hitRadius);

        // don't execute ability if either player is in a PVP disabled zone, or no target is found.
        if (target == null || !regProtManager.isLocationPVPEnabled(player, player.getLocation()) ||
                        !regProtManager.isLocationPVPEnabled(player, target.getLocation()))
        {
            return false;
        }


        double distance = player.getEyeLocation().distance(target.getLocation());
        Location previousLoc = target.getLocation();
        BukkitRunnable abilityTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Location stepLocation;
                // cancel animation entirely if player releases the button
                if(EntityUtil.getTimeElapsedEntityMeta(player, activeCastKey) < interactEventTimeWindow)
                {
                    Location idealLocation = player.getEyeLocation().clone();
                    idealLocation = idealLocation.add(idealLocation.clone().getDirection().normalize().multiply(distance));

                    if (idealLocation.distance(target.getLocation()) > maxTravelStep)
                    {
                        // calculate new location based on step size
                        //get vector between the two points and add it to the targets location
                        Vector stepVector = (idealLocation.clone().toVector().subtract(
                                target.getLocation().clone().toVector()).normalize().multiply(maxTravelStep));
                        stepLocation = target.getLocation().clone().add(stepVector);
                    }
                    else
                    {
                        stepLocation = idealLocation;
                    }
                    // if blocks at the new location aren't passable, don't move target.
                    if (!stepLocation.getBlock().isPassable() ||
                            !stepLocation.clone().add(0,1,0).getBlock().isPassable() ||
                            !stepLocation.clone().add(0,2,0).getBlock().isPassable()) // debug bounding boxes with alex
                    {
                        stepLocation = previousLoc;
                    }

                }
                else
                {
                    stepLocation = previousLoc;
                }

                // if player or target is in a protected region, cancel the skill.
                if (    !regProtManager.isLocationPVPEnabled(player, player.getLocation()) ||
                        !regProtManager.isLocationPVPEnabled(player, target.getLocation()))
                {
                    if (!this.isCancelled())
                    {
                        this.cancel();
                    }
                    return;
                }

                regProtManager.tagEntity(target, player);
                target.teleport(stepLocation);
                target.setVelocity(new Vector(0,0,0));

                previousLoc.setX(stepLocation.getX());
                previousLoc.setY(stepLocation.getY());
                previousLoc.setZ(stepLocation.getZ());
            }
        };
        abilityTask.runTaskTimer(plugin,0L, 1L);

        BukkitRunnable cancelTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (!abilityTask.isCancelled())
                {
                    abilityTask.cancel();
                }
            }
        };
        cancelTask.runTaskLater(plugin, duration * 20L);
        return true;
    }

    private void animateCircle(Player player, double angle)
    {
        // circle =
    }

    @Override
    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "And if I just move");
        lore.add(ChatColor.GRAY + "this guy like so...");
        return getAbilityItem(plugin, player, lore, 2);
    }
}
