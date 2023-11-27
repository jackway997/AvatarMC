package sprucegoose.avatarmc.abilities.fire;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.ImageParticles;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.*;

public class MeteorStrike extends Ability implements Listener
{
    // To do: add particle circle on the ground while charging

    public enum CHARGE_STATE {stage1, stage2, stage3, stage4}

    public MeteorStrike(JavaPlugin plugin, RegionProtectionManager regProtMan)
    {
        super(plugin, regProtMan, ELEMENT_TYPE.fire, ABILITY_LEVEL.master);
        setCooldown(5000);
    }

    private Map<UUID, BukkitTask> activeAbilities = new HashMap<>();

    @EventHandler
    public void onPlayerInteractEvent(PlayerToggleSneakEvent e)
    {
        Player player = e.getPlayer();
        ItemStack mainItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();

        if (    ((mainItem != null && AvatarIDs.itemStackHasAvatarID(plugin,mainItem, this.getAbilityID()) &&
                    PlayerIDs.itemStackHasPlayerID(plugin, mainItem, player))
                ||
                (offHandItem != null && AvatarIDs.itemStackHasAvatarID(plugin, offHandItem, this.getAbilityID()) &&
                        PlayerIDs.itemStackHasPlayerID(plugin, offHandItem, player)))
                && !onCooldown(player) && e.isSneaking())
        {

            // put in arrays to wrap into objects
            CHARGE_STATE[] state = {CHARGE_STATE.stage1};
            int[] timer = {0};
            final int stage1 = 50, stage2 = 100, stage3 = 150;

            BukkitRunnable chargeUpTask = new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if (player.isSneaking())
                    {
                        timer[0] += 1;
                        if (timer[0] >= stage3)
                        {
                            state[0] = CHARGE_STATE.stage4;
                        } else if (timer[0] >= stage2)
                        {
                            state[0] = CHARGE_STATE.stage3;
                        } else if (timer[0] >= stage1)
                        {
                            state[0] = CHARGE_STATE.stage2;
                        }

                        animateCharge(player, state[0]);
                    }
                    else
                    {
                        if (state[0] != CHARGE_STATE.stage1)
                        {
                            launchMeteor(player, state[0]);
                        }                        // or do meteor strike
                        this.cancel();

                    }
                }
            };
            chargeUpTask.runTaskTimer(plugin, 0L, 2L);

//            addCooldown(player, item);
//            createSymbol(plugin, e);
//            e.setCancelled(true);
        }
    }

    private void animateCharge(Player player, CHARGE_STATE state)
    {
        double travelDistance = 3.0;
        double speed = 1.0;

        // generate a random direction
        Random r = new Random();
        double randomX = -1 + ((2) * r.nextDouble());
        double randomY = 1 * r.nextDouble();
        double randomZ = -1 + ((2) * r.nextDouble());

        // Calculate particle location and speed
        Vector particleDirection = new Vector(randomX, randomY, randomZ).normalize().multiply(travelDistance);
        Location startLoc = player.getLocation().clone().add(particleDirection);
        particleDirection = particleDirection.normalize().multiply(-1.0);
        double xSpeed = particleDirection.getX();
        double ySpeed = particleDirection.getY();
        double zSpeed = particleDirection.getZ();

        Particle particle;
        if (state == CHARGE_STATE.stage1)
        {
            particle = Particle.FLAME;
        }
        else
        {
            particle = Particle.SOUL_FIRE_FLAME;
        }

        player.getWorld().spawnParticle(particle, startLoc,0, xSpeed, ySpeed, zSpeed, speed);

    }

    private void launchMeteor(Player player, CHARGE_STATE state)
    {

    }


    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "I came in like a ");
        lore.add(ChatColor.GRAY + "wrecking ball.");
        return getAbilityItem(plugin, player, lore, 1);
    }
}
