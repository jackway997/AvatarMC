package sprucegoose.avatarmc.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class BendingAbilities
{
    public final static String waterBendKey = "waterbend-key";

    public static ItemStack getWaterBend(JavaPlugin plugin, Player player)
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

        AvatarIDs.setItemStackAvatarID(plugin, skill, waterBendKey);
        PlayerIDs.setItemStackPlayerID(plugin, skill, player);

        return skill;
    }
}