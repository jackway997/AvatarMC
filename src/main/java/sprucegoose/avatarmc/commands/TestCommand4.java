package sprucegoose.avatarmc.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.abilities.ProgressionManager;

public class TestCommand4 implements CommandExecutor
{
    JavaPlugin plugin;
    ProgressionManager progressionManager;
    public TestCommand4(JavaPlugin plugin, ProgressionManager progressionManager)
    {
        this.plugin = plugin;
        this.progressionManager = progressionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        String newType;
        if (strings.length == 1) {
            newType = strings[0];
        } else {
            plugin.getLogger().info("Invalid use of command");
            return true;
        }

        if (sender instanceof Player player)
        {
            plugin.getLogger().info("pre player exp: " + String.valueOf(progressionManager.getExp(player)));
            progressionManager.reassignBenderType(player, ProgressionManager.stringToBenderType(newType));
            plugin.getLogger().info("post player exp: " + String.valueOf(progressionManager.getExp(player)));
        }
        else {
            plugin.getLogger().info("Invalid use of command");
        }
        return true;
    }
}
