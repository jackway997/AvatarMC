package sprucegoose.avatarmc;

import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.commands.TestCommand;
import sprucegoose.avatarmc.listeners.CraftBlocks;
import sprucegoose.avatarmc.listeners.WaterBendListener;

public final class AvatarMC extends JavaPlugin
{

    @Override
    public void onEnable()
    {
        // Plugin startup logic
        // Register commands
        getCommand("test").setExecutor(new TestCommand(this));

        // Register event listeners
        getServer().getPluginManager().registerEvents(new WaterBendListener(this), this);
        getServer().getPluginManager().registerEvents(new CraftBlocks(this), this);
    }

    @Override
    public void onDisable()
    {
        // Plugin shutdown logic
    }
}
