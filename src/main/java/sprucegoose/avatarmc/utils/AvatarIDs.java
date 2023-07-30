package sprucegoose.avatarmc.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class AvatarIDs
{
    public final static String avatarIDKey = "avatar-id";

    public static void setItemStackAvatarID(JavaPlugin plugin, ItemStack itemStack, String avatarID)
    {
            ItemMetaTag.setItemMetaTag(plugin, itemStack, avatarIDKey, avatarID);
    }

    public static boolean itemStackHasAvatarID(JavaPlugin plugin, ItemStack itemStack, String avatarID)
    {
        return ItemMetaTag.itemStackHasTag(plugin, itemStack, avatarIDKey, avatarID);
    }

    public static boolean itemStackHasAnyAvatarID(JavaPlugin plugin, ItemStack itemStack)
    {
        return ItemMetaTag.itemStackHasAnyTag(plugin, itemStack, avatarIDKey);
    }

    public static String getItemStackAvatarID(JavaPlugin plugin, ItemStack itemStack, String avatarID)
    {
        return ItemMetaTag.getItemStackTag(plugin, itemStack, avatarIDKey);
    }
}
