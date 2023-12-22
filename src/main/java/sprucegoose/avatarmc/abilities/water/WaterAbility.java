package sprucegoose.avatarmc.abilities.water;

import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.region.RegionProtectionManager;

public abstract class WaterAbility extends Ability
{
    WaterEffects waterEffects;

    public WaterAbility(JavaPlugin plugin,
                        RegionProtectionManager regProtManager,
                        WaterEffects waterEffects,
                        ABILITY_LEVEL level)
    {
        super(plugin, regProtManager, ELEMENT_TYPE.water, level);
        this.waterEffects = waterEffects;
    }
}
