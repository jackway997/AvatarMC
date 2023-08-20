package sprucegoose.avatarmc.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.abilities.ProgressionManager;

public class RemoveBenderCommand implements CommandExecutor {
    JavaPlugin plugin;
    ProgressionManager progressionManager;

    public RemoveBenderCommand(JavaPlugin plugin, ProgressionManager progressionManager)
    {
        this.plugin = plugin;
        this.progressionManager = progressionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings)
    {
        Player player;
        if(strings.length > 1)
        {
            player = plugin.getServer().getPlayer(strings[0]);
        }
        else if (sender instanceof Player)
        {
            player = (Player)sender;
        }
        else
        {
            sender.sendMessage("Invalid use of command");
            return true;
        }

        if (player != null)
        {
            progressionManager.removeBenderType(player);
        }
        else
        {
            sender.sendMessage("player not found");
        }
        return true;
    }
}
