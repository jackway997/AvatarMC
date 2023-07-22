package sprucegoose.avatarmc.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemMetaTag
{
    public static <T> void setItemMetaTag(JavaPlugin plugin, ItemStack itemStack, String tagKey, String tagValue)
    {
            NamespacedKey key = new NamespacedKey(plugin, tagKey);
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null)
            {
                itemMeta.getPersistentDataContainer().set(key, T, tagValue);
                itemStack.setItemMeta(itemMeta);

            }
            else
                System.out.println("no item meta exists for object"+ itemStack.toString());
    }

    public static boolean itemStackHasTag(JavaPlugin plugin, ItemStack itemStack, String tagKey, String tagValue)
    {
        NamespacedKey key = new NamespacedKey(plugin, tagKey);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null && tagValue != null) {
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();

            return container.has(key, PersistentDataType.STRING) &&
                    container.get(key, PersistentDataType.STRING).equals(tagValue);
        }
        else
            return false;
    }

    public static boolean itemStackHasAnyTag(JavaPlugin plugin, ItemStack itemStack, String tagKey)
    {
        NamespacedKey key = new NamespacedKey(plugin, tagKey);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta != null)
        {
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            return container.has(key, PersistentDataType.STRING);
        }
        else
            return false;
    }

    public static String getItemStackTag(JavaPlugin plugin, ItemStack itemStack, String tagKey)
    {
        NamespacedKey key = new NamespacedKey(plugin, tagKey);
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
