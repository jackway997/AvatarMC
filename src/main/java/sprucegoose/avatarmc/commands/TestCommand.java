package sprucegoose.avatarmc.commands;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.abilities.AbilityManager;
import sprucegoose.avatarmc.utils.ImageParticles;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.bukkit.Color;
import java.awt.*;

public class TestCommand implements CommandExecutor
{
    JavaPlugin plugin;
    AbilityManager abilityManager;
    public TestCommand(JavaPlugin plugin, AbilityManager abilityManager)
    {
        this.plugin = plugin;
        this.abilityManager = abilityManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings)
    {
        if (sender instanceof Player player)
        {
            for ( Ability ability : abilityManager.getAbilities())
            {
                ItemStack skillBook = ability.getSkillBookItem(plugin);
                player.getInventory().addItem(skillBook);
            }
        }

        return true;
    }


}
