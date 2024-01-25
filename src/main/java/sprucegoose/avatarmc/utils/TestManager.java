package sprucegoose.avatarmc.utils;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;

public class TestManager {
    private static TestManager instance;
    private JavaPlugin plugin;

    private HashSet<Player> testModePlayers = new HashSet<>();

    public TestManager(JavaPlugin plugin)
    {
        this.plugin = plugin;
        instance = this;
    }

    public static TestManager getInstance() {
        return instance;
    }

    public void setTestMode(Player player)
    {
        testModePlayers.add(player);
        plugin.getLogger().info(player.getName() + " now in ability test mode");
    }

    public void removeTestMode(Player player)
    {
        testModePlayers.remove(player);
        plugin.getLogger().info(player.getName() + " no longer in ability test mode");
    }

    public boolean playerInTestMode(Player player)
    {
        return testModePlayers.contains(player);
    }
}
