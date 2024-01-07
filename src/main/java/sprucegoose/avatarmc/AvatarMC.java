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
        progressionManager.setAbilityManager(abilityManager); // dodgey, lowelle
        skillMenuManager = new SkillMenu(this, abilityManager, progressionManager);

        // Plugin startup logic
        // Register commands
        getCommand("test").setExecutor(new TestCommand(this, abilityManager));
        getCommand("skills").setExecutor(new SkillsMenuCommand(this, skillMenuManager));
        getCommand("remove_ability").setExecutor(new RemoveAbilityCommand(this, abilityManager));
        getCommand("give_ability").setExecutor(new GiveAbilityCommand(this, abilityManager));
        getCommand("give_ability_book").setExecutor(new GiveAbilityBookCommand(this, abilityManager));
        getCommand("mob_use_hostile_ability").setExecutor(new MobUseHostileAbilityCommand(this, abilityManager));
        getCommand("set_bender").setExecutor(new SetBenderCommand(this, progressionManager));
        getCommand("remove_bender").setExecutor(new RemoveBenderCommand(this, progressionManager));
        getCommand("give_player_exp").setExecutor(new GivePlayerExp(this, progressionManager));
        getCommand("test4").setExecutor(new TestCommand4(this, progressionManager));

        // Register ability listeners
        for ( Ability ability : abilityManager.getAbilities())
        {
            getServer().getPluginManager().registerEvents(ability, this);
        }
        // Register Misc listeners
        getServer().getPluginManager().registerEvents(skillMenuManager, this);
        getServer().getPluginManager().registerEvents(abilityManager, this);
        getServer().getPluginManager().registerEvents(waterEffects, this);
        getServer().getPluginManager().registerEvents(progressionManager, this);
    }

    @Override
    public void onDisable()
    {
        // Plugin shutdown logic
    }
}
