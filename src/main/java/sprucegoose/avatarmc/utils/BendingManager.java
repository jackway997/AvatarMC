package sprucegoose.avatarmc.utils;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.abilities.AirBlast;
import sprucegoose.avatarmc.abilities.EarthBend;
import sprucegoose.avatarmc.abilities.WaterBend;
import sprucegoose.avatarmc.storage.PlayerProgression;

import java.util.*;

public class BendingManager implements Listener
{
    private final Set<Ability> abilities = new HashSet<>();
    private final JavaPlugin plugin;
    private final PlayerProgression pp;
    private final HashMap<String, Ability> abilityMatrix = new HashMap<>();

    public BendingManager(JavaPlugin plugin, PlayerProgression pp)
    {
        this.plugin = plugin;
        this.pp = pp;
        registerAbilities();
    }

    private void registerAbilities()
    {
        registerAbility(new WaterBend(plugin));
        registerAbility(new AirBlast(plugin));
        registerAbility(new EarthBend(plugin));
    }

    private void registerAbility(Ability ability)
    {
        this.abilities.add(ability);
        this.abilityMatrix.put(ability.getClass().getSimpleName(), ability);
    }

    public Set<Ability> getAbilities()
    {
        return abilities;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e)
    {
        this.pp.load(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerLeaveEvent(PlayerQuitEvent e)
    {
        this.pp.unload(e.getPlayer().getUniqueId());
    }

    public Set<Ability> getPlayerAbilities(Player player)
    {
        Set<Ability> outSet = new HashSet<>();

        for (String ability : pp.getAbilities(player))
        {
            if (abilityMatrix.containsKey(ability))
                outSet.add(abilityMatrix.get(ability));
            // else
            //    plugin.getLogger().warning("Player has ability:" + ability + " which does not exist!");
        }
        return outSet;
    }

}
