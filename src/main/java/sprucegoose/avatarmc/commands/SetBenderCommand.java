package sprucegoose.avatarmc.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.abilities.ProgressionManager;
import sprucegoose.avatarmc.storage.ProgressionStorage;

public class SetBenderCommand implements CommandExecutor {
    JavaPlugin plugin;
    ProgressionManager progressionManager;

    public SetBenderCommand(JavaPlugin plugin, ProgressionManager progressionManager)
    {
        this.plugin = plugin;
        this.progressionManager = progressionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings)
    {
        Player player;
        String type;
        if(strings.length > 1)
        {
            player = plugin.getServer().getPlayer(strings[0]);
            type = strings[1];
        }
        else if (sender instanceof Player)
        {
            player = (Player)sender;
            type = strings[0];
        }
        else
        {
            sender.sendMessage("Invalid use of command");
            return true;
        }

        if (player != null)
        {
            if (!progressionManager.setBenderType(player, type))
            {
                sender.sendMessage("Invalid bending type. Valid types are "+
                        progressionManager.getBenderTypeStringList());
            }
        }
        else
        {
            sender.sendMessage("player not found");
        }
        return true;
    }
}
