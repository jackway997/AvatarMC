package sprucegoose.avatarmc.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockIterator;

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

    public static String setTimeStampedBlockMeta(JavaPlugin plugin, String key, FallingBlock block)
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

    public static boolean blockHasMeta(Block block, String key)
    {
        return block.hasMetadata(key);
    }

    public static boolean blockHasMeta(FallingBlock block, String key)
    {
        return block.hasMetadata(key);
    }

    public static Block getTargetBlock(Player player, int range) {
        BlockIterator iter = new BlockIterator(player, range);
        Block lastBlock = iter.next();
        while (iter.hasNext()) {
            lastBlock = iter.next();
            if (lastBlock.getType() == Material.AIR) {
                continue;
            }
            break;
        }
        return lastBlock;
    }

}