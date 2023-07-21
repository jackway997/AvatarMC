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
            if (itemMeta != null)
            {
                itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, avatarID);
                itemStack.setItemMeta(itemMeta);
            }
            else
                System.out.println("no item meta exists for object"+ itemStack.toString());
    }

    public static boolean itemStackHasAvatarID(JavaPlugin plugin, ItemStack itemStack, String avatarID)
    {
        NamespacedKey key = new NamespacedKey(plugin, avatarIDKey);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null && avatarID != null) {
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();

            return container.has(key, PersistentDataType.STRING) &&
                    container.get(key, PersistentDataType.STRING).equals(avatarID);
        }
        else
            return false;
    }

    public static boolean itemStackHasAnyAvatarID(JavaPlugin plugin, ItemStack itemStack)
    {
        NamespacedKey key = new NamespacedKey(plugin, avatarIDKey);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta != null)
        {
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            return container.has(key, PersistentDataType.STRING);
        }
        else
            return false;
    }

    public static String getItemStackAvatarID(JavaPlugin plugin, ItemStack itemStack, String avatarID)
    {
        NamespacedKey key = new NamespacedKey(plugin, avatarIDKey);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();

            if (container.has(key, PersistentDataType.STRING))
                return container.get(key, PersistentDataType.STRING);
            else
                return null;
        }
        else
            return null;
    }
}
