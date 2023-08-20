package sprucegoose.avatarmc.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.abilities.AbilityManager;

public class RemoveAbilityCommand implements CommandExecutor
{
    JavaPlugin plugin;
    AbilityManager abilityManager;

    public RemoveAbilityCommand(JavaPlugin plugin, AbilityManager abilityManager)
    {
        this.plugin = plugin;
        this.abilityManager = abilityManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings)
    {
        Player player;
        String ability;
        if(strings.length > 1)
        {
            player = plugin.getServer().getPlayer(strings[0]);
            ability = strings[1];
        }
        else if (sender instanceof Player)
        {
            player = (Player)sender;
            ability = strings[0];
        }
        else
        {
            sender.sendMessage("Invalid use of command");
            return true;
        }

        if (player != null)
        {
            if (!abilityManager.removeAbility(player, ability)) {
                sender.sendMessage("Invalid ability. Valid abilities are " +
                        abilityManager.getAbilityStringList());
            }
        }
        else
        {
            sender.sendMessage("player not found");
        }
        return true;
    }
}