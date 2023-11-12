package sprucegoose.avatarmc.abilities;

import de.slikey.effectlib.util.ParticleEffect;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.BlockUtil;
import sprucegoose.avatarmc.utils.ImageParticles;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.*;

/*
Notes: bugs: when you toss the same block multiple times, sometimes the
 scheduler will remove a block from a previous toss
 */

public class Firebolt extends Ability
{
    private final Map<UUID, Boolean> explosionOccurred = new HashMap<>();
    private Map<UUID, BukkitTask> activeBends = new HashMap<>();



    public Firebolt(JavaPlugin plugin, RegionProtectionManager regProtManager)
    {
        super(plugin, regProtManager, ELEMENT_TYPE.fire, ABILITY_LEVEL.adept);
        setCooldown(1);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        UUID playerUUID = player.getUniqueId();
        EquipmentSlot slot = e.getHand();
        ItemStack item = e.getItem();

        // Check if the player is holding the skill item
        if (    slot != null && item != null &&
                (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) &&
                (slot.equals(EquipmentSlot.HAND) || slot.equals(EquipmentSlot.OFF_HAND)) &&
                AvatarIDs.itemStackHasAvatarID(plugin,item, this.getAbilityID()) &&
                PlayerIDs.itemStackHasPlayerID(plugin, item, player))
        {


            if (!activeBends.containsKey(playerUUID)) // If not currently bending flame
            {
                // spawn flame
                Location fireLoc = player.getEyeLocation();
                fireLoc.add(fireLoc.getDirection());

                // Schedule Task to animate flame
                BukkitScheduler scheduler = Bukkit.getScheduler();
                BukkitTask task1 = scheduler.runTaskTimer(plugin, () ->
                {
                    player.getWorld().spawnParticle(Particle.FLAME,fireLoc,1,0.1,0.1,0.1);
                },  0,  2L);
                activeBends.put(playerUUID, task1);

                // With BukkitScheduler
                BukkitTask task2 = scheduler.runTaskLater(plugin, () ->
                {
                    BukkitTask animationTask = activeBends.get(playerUUID);
                    if (animationTask != null)
                    {
                        animationTask.cancel();
                        activeBends.remove(playerUUID);
                    }

                }, 3L * 20L);
            }
            else
            {
                // launch flame
            }

            e.setCancelled(true);
        }
    }

    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Practice makes perfect");
        return getAbilityItem(plugin, player, lore, 2);
    }
}
