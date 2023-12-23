package sprucegoose.avatarmc.abilities.water;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.*;


// To do: spiders do not freeze

public class FlowAsWater extends WaterAbility
{
    public FlowAsWater(JavaPlugin plugin, RegionProtectionManager regProtManager, WaterEffects waterEffects)
    {
        super(plugin, regProtManager, waterEffects, ABILITY_LEVEL.expert);
        setCooldown(5000);
    }

    private Set<UUID> fallingSet = new HashSet<>();

    // constants
    private final double speed = 0.4;
    private final long duration = 3L * 20L;

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent e)
    {
        Player player = e.getPlayer();
        ItemStack mainItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();

        if (mainItem != null && AvatarIDs.itemStackHasAvatarID(plugin, mainItem, this.getAbilityID()) &&
                PlayerIDs.itemStackHasPlayerID(plugin, mainItem, player) && !onCooldown(player) && e.isSneaking())
        {
            if (!onCooldown(player))
            {
                addCooldown(player, mainItem);
                doAbility(plugin, player);
            }
        } else if (offHandItem != null && AvatarIDs.itemStackHasAvatarID(plugin, offHandItem, this.getAbilityID()) &&
                PlayerIDs.itemStackHasPlayerID(plugin, offHandItem, player) && !onCooldown(player) && e.isSneaking())

        {
            if (!onCooldown(player))
            {
                addCooldown(player, offHandItem);
                doAbility(plugin, player);
            }
        }
    }

    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Go with the flow.");
        return getAbilityItem(plugin, player, lore, 2);
    }

    private void doAbility(JavaPlugin plugin, Player player)
    {
        fallingSet.add(player.getUniqueId());
        BukkitRunnable movementTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (player.isSneaking())
                {
                    // boost player towards look direction
                    Location lookDirection = player.getEyeLocation().clone();

                    Vector vectorDirection = lookDirection.getDirection().normalize().multiply(speed);
                    player.setVelocity(vectorDirection);

                    // draw some particles
                    //bounds
                    final double minWidth = -0.35;
                    final double maxWidth = 0.35;
                    final double minHeight = 0;
                    final double maxHeight = 1.2;
                    int particlesPerTick = 4;
                    int blueToWhiteRatio = 5;

                    Location playerLoc = player.getLocation();
                    double theta = playerLoc.getYaw();
                    Random rand = new Random(System.currentTimeMillis());
                    for (int ii = 0 ; ii < particlesPerTick ; ii++)
                    {
                        double widthPos = rand.nextDouble(minWidth, maxWidth);
                        double x = widthPos * Math.cos(theta*Math.PI/180);
                        double y = rand.nextDouble(minHeight, maxHeight);
                        double z = widthPos * Math.sin(theta*Math.PI/180);

                        Location particleLoc = new Location(player.getWorld(),x+playerLoc.getX(), y+playerLoc.getY(), z+playerLoc.getZ());
                        Color color;
                        int colourRoll = rand.nextInt(0,blueToWhiteRatio);
                        if (colourRoll == (blueToWhiteRatio-1))
                        {
                            color = Color.WHITE;
                        }
                        else
                        {
                            color = Color.fromRGB(0,0,0); // TBC with colour picker
                        }

                        Particle.DustOptions options = new Particle.DustOptions(color, 1);
                        player.getWorld().spawnParticle(Particle.REDSTONE,particleLoc,1,options);
                    }
                }
                else
                {
                    if (!this.isCancelled())
                    {
                        this.cancel();
                    }
                }
            }
        };
        movementTask.runTaskTimer(plugin, 0L, 1L);

        BukkitRunnable cancelTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (!movementTask.isCancelled())
                {
                    movementTask.cancel();
                }
            }
        };
        cancelTask.runTaskLater(plugin, duration);
    }

    @EventHandler
    public void onPlayerFall(EntityDamageEvent e) {
        if (    e.getEntity() instanceof Player player &&
                fallingSet.contains(player.getUniqueId()) &&
                e.getCause() == EntityDamageEvent.DamageCause.FALL)
        {
            fallingSet.remove(player.getUniqueId());
            e.setCancelled(true);
        }
    }

}

