package sprucegoose.avatarmc;

import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.abilities.BoulderToss;
import sprucegoose.avatarmc.commands.TestCommand;
import sprucegoose.avatarmc.commands.TestCommand2;
import sprucegoose.avatarmc.abilities.AirBlast;
import sprucegoose.avatarmc.commands.TestCommand3;
import sprucegoose.avatarmc.commands.TestCommand4;
import sprucegoose.avatarmc.listeners.CraftBlocks;
import sprucegoose.avatarmc.listeners.SkillMenu;
import sprucegoose.avatarmc.abilities.CreateWater;
import sprucegoose.avatarmc.storage.MySQL;
import sprucegoose.avatarmc.storage.PlayerProgression;
import sprucegoose.avatarmc.abilities.AbilityManager;

public final class AvatarMC extends JavaPlugin
{
    private MySQL db;
    private PlayerProgression playerProgression;
    private AbilityManager abilityManager;
    private SkillMenu skillMenuManager;

    @Override
    public void onEnable()
    {
        saveDefaultConfig(); // add config.yml to plugins folder

        // Plugin startup logic
        String url = this.getConfig().getString("database-name");
        String user = this.getConfig().getString("database-user");
        String pass = this.getConfig().getString("database-pass");

        db = new MySQL(this, this.getLogger(), user, pass, url);

        db.open();

        playerProgression = new PlayerProgression(this, db);

        abilityManager = new AbilityManager(this, playerProgression);
        skillMenuManager = new SkillMenu(this, abilityManager);


        // Plugin startup logic
        // Register commands
        getCommand("test").setExecutor(new TestCommand(this, abilityManager));
        getCommand("test2").setExecutor(new TestCommand2(this, skillMenuManager));
        getCommand("remove_ability").setExecutor(new TestCommand3(this, playerProgression));
        getCommand("give_ability").setExecutor(new TestCommand4(this, playerProgression));

        // Register event listeners
        getServer().getPluginManager().registerEvents(new CreateWater(this), this);
        getServer().getPluginManager().registerEvents(new AirBlast(this), this);
        getServer().getPluginManager().registerEvents(new BoulderToss(this), this);
        getServer().getPluginManager().registerEvents(new CraftBlocks(this), this);
        getServer().getPluginManager().registerEvents(new SkillMenu(this, abilityManager), this);
        getServer().getPluginManager().registerEvents(abilityManager, this);


    }

    @Override
    public void onDisable()
    {
        // Plugin shutdown logic
    }
}
