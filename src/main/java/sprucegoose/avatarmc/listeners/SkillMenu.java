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
import sprucegoose.avatarmc.abilities.ProgressionManager;
import sprucegoose.avatarmc.utils.ItemMetaTag;

public class SkillMenu implements Listener
{
    private final JavaPlugin plugin;
    public final AbilityManager abilityManager;
    public final ProgressionManager progressionManager;
    public final static String invKey = "SkillInventoryKey";

    public SkillMenu(JavaPlugin plugin, AbilityManager abilityManager, ProgressionManager progressionManager)
    {
        this.plugin = plugin;
        this.abilityManager = abilityManager;
        this.progressionManager = progressionManager;
    }


    private final static String skillMenuName = ChatColor.BLACK + ""+ ChatColor.BOLD+ "Abilities";

    public static boolean isSkillMenu(String invName)
    {
        if (invName != null && invName.length() >= skillMenuName.length()) {
            String invPrefix = invName.substring(0, skillMenuName.length());

            //p.getLogger().info(Boolean.toString(skillMenuName.equals(invPrefix)));
            //plugin.getLogger().info(invPrefix);
            //plugin.getLogger().info(skillMenuName);
            return skillMenuName.equals(invPrefix);
        }
        else
            return false;
    }

    public void openInventory(JavaPlugin plugin, Player player)
    {
        ProgressionManager.BENDER_TYPE type = progressionManager.getBenderType(player);
        String benderLevel = progressionManager.getBenderLevel(player).name();
        benderLevel = benderLevel.substring(0,1).toUpperCase() + benderLevel.substring(1);

        String skillMenuNameLocal;
        if (type == ProgressionManager.BENDER_TYPE.avatar)
        {
            skillMenuNameLocal = skillMenuName + " | "+ ChatColor.BLACK + benderLevel + " " + type;
        }
        else if (type != null && type != ProgressionManager.BENDER_TYPE.none)
        {
            skillMenuNameLocal = skillMenuName + " | "+  ChatColor.BLACK + benderLevel +
                    " " + type + " bender";
        }
        else
        {
            skillMenuNameLocal = skillMenuName + " | "+ ChatColor.BLACK +"Non-bender";
        }
        int numAbilities = (abilityManager.getPlayerAbilities(player).size() / 9) + 1;
        int invSize = 9 * numAbilities;
        int slot = 0;

        Inventory inv = Bukkit.createInventory(player, invSize, skillMenuNameLocal);

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

        for (int ii = slot ; ii < invSize; ii++)
            inv.setItem(ii, filler);

        player.openInventory(inv);
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent e)
    {
        if(isSkillMenu(e.getView().getTitle()))
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
