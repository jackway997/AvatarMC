package sprucegoose.avatarmc;

import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.abilities.EarthBend;
import sprucegoose.avatarmc.commands.TestCommand;
import sprucegoose.avatarmc.commands.TestCommand2;
import sprucegoose.avatarmc.abilities.AirBlast;
import sprucegoose.avatarmc.commands.TestCommand3;
import sprucegoose.avatarmc.commands.TestCommand4;
import sprucegoose.avatarmc.listeners.CraftBlocks;
import sprucegoose.avatarmc.listeners.SkillMenu;
import sprucegoose.avatarmc.abilities.WaterBend;
import sprucegoose.avatarmc.storage.MySQL;
import sprucegoose.avatarmc.storage.PlayerProgression;
import sprucegoose.avatarmc.utils.BendingManager;

import static org.bukkit.Bukkit.getServer;

public final class AvatarMC extends JavaPlugin
{
    private MySQL db;
    private PlayerProgression playerProgression;
    private BendingManager bendingManager;
    private SkillMenu skillMenuManager;

    @Override
    public void onEnable()
    {
        // Plugin startup logic
        String url = "test_db";
        String user = "root";
        String pass = "";

        db = new MySQL(this, this.getLogger(), user, pass, url);

        db.open();

        playerProgression = new PlayerProgression(this, db);

        bendingManager = new BendingManager(this, playerProgression);
        skillMenuManager = new SkillMenu(this, bendingManager);


        // Plugin startup logic
        // Register commands
        getCommand("test").setExecutor(new TestCommand(this));
        getCommand("test2").setExecutor(new TestCommand2(this, skillMenuManager));
        getCommand("remove_ability").setExecutor(new TestCommand3(this, playerProgression));
        getCommand("give_ability").setExecutor(new TestCommand4(this, playerProgression));

        // Register event listeners
        getServer().getPluginManager().registerEvents(new WaterBend(this), this);
        getServer().getPluginManager().registerEvents(new AirBlast(this), this);
        getServer().getPluginManager().registerEvents(new EarthBend(this), this);
        getServer().getPluginManager().registerEvents(new CraftBlocks(this), this);
        getServer().getPluginManager().registerEvents(new SkillMenu(this, bendingManager), this);
        getServer().getPluginManager().registerEvents(bendingManager, this);


    }

    @Override
    public void onDisable()
    {
        // Plugin shutdown logic
    }
}
