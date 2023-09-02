package sprucegoose.avatarmc.abilities;

import org.bukkit.*;
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
import org.bukkit.scheduler.BukkitTask;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.ImageParticles;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EarthTribute extends Ability implements Listener
{

    public EarthTribute(JavaPlugin plugin, RegionProtectionManager regProtMan)
    {
        super(plugin, regProtMan, ELEMENT_TYPE.earth, ABILITY_LEVEL.beginner);
        setCooldown(5000);
    }

    private Map<UUID, BukkitTask> activeAbilities = new HashMap<>();

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e)
    {
        EquipmentSlot slot = e.getHand();
        ItemStack item = e.getItem();
        Player player = e.getPlayer();

        if (    slot != null && item != null &&
                (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) &&
                (slot.equals(EquipmentSlot.HAND) || slot.equals(EquipmentSlot.OFF_HAND)) &&
                AvatarIDs.itemStackHasAvatarID(plugin,item, this.getAbilityID()) &&
                PlayerIDs.itemStackHasPlayerID(plugin, item, player) && !onCooldown(player)
        )
        {
            addCooldown(player, item);
            createSymbol(plugin, e);
            e.setCancelled(true);
        }
    }

    private void createSymbol(JavaPlugin plugin, PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Location location = player.getLocation().add(0,4.5,0);
        location.getWorld().playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 0.7f, 1.0f);

        if (!activeAbilities.containsKey(uuid))
        {
            // With BukkitScheduler
            BukkitScheduler scheduler = Bukkit.getScheduler();
            BukkitTask task = scheduler.runTaskTimer(plugin, () ->
            {
                ImageParticles.renderUprightImage(plugin, location, "graphics/earth_symbol.png");

            }, 0, 2L);
            activeAbilities.put(uuid, task);

            // With BukkitScheduler
            BukkitTask task2 = scheduler.runTaskLater(plugin, () ->
            {
                activeAbilities.get(uuid).cancel();
                activeAbilities.remove(uuid);

            }, 20L * 2L);
        }
    }

    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Proudly display the");
        lore.add(ChatColor.GRAY + "symbol of your people");
        return getAbilityItem(plugin, player, lore, 1);
    }
}
