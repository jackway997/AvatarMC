package sprucegoose.avatarmc.abilities.water;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.*;
import java.util.ArrayList;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.EventPriority;

public class CreateWater extends Ability implements Listener
{

    public CreateWater(JavaPlugin plugin, RegionProtectionManager regProtMan)
    {
        super(plugin, regProtMan, ELEMENT_TYPE.water, ABILITY_LEVEL.beginner);
        setCooldown(2000);
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e)
    {
        EquipmentSlot slot = e.getHand();
        ItemStack item = e.getItem();
        Player player = e.getPlayer();

        Block targetBlockLocation = player.getTargetBlock(null, 100).getRelative(e.getBlockFace());

        if (    slot != null && item != null &&
                e.getAction().equals(Action.RIGHT_CLICK_BLOCK) &&
                targetBlockLocation.isEmpty() &&
                (slot.equals(EquipmentSlot.HAND) || slot.equals(EquipmentSlot.OFF_HAND)) &&
                abilityChecks(player, item) && !onCooldown(player) /*&&
                !regProtManager.isRegionProtected(player, e.getClickedBlock().getLocation(),this)*/
            ) {
            if (createWater(plugin, player, targetBlockLocation))
            {
                addCooldown(player, item);
            }
            e.setCancelled(true);
        }
    }

    private boolean createWater(JavaPlugin plugin, Player player, Block targetBlockLocation)
    {
        if (regProtManager.isLocationBreakable(player, targetBlockLocation.getLocation()))
        {
            targetBlockLocation.setType(Material.WATER);
            targetBlockLocation.getWorld().playSound(targetBlockLocation.getLocation(), Sound.ENTITY_DOLPHIN_SPLASH, 0.5f, 1.0f);

            // With BukkitScheduler
            BukkitScheduler scheduler = Bukkit.getScheduler();
            scheduler.runTaskLater(plugin, () ->
            {
                if (targetBlockLocation.getType().equals(Material.WATER))
                {
                    targetBlockLocation.setType(Material.AIR);
                }
            }, 20L * 1L);
            return true;
        }
        else
        {
            return false;
        }

    }

    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "extracts water");
        lore.add(ChatColor.GRAY + "from da erf");
        return getAbilityItem(plugin, player, lore, 2);
    }


}
