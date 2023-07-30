package sprucegoose.avatarmc.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.listeners.SkillMenu;

public class TestCommand2 implements CommandExecutor
{
    JavaPlugin plugin;
    SkillMenu skillMenu;

    public TestCommand2(JavaPlugin plugin, SkillMenu skillMenu)
    {
        this.plugin = plugin;
        this.skillMenu = skillMenu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings)
    {
        if (sender instanceof Player player)
        {
            skillMenu.openInventory(plugin, player);
        }
        return true;
    }
}
