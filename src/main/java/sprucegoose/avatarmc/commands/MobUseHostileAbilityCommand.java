package sprucegoose.avatarmc.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.abilities.AbilityManager;

import java.util.UUID;

public class MobUseHostileAbilityCommand implements CommandExecutor
{
    JavaPlugin plugin;
    AbilityManager abilityManager;

    public MobUseHostileAbilityCommand(JavaPlugin plugin, AbilityManager abilityManager)
    {
        this.plugin = plugin;
        this.abilityManager = abilityManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings)
    {
        LivingEntity caster;
        Ability ability;
        Player target; // assertion: mobs can only target players
        if(strings.length == 3)
        {
            caster = (LivingEntity)plugin.getServer().getEntity(UUID.fromString(strings[0]));
            target = plugin.getServer().getPlayer(strings[1]);
            ability = abilityManager.getAbilityFromString(strings[2]);
        }
        else
        {
            sender.sendMessage("Invalid use of command");
            return true;
        }

        if (target != null && ability != null)
        {
            ability.doHostileAbilityAsMob(plugin, caster, target);
        }
        else
        {
            plugin.getLogger().warning(caster + " tried to use ["+ strings[1] + "] on ["+ strings[0] + "], but failed.");
        }
        return true;
    }
}