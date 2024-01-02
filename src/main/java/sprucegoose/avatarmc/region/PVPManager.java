package sprucegoose.avatarmc.region;

import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PVPManager
{
    private String pluginName;
    private PvPManager plugin;

    public PVPManager(String pluginName) {
        this.pluginName = pluginName;

        if (Bukkit.getPluginManager().isPluginEnabled(pluginName)) {
            this.plugin = (PvPManager) Bukkit.getPluginManager().getPlugin(pluginName);
        }
    }

    public void tagPlayer(Player player, Player attacker)
    {
        // Once this is done you can get the PlayerHandler class
        PlayerHandler playerHandler = plugin.getPlayerHandler();
        PvPlayer pvpPlayer = playerHandler.get(player);
        PvPlayer pvpAttacker = playerHandler.get(attacker);

        pvpPlayer.setTagged(false, pvpAttacker);
    }

}
