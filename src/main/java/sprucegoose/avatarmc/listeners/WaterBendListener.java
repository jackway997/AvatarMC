package sprucegoose.avatarmc.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.Ability;
import sprucegoose.avatarmc.utils.*;

public class WaterBendListener extends Ability implements Listener
{
    public WaterBendListener(JavaPlugin plugin)
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
                AvatarIDs.itemStackHasAvatarID(plugin,item, BendingAbilities.waterBendKey) &&
                PlayerIDs.itemStackHasPlayerID(plugin, item, player) && !onCooldown(player)
            )
        {
            addCooldown(player, item);
            Effects.createWater(plugin, e);
            e.setCancelled(true);
        }
    }
}
