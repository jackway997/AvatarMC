package sprucegoose.avatarmc.utils;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class BendingManager
{
    private ArrayList<String> abilities;
    private JavaPlugin plugin;

    public BendingManager(JavaPlugin plugin)
    {
        this.plugin = plugin;
    }

    private void registerAbility(String ability)
    {
        if (!this.abilities.contains(ability))
            this.abilities.add(ability);
    }

    private void registerAbilities()
    {
        registerAbility(BendingAbilities.waterBendKey);
        registerAbility(BendingAbilities.airBlastKey);
    }

    public ArrayList<String> getAbilities()
    {
        return abilities;
    }

    //convert to store bending player instead of list of players that know skill
}
