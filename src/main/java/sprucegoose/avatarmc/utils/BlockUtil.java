package sprucegoose.avatarmc.utils;

import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BlockUtil {
    private final static String timeFormat = "yyyy/MM/dd HH:mm:ss";
    public static String setTimeStampedBlockMeta(JavaPlugin plugin, String key, Block block)
    {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(timeFormat);
        LocalDateTime now = LocalDateTime.now();
        String time = dtf.format(now);

        block.setMetadata(key, new FixedMetadataValue(plugin, time));

        return time;
    }

    public static void removeTimeStampedBlockMeta(JavaPlugin plugin, String key, Block block)
    {
        block.removeMetadata(key, plugin);
    }

    public static boolean blockHasMetaAtTime(Block block, String key, String time)
    {
        List<MetadataValue> values = block.getMetadata(key);
        if (!values.isEmpty() && values.get(0).asString().equals(time))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

}