package sprucegoose.avatarmc.abilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import sprucegoose.avatarmc.Ability;
import sprucegoose.avatarmc.utils.*;

import java.util.ArrayList;

public class WaterBend extends Ability implements Listener
{

    public WaterBend(JavaPlugin plugin)
    {
        super(plugin);
        setCooldown(2000);
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e)
    {
        EquipmentSlot slot = e.getHand();
        ItemStack item = e.getItem();
        Player player = e.getPlayer();

        if (    slot != null && item != null &&
                e.getAction().equals(Action.RIGHT_CLICK_BLOCK) &&
                (slot.equals(EquipmentSlot.HAND) || slot.equals(EquipmentSlot.OFF_HAND)) &&
                AvatarIDs.itemStackHasAvatarID(plugin,item, WaterBend.class.getSimpleName()) &&
                PlayerIDs.itemStackHasPlayerID(plugin, item, player) && !onCooldown(player)
            )
        {
            addCooldown(player, item);
            createWater(plugin, e);
            e.setCancelled(true);
        }
    }

    private static void createWater(JavaPlugin plugin, PlayerInteractEvent event)
    {
        Block targetBlockLocation = event.getPlayer().getTargetBlock(null, 100).getRelative(event.getBlockFace());
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
    }

    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        ItemStack skill = new ItemStack(Material.LAPIS_LAZULI, 1);

        ItemMeta skill_meta = skill.getItemMeta();
        skill_meta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + "Create Water");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "(Soulbound)");
        lore.add(ChatColor.GRAY + "extracts water");
        lore.add(ChatColor.GRAY + "from da erf");
        skill_meta.setLore(lore);
        skill.setItemMeta(skill_meta);

        AvatarIDs.setItemStackAvatarID(plugin, skill, WaterBend.class.getSimpleName());
        PlayerIDs.setItemStackPlayerID(plugin, skill, player);

        return skill;
    }


}
