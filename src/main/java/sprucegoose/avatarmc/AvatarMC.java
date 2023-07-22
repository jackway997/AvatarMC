package sprucegoose.avatarmc;

import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.commands.TestCommand;
import sprucegoose.avatarmc.commands.TestCommand2;
import sprucegoose.avatarmc.listeners.AirBlastListener;
import sprucegoose.avatarmc.listeners.CraftBlocks;
import sprucegoose.avatarmc.listeners.SkillMenu;
import sprucegoose.avatarmc.listeners.WaterBendListener;

public final class AvatarMC extends JavaPlugin
{

    @Override
    public void onEnable()
    {
        // Plugin startup logic
        // Register commands
        getCommand("test").setExecutor(new TestCommand(this));
        getCommand("test2").setExecutor(new TestCommand2(this));

        // Register event listeners
        getServer().getPluginManager().registerEvents(new WaterBendListener(this), this);
        getServer().getPluginManager().registerEvents(new AirBlastListener(this), this);
        getServer().getPluginManager().registerEvents(new CraftBlocks(this), this);
        getServer().getPluginManager().registerEvents(new SkillMenu(this), this);
    }

    @Override
    public void onDisable()
    {
        // Plugin shutdown logic
    }
}
