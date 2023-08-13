package sprucegoose.avatarmc;

import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.abilities.EarthBend;
import sprucegoose.avatarmc.commands.TestCommand;
import sprucegoose.avatarmc.commands.TestCommand2;
import sprucegoose.avatarmc.abilities.AirBlast;
import sprucegoose.avatarmc.listeners.CraftBlocks;
import sprucegoose.avatarmc.listeners.SkillMenu;
import sprucegoose.avatarmc.abilities.WaterBend;
import sprucegoose.avatarmc.utils.BendingManager;

import static org.bukkit.Bukkit.getServer;

public final class AvatarMC extends JavaPlugin
{
    BendingManager bendingManager;
    SkillMenu skillMenuManager;

    @Override
    public void onEnable()
    {
        bendingManager = new BendingManager(this);
        skillMenuManager = new SkillMenu(this, bendingManager);

        // Plugin startup logic
        // Register commands
        getCommand("test").setExecutor(new TestCommand(this));
        getCommand("test2").setExecutor(new TestCommand2(this, skillMenuManager));

        // Register event listeners
        getServer().getPluginManager().registerEvents(new WaterBend(this), this);
        getServer().getPluginManager().registerEvents(new AirBlast(this), this);
        getServer().getPluginManager().registerEvents(new EarthBend(this), this);
        getServer().getPluginManager().registerEvents(new CraftBlocks(this), this);
        getServer().getPluginManager().registerEvents(new SkillMenu(this, bendingManager), this);
    }

    @Override
    public void onDisable()
    {
        // Plugin shutdown logic
    }
}
