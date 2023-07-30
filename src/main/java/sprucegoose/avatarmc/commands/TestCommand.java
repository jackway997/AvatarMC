package sprucegoose.avatarmc.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.abilities.AirBlast;
import sprucegoose.avatarmc.abilities.WaterBend;

public class TestCommand implements CommandExecutor
{
    JavaPlugin plugin;
    public TestCommand(JavaPlugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings)
    {
        if (sender instanceof Player player)
        {
            ItemStack skill = (new WaterBend(plugin)).getAbilityItem(plugin, player);
            player.getInventory().addItem(skill);
            skill = (new AirBlast(plugin)).getAbilityItem(plugin, player);
            player.getInventory().addItem(skill);

        }
        return true;
    }
}
