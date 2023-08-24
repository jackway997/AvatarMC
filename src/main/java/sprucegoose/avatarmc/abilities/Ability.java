package sprucegoose.avatarmc.abilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.ItemMetaTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public abstract class Ability implements Listener {


    public enum ELEMENT_TYPE {air, water, earth, fire}
    public enum ABILITY_LEVEL {beginner, adept, expert, master}
    public final HashMap<UUID, Long> cooldowns = new HashMap<UUID, Long>();
    private ELEMENT_TYPE element;
    private ABILITY_LEVEL level;
    protected long cooldown = 3000;
    protected JavaPlugin plugin;
    protected RegionProtectionManager regProtManager;
    private static final String skillBookKey = "AvatarMCSkillBookKey";

    public Ability(JavaPlugin plugin, RegionProtectionManager regProtManager, ELEMENT_TYPE element, ABILITY_LEVEL level)
    {
        this.plugin = plugin;
        this.regProtManager = regProtManager;
        this.element = element;
        this.level = level;
    }

    public ELEMENT_TYPE getElement(){
        return this.element;
    }

    public static final String getSkillBookKey()
    {
        return skillBookKey;
    }
    public void setCooldown(long cooldown)
    {
        this.cooldown = cooldown;
    }

    public void addCooldown(Player player, ItemStack item)
    {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        //System.out.println("Cooldown added for "+ player.getName()); // debug info
        updateCooldownDisplay(player, item);
    }

    public boolean onCooldown(Player player)
    {
        Long lastTime = cooldowns.get(player.getUniqueId());
        if (lastTime == null)
            return false;
        else return (System.currentTimeMillis() - lastTime) < cooldown;
    }

    protected int getBookModelData()
    {
        switch(element)
        {
            case air: return 1;
            case earth: return 2;
            case fire: return 3;
            case water: return 4;
            default: return 0;
        }
    }

    protected ChatColor getBookColour()
    {
        switch(level)
        {
            case beginner: return ChatColor.GRAY;
            case adept: return ChatColor.BLUE;
            case expert: return ChatColor.DARK_PURPLE;
            case master: return ChatColor.GOLD;
            default: return ChatColor.BLACK;
        }
    }

    public String getDisplayName()
    {
        return getAbilityID(); // to be updated
    }

    public void updateCooldownDisplay(Player player, ItemStack item)
    {
        Long lastTime = cooldowns.get(player.getUniqueId());
        if (lastTime == null)
            return;
        else if (System.currentTimeMillis() - lastTime < cooldown)
        {
            int numSeconds = (int)Math.ceil((double)(cooldown - System.currentTimeMillis() + lastTime) / 1000.0);
            // update text on item
            //System.out.println(numSeconds);
            item.setAmount(numSeconds);

            BukkitScheduler scheduler = Bukkit.getScheduler();
            scheduler.runTaskLater(plugin, () ->
            {
                updateCooldownDisplay(player, item);
            }, 20L);

        }
    }

    public final String getAbilityID()
    {
        return this.getClass().getSimpleName();
    }

    public String toString()
    {
        return this.getClass().getSimpleName();
    }
    public final String getAbilityBookID()
    {
        return this.getClass().getSimpleName() + "_book";
    }


    public abstract ItemStack getAbilityItem(JavaPlugin plugin, Player player);

    public ItemStack getSkillBookItem(JavaPlugin plugin)
    {
        ItemStack skillBook = new ItemStack(Material.BOOK, 1);
        ItemMeta skill_meta = skillBook.getItemMeta();
        String displayName = getBookColour() + "" + ChatColor.BOLD + getDisplayName();
        skill_meta.setDisplayName(displayName);
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.DARK_GRAY + "" + ChatColor.ITALIC +"(shift-right click to learn)");
        skill_meta.setLore(lore);
        skill_meta.setCustomModelData(this.getBookModelData());
        skillBook.setItemMeta(skill_meta);
        ItemMetaTag.setItemMetaTag(plugin, skillBook, getSkillBookKey(), getAbilityBookID());

        return skillBook;
    }
}
