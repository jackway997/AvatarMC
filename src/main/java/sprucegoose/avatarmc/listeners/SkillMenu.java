package sprucegoose.avatarmc.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.BendingAbilities;
import sprucegoose.avatarmc.utils.ItemMetaTag;

public class SkillMenu implements Listener
{
    private JavaPlugin plugin;
    public final static String invKey = "SkillInventoryKey";

    public SkillMenu(JavaPlugin plugin)
    {
        this.plugin = plugin;
    }

    private final static String skillMenuName = ChatColor.RED + "Test Inventory";

    public static void openInventory(JavaPlugin plugin, Player player)
    {
        Inventory inv = Bukkit.createInventory(player, 9, skillMenuName);

        ItemStack item = BendingAbilities.getWaterBend(plugin, player);
        ItemMetaTag.setItemMetaTag(plugin, item, invKey, BendingAbilities.waterBendKey);
        inv.setItem(0, item);
        player.openInventory(inv);
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent e)
    {

        if(e.getView().getTitle().equals(skillMenuName))
        {
            if (e.getClickedInventory() != null && e.getClickedInventory().getType() != InventoryType.PLAYER)
            {
                Player player = (Player) e.getView().getPlayer();

                if (e.getCurrentItem() == null)
                    return;
                else if (ItemMetaTag.itemStackHasTag(plugin, e.getCurrentItem(), invKey, BendingAbilities.waterBendKey)
                        && !player.getInventory().containsAtLeast(BendingAbilities.getWaterBend(plugin, player), 1)
                        && !player.getItemOnCursor().equals(BendingAbilities.getWaterBend(plugin, player)))
                {
                    player.getInventory().addItem(BendingAbilities.getWaterBend(plugin, player));
                }
                e.setCancelled(true);
            }
        }

    }


}
