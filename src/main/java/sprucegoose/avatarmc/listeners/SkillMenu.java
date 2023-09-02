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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.abilities.AbilityManager;
import sprucegoose.avatarmc.utils.ItemMetaTag;

public class SkillMenu implements Listener
{
    private final JavaPlugin plugin;
    public final AbilityManager abilityManager;
    public final static String invKey = "SkillInventoryKey";

    public SkillMenu(JavaPlugin plugin, AbilityManager abilityManager)
    {
        this.plugin = plugin;
        this.abilityManager = abilityManager;
    }

    private final static String skillMenuName = ChatColor.RED + "Test Inventory";

    public void openInventory(JavaPlugin plugin, Player player)
    {
        int invSize = 9 * 1;
        int slot = 0;

        Inventory inv = Bukkit.createInventory(player, invSize, skillMenuName);

        // Populate menu with all skills
        for (Ability ability : abilityManager.getPlayerAbilities(player))
        {
            ItemStack item = ability.getAbilityItem(plugin, player);
            ItemMetaTag.setItemMetaTag(plugin, item, invKey, ability.getAbilityID());
            inv.setItem(slot, item);
            slot++;
        }

        // Fill in remaining slots with blanks
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta itemMeta = filler.getItemMeta();
        if (itemMeta != null)
        {
            itemMeta.setDisplayName(ChatColor.RESET + "");
            filler.setItemMeta(itemMeta);
        }

        for (int ii = slot ; ii < 9; ii++)
            inv.setItem(ii, filler);

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
                ItemStack item = e.getCurrentItem();

                if (item == null)
                    return;

                for (Ability ability : abilityManager.getAbilities())
                    abilityClick(ability, player, item);

                e.setCancelled(true);
            }
        }
    }

    private void abilityClick(Ability ability, Player player, ItemStack item)
    {
        if (ItemMetaTag.itemStackHasTag(plugin, item, invKey, ability.getAbilityID())
            && !player.getInventory().containsAtLeast(ability.getAbilityItem(plugin, player), 1)
            && !player.getItemOnCursor().equals(ability.getAbilityItem(plugin, player)))
        {
            player.getInventory().addItem(ability.getAbilityItem(plugin, player));
        }
    }


}
