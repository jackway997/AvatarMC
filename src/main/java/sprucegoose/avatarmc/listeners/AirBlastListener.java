package sprucegoose.avatarmc.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.Ability;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.BendingAbilities;
import sprucegoose.avatarmc.utils.Effects;
import sprucegoose.avatarmc.utils.PlayerIDs;

public class AirBlastListener extends Ability implements Listener
{


    public AirBlastListener(JavaPlugin plugin)
    {
        super(plugin);
        setCooldown(3000);
    }

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
                AvatarIDs.itemStackHasAvatarID(plugin,item, BendingAbilities.airBlastKey) &&
                PlayerIDs.itemStackHasPlayerID(plugin, item, e.getPlayer()) && !onCooldown(player)
            )
        {
            addCooldown(player, item);
            Effects.airBlast(player);
            e.setCancelled(true);
        }
    }
}
