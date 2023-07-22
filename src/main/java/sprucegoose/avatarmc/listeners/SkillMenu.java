package sprucegoose.avatarmc.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.BendingAbilities;

public class SkillMenu implements Listener
{
    private JavaPlugin plugin;

    public SkillMenu(JavaPlugin plugin)
    {
        this.plugin = plugin;
    }

    private final static String skillMenuName = ChatColor.RED + "Test Inventory";

    public static void openInventory(JavaPlugin plugin, Player player)
    {
        Inventory inv = Bukkit.createInventory(player, 9, skillMenuName);

        ItemStack item = BendingAbilities.getWaterBend(plugin, player);
        inv.setItem(0, item);
        player.openInventory(inv);
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent e)
    {
        if(e.getView().getTitle().equals(skillMenuName))
        {
            Player player = (Player)e.getView().getPlayer();

            if (e.getCurrentItem() == null)
                return;
            else if (AvatarIDs.itemStackHasAvatarID (plugin, e.getCurrentItem(), BendingAbilities.waterBendKey)
                && !player.getInventory().containsAtLeast(BendingAbilities.getWaterBend(plugin, player),1))
            {
                player.getInventory().addItem(BendingAbilities.getWaterBend(plugin, player));
            }
            e.setCancelled(true);
        }

    }


}
