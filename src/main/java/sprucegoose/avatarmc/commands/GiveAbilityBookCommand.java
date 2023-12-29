package sprucegoose.avatarmc.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.abilities.AbilityManager;

public class GiveAbilityBookCommand implements CommandExecutor
{
    JavaPlugin plugin;
    AbilityManager abilityManager;

    public GiveAbilityBookCommand(JavaPlugin plugin, AbilityManager abilityManager)
    {
        this.plugin = plugin;
        this.abilityManager = abilityManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings)
    {
        Player player;
        String ability;
        String source;
        if(strings.length == 3)
        {
            player = plugin.getServer().getPlayer(strings[0]);
            ability = strings[1];
            source = strings[2];
        }
        else
        {
            sender.sendMessage("Invalid use of command");
            return true;
        }

        if (player != null)
        {
            if (!abilityManager.giveAbilityBook(player, ability)) {
                sender.sendMessage("Invalid ability. Valid abilities are " +
                        abilityManager.getAbilityStringList());
            }
            else
            {
                player.sendMessage(abilityManager.getAbilityColor(ability) + "" +ChatColor.BOLD + "" + ChatColor.ITALIC +
                        source +" dropped "+ abilityManager.abilityIDtoName(ability) + "!");
            }
        }
        else
        {
            sender.sendMessage("player not found");
        }
        return true;
    }
}