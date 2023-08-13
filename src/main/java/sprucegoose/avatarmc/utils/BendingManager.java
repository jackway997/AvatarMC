package sprucegoose.avatarmc.utils;

import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.abilities.AirBlast;
import sprucegoose.avatarmc.abilities.EarthBend;
import sprucegoose.avatarmc.abilities.WaterBend;

import java.util.ArrayList;

public class BendingManager
{
    private final ArrayList<Ability> abilities;
    private final JavaPlugin plugin;

    public BendingManager(JavaPlugin plugin)
    {
        this.plugin = plugin;
        abilities = new ArrayList<Ability>();
        registerAbilities();
    }

    private void registerAbility(Ability ability)
    {
        if (!this.abilities.contains(ability))
            this.abilities.add(ability);
    }

    private void registerAbilities()
    {
        registerAbility(new WaterBend(plugin));
        registerAbility(new AirBlast(plugin));
        registerAbility(new EarthBend(plugin));
    }

    public ArrayList<Ability> getAbilities()
    {
        return abilities;
    }

    //convert to store bending player instead of list of players that know skill
}
