package sprucegoose.avatarmc.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class AvatarIDs
{
    public final static String avatarIDKey = "avatar-id";

    public static void setItemStackAvatarID(JavaPlugin plugin, ItemStack itemStack, String avatarID)
    {
            NamespacedKey key = new NamespacedKey(plugin, avatarIDKey);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, avatarID);
            itemStack.setItemMeta(itemMeta);
    }

    public static boolean itemStackHasAvatarID(JavaPlugin plugin, ItemStack itemStack, String avatarID)
    {
        NamespacedKey key = new NamespacedKey(plugin, avatarIDKey);
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        if(     container.has(key, PersistentDataType.STRING) &&
                container.get(key, PersistentDataType.STRING).equals(avatarID) )
        { return true; }
        else
        { return false; }
    }

    public static boolean itemStackHasAnyAvatarID(JavaPlugin plugin, ItemStack itemStack)
    {
        NamespacedKey key = new NamespacedKey(plugin, avatarIDKey);
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        if( container.has(key, PersistentDataType.STRING) )
        { return true; }
        else
        { return false; }
    }

    public static String getItemStackAvatarID(JavaPlugin plugin, ItemStack itemStack, String avatarID)
    {
        NamespacedKey key = new NamespacedKey(plugin, avatarIDKey);
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        if(container.has(key, PersistentDataType.STRING))
        {
            return container.get(key,PersistentDataType.STRING);
        }
        else
        { return null; }
    }
}
