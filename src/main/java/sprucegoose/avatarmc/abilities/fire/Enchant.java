package sprucegoose.avatarmc.abilities.fire;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.*;

public class Enchant extends Ability implements Listener
{
    private long cooldown; // 30s
    private long duration; // 20s
    private long trueBurnDuration; // 8s
    private double tickDamage; // 2

    public Enchant(JavaPlugin plugin, RegionProtectionManager regProtMan)
    {
        super(plugin, regProtMan, ELEMENT_TYPE.fire, ABILITY_LEVEL.adept);
        setCooldown(cooldown * 1000);
    }

    @Override
    public void loadProperties()
    {
        this.cooldown = getConfig().getLong("Abilities.Fire.Enchant.Cooldown");
        this.duration = getConfig().getLong("Abilities.Fire.Enchant.Duration");
        this.trueBurnDuration = getConfig().getLong("Abilities.Fire.Enchant.TrueBurnDuration");
        this.tickDamage = getConfig().getDouble("Abilities.Fire.Enchant.TickDamage");
    }

    private Map<UUID, BukkitTask> activeAbilities = new HashMap<>();

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e)
    {
        EquipmentSlot slot = e.getHand();
        ItemStack item = e.getItem();
        Player player = e.getPlayer();

        if (slot != null && item != null &&
                (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) &&
                (slot.equals(EquipmentSlot.HAND) || slot.equals(EquipmentSlot.OFF_HAND)) &&
                abilityChecks(player, item) && !onCooldown(player) )
        {
            if (player.isSneaking())
            {
                fireImbuement(player);
                player.setMetadata("fireImbued", new FixedMetadataValue(plugin, true));
                enchantAnimation(player);
                addCooldown(player, item);
            }
            else
            {
               if (getEntityHit(player))
               {
                   addCooldown(player, item);
               }
            }
            e.setCancelled(true);
        }
    }

    public void fireImbuement(Player player)
    {
        BukkitRunnable task1 = new BukkitRunnable()
        {
            @Override
            public void run()
            {


                player.removeMetadata("fireImbued", plugin);
                player.sendMessage(ChatColor.DARK_RED + "Your fire imbuement has worn off");

            }
        };
        task1.runTaskLater(plugin, duration * 20L);
    }

    public boolean getEntityHit(Player p)
    {

        int iterations = 40;
        Location eyeLocation = p.getLocation();
        for (int i = 0; i < iterations; i++)
        {
            double x = i;

            Vector direction = eyeLocation.getDirection();
            Vector rayPos = direction.clone().multiply(x);

            Location rayLoc = new Location(
                    eyeLocation.getWorld(),
                    eyeLocation.getX() + rayPos.getX(),
                    eyeLocation.getY() + rayPos.getY() + 2,
                    eyeLocation.getZ() + rayPos.getZ()
            );

            Collection<Entity> nearbyEntities = rayLoc.getWorld().getNearbyEntities(rayLoc, 0.65, 1, 0.65);
            for (Entity entity : nearbyEntities)
            {
                if (entity != null && entity != p && entity instanceof Player pe && p.hasLineOfSight(entity))
                {


                    pe.setMetadata("fireImbued", new FixedMetadataValue(plugin, true));
                    pe.sendMessage(ChatColor.WHITE + "" + p.getDisplayName() + ChatColor.DARK_RED + " has imbued you with fire");
                    enchantAnimation(pe);
                    fireImbuement(pe);
                    return true;

                }
            }
        }
       return false;
    }

    @EventHandler
    public void fireImbuementCheck(EntityDamageByEntityEvent e)
    {
        if (e.getDamager() instanceof Player)
        {
            Player p = (Player) e.getDamager();

            if (p.hasMetadata("fireImbued"))
            {

                Entity target = e.getEntity();
                if (target instanceof LivingEntity le)
                {

                    target.setMetadata("trueBurn", new FixedMetadataValue(plugin, true));
                    trueBurnTimer(le);
                    trueBurn(p, le);
                }
            }
        }
    }

    public void trueBurnTimer(Entity p)
    {
        BukkitRunnable task2 = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                p.removeMetadata("trueBurn", plugin);

            }
        };task2.runTaskLater(plugin, trueBurnDuration * 20L);
    }

    public void trueBurn(LivingEntity caster, Entity p)
    {
        BukkitRunnable task2 = new BukkitRunnable()
        {
            @Override
            public void run() {

                LivingEntity target = (LivingEntity) p;
                Location location = p.getLocation();
                World world = location.getWorld();

                if (p.hasMetadata("trueBurn"))
                {
                    if (world != null && !regProtManager.isLocationPVPEnabled(caster, p.getLocation()))
                    {
                        for (int i = 0; i < 1; i++)
                        { // Spawn 30 particle
                            if (!target.isDead())
                            {
                                world.spawnParticle(Particle.SOUL_FIRE_FLAME, location, 0, 0, 0, 0, 0.1);
                                ((LivingEntity) p).damage(tickDamage);
                            }
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
            }
        }; task2.runTaskTimer(plugin,0L,1L);
    }

    public void enchantAnimation(Player p)
    {
        BukkitRunnable task4 = new BukkitRunnable()
        {
            int step = 0;

            @Override
            public void run()
            {
                Location center = p.getLocation();
                World world = p.getWorld();

                double radius = 1.2; // Define the radius of the helix
                int particleCount = 100; // Number of particles in the circle
                double waveFrequency = 0.5; // Adjust frequency of the sine wave
                double amplitude = 0.062; // Adjust the amplitude of the sine wave
                int iterations = 40*3/4; // Number of iterations

                if (step >= iterations)
                {
                    this.cancel();
                    return;
                }

                double angle = 2 * Math.PI * ((double) step / iterations);

                double x = center.getX() + (radius * Math.cos(angle));
                double y = center.getY() + (amplitude * step);
                double z = center.getZ() + (radius * Math.sin(angle));

                Location particleLoc = new Location(world, x, y, z);
                world.spawnParticle(Particle.REDSTONE, particleLoc, 1, new Particle.DustOptions(Color.RED, 1));

                step++; // Increment the step to move to the next particle position
            }
        }; task4.runTaskTimer(plugin, 0L, 1L);
    }

    public ItemStack getAbilityItem (JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Barbeque your enemies");
        lore.add(ChatColor.GRAY + "with style");
        return getAbilityItem(plugin, player, lore, 1);
    }

}
