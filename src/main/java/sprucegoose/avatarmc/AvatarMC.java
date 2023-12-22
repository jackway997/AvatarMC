package sprucegoose.avatarmc;

import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.abilities.*;
import sprucegoose.avatarmc.abilities.water.WaterEffects;
import sprucegoose.avatarmc.commands.*;
import sprucegoose.avatarmc.listeners.SkillMenu;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.storage.MySQL;
import sprucegoose.avatarmc.storage.AbilityStorage;
import sprucegoose.avatarmc.storage.ProgressionStorage;

public final class AvatarMC extends JavaPlugin
{
    private MySQL db;
    private AbilityStorage abilityStorage;
    private ProgressionStorage progressionStorage;
    private AbilityManager abilityManager;
    private ProgressionManager progressionManager;
    private SkillMenu skillMenuManager;
    private RegionProtectionManager regionProtectionManager;
    private WaterEffects waterEffects;
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

        abilityStorage = new AbilityStorage(this, db);
        progressionStorage = new ProgressionStorage(this, db);

        waterEffects = new WaterEffects(this);
        regionProtectionManager = new RegionProtectionManager(this);
        progressionManager = new ProgressionManager(this, progressionStorage);
        abilityManager = new AbilityManager(this, abilityStorage, progressionManager, regionProtectionManager,
                waterEffects);
        skillMenuManager = new SkillMenu(this, abilityManager);


        // Plugin startup logic
        // Register commands
        getCommand("test").setExecutor(new TestCommand(this, abilityManager));
        getCommand("test2").setExecutor(new TestCommand2(this, skillMenuManager));
        getCommand("remove_ability").setExecutor(new RemoveAbilityCommand(this, abilityManager));
        getCommand("give_ability").setExecutor(new GiveAbilityCommand(this, abilityManager));
        getCommand("set_bender").setExecutor(new SetBenderCommand(this, progressionManager));
        getCommand("remove_bender").setExecutor(new RemoveBenderCommand(this, progressionManager));
        getCommand("test3").setExecutor(new TestCommand3(this, waterEffects));

        // Register ability listeners
        for ( Ability ability : abilityManager.getAbilities())
        {
            getServer().getPluginManager().registerEvents(ability, this);
        }
        // Register Misc listeners
        getServer().getPluginManager().registerEvents(new SkillMenu(this, abilityManager), this);
        getServer().getPluginManager().registerEvents(abilityManager, this);
        getServer().getPluginManager().registerEvents(waterEffects, this);
    }

    @Override
    public void onDisable()
    {
        // Plugin shutdown logic
    }
}
