package sprucegoose.avatarmc.listeners;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
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
    public void prepareCraftItem(PrepareItemCraftEvent event) {
        CraftingInventory inv = event.getInventory();

        for(ItemStack itemStack : inv.getStorageContents()) {
            if(AvatarIDs.itemStackHasAnyAvatarID(plugin, itemStack))
            {
                inv.setResult(null);
            }
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent e)
    {
        plugin.getLogger().info("Item spawn event");
    }
    @EventHandler
    public void onItemDrop (PlayerDropItemEvent e)
    {
        plugin.getLogger().info("Item drop event");
        Item drop = e.getItemDrop();
        if (AvatarIDs.itemStackHasAnyAvatarID(plugin,drop.getItemStack()))
        {
            e.setCancelled(true);
            e.getPlayer().getInventory().remove(drop.getItemStack());
        }
    }

    @EventHandler
    public void onInventoryClickEvent (InventoryClickEvent e) {
        plugin.getLogger().info("Inventory click event");
        plugin.getLogger().info("clicked inv: "+e.getClickedInventory());
        plugin.getLogger().info("action: "+e.getAction());
        plugin.getLogger().info("inv: "+e.getInventory());
        plugin.getLogger().info("cursor: "+e.getCursor());
        plugin.getLogger().info("curr item: "+e.getCurrentItem());
        ItemStack cursor = e.getCursor();
        ItemStack currItem = e.getCurrentItem();
        InventoryAction action = e.getAction();
        Inventory inv = e.getInventory();
        Inventory clickedInv = e.getClickedInventory();
        if ((AvatarIDs.itemStackHasAnyAvatarID(plugin, cursor) || AvatarIDs.itemStackHasAnyAvatarID(plugin, currItem)) &&
                (action == InventoryAction.MOVE_TO_OTHER_INVENTORY && (inv.getType() == InventoryType.PLAYER)) ||
                (clickedInv != null && clickedInv.getType() == InventoryType.PLAYER && !SkillMenu.isSkillMenu(e.getView().getTitle()))
        )
        {
            e.setCancelled(true);
        }

    }


    @EventHandler
    public void sheepDyeWoolEvent(SheepDyeWoolEvent e) {
        Player player = e.getPlayer();
        if (player == null)
        {
            return;
        }
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();

        if (AvatarIDs.itemStackHasAnyAvatarID(plugin,item))
        {
            e.setCancelled(true);
        }
    }
}