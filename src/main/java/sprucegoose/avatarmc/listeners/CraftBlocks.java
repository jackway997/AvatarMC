package sprucegoose.avatarmc.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.utils.AvatarIDs;

public class CraftBlocks implements Listener
{
    private JavaPlugin plugin;

    public CraftBlocks(JavaPlugin plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void craftItem(CraftItemEvent event) {
        System.out.println("Testing craft item event");
        CraftingInventory inv = event.getInventory();


        for(ItemStack itemStack : inv.getStorageContents()) {
            if(AvatarIDs.itemStackHasAnyAvatarID(plugin, itemStack))
            {
                event.setCancelled(true);
            }
        }
    }
}
