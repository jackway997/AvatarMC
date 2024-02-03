package sprucegoose.avatarmc.configuration;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigManager {


    public static Config presetConfig;
    public static Config defaultConfig;
    public static Config languageConfig;

    public ConfigManager(JavaPlugin plugin) {
        presetConfig = new Config(plugin, new File("presets.yml"));
        defaultConfig = new Config(plugin, new File("config.yml"));
        languageConfig = new Config(plugin, new File("language.yml"));
        configCheck(ConfigType.DEFAULT);
        configCheck(ConfigType.LANGUAGE);
        configCheck(ConfigType.PRESETS);
    }

    public static void configCheck(final ConfigType type) {
        FileConfiguration config;
        if (type == ConfigType.PRESETS) {
            config = presetConfig.get();

            final ArrayList<String> abilities = new ArrayList<String>();

            config.addDefault("Example", abilities);

            presetConfig.save();
        } else if (type == ConfigType.LANGUAGE) {
            config = languageConfig.get();

            /*
            config.addDefault("Chat.Enable", !hasChatPlugin());
            config.addDefault("Chat.Format", "<name>: <message>");
            config.addDefault("Chat.Colors.Avatar", "DARK_PURPLE");
            config.addDefault("Chat.Colors.Air", "GRAY");
            config.addDefault("Chat.Colors.AirSub", "DARK_GRAY");
            config.addDefault("Chat.Colors.Spiritual", "#cab0ff");
            config.addDefault("Chat.Colors.Flight", "#dbf5ff");
            config.addDefault("Chat.Colors.Water", "AQUA");
            config.addDefault("Chat.Colors.WaterSub", "DARK_AQUA");
            config.addDefault("Chat.Colors.Blood", "#a30010");
            config.addDefault("Chat.Colors.Ice", "#99f5ff");
            config.addDefault("Chat.Colors.Plant", "#008048");
            config.addDefault("Chat.Colors.Healing", "#36d2e3");
            config.addDefault("Chat.Colors.Earth", "GREEN");
            config.addDefault("Chat.Colors.EarthSub", "DARK_GREEN");
            config.addDefault("Chat.Colors.Lava", "#c73800");
            config.addDefault("Chat.Colors.Metal", "#c7c5c5");
            config.addDefault("Chat.Colors.Sand", "#ffdc82");
            config.addDefault("Chat.Colors.Fire", "RED");
            config.addDefault("Chat.Colors.FireSub", "DARK_RED");
            config.addDefault("Chat.Colors.BlueFire", "#1ac5fd");
            config.addDefault("Chat.Colors.Combustion", "#690213");
            config.addDefault("Chat.Colors.Lightning", "#820d0d");
            config.addDefault("Chat.Colors.Chi", "GOLD");
            config.addDefault("Chat.Colors.ChiSub", "YELLOW");
            config.addDefault("Chat.Branding.JoinMessage.Enabled", true);
            config.addDefault("Chat.Branding.Color", "GOLD");
            config.addDefault("Chat.Branding.Borders.TopBorder", "");
            config.addDefault("Chat.Branding.Borders.BottomBorder", "");
            config.addDefault("Chat.Branding.ChatPrefix.Prefix", "");
            config.addDefault("Chat.Branding.ChatPrefix.Suffix", " \u00BB ");
            config.addDefault("Chat.Branding.ChatPrefix.Main", "ProjectKorra");
            config.addDefault("Chat.Branding.ChatPrefix.Hover", "Bending brought to you by ProjectKorra!\\nClick for more info.");
            config.addDefault("Chat.Branding.ChatPrefix.Click", "https://projectkorra.com");

            config.addDefault("Chat.Prefixes.Air", "[Air]");
            config.addDefault("Chat.Prefixes.Earth", "[Earth]");
            config.addDefault("Chat.Prefixes.Fire", "[Fire]");
            config.addDefault("Chat.Prefixes.Water", "[Water]");
            config.addDefault("Chat.Prefixes.Chi", "[Chi]");
            config.addDefault("Chat.Prefixes.Avatar", "[Avatar]");
            config.addDefault("Chat.Prefixes.Nonbender", "[Nonbender]");

            config.addDefault("Board.Title", "&lAbilities");
            config.addDefault("Board.Prefix.Text", "> ");
            config.addDefault("Board.Prefix.SelectedColor", ChatColor.WHITE.getName());
            config.addDefault("Board.Prefix.NonSelectedColor", ChatColor.DARK_GRAY.getName());
            config.addDefault("Board.EmptySlot", "&8-- Slot {slot_number} --");
            config.addDefault("Board.MiscSeparator", "  ----------");

            if (!config.contains("Board.Extras")) {
                config.addDefault("Board.Extras.RaiseEarthWall", ChatColor.GREEN.getName());
                config.addDefault("Board.Extras.SurgeWave", ChatColor.AQUA.getName());
            }

            config.addDefault("Extras.Water.NightMessage", "Your waterbending has become empowered due to the moon rising.");
            config.addDefault("Extras.Water.DayMessage", "You feel the empowering of your waterbending subside as the moon sets.");
            config.addDefault("Extras.Fire.NightMessage", "You feel the empowering of your firebending subside as the sun sets.");
            config.addDefault("Extras.Fire.DayMessage", "You feel the strength of the rising sun empower your firebending.");
            */

            config.addDefault("Commands.NoPermission", "You do not have permission to do that.");
            config.addDefault("Commands.MustBePlayer", "You must be a player to perform this action.");
            config.addDefault("Commands.ProperUsage", "Proper Usage: {command}");


            languageConfig.save();
        } else if (type == ConfigType.DEFAULT) {
            config = defaultConfig.get();

            final ArrayList<String> earthBlocks = new ArrayList<String>();

            earthBlocks.add("#base_stone_nether"); // added in 1.16.2
            earthBlocks.add("#base_stone_overworld"); // added in 1.16.2
            earthBlocks.add("MUD");
            earthBlocks.add("MUDDY_MANGROVE_ROOTS");
            earthBlocks.add("#coal_ores"); //These tags were only added in 1.17 and above
            earthBlocks.add("#diamond_ores");
            earthBlocks.add("#emerald_ores");
            earthBlocks.add("#lapis_ores");
            earthBlocks.add("#redstone_ores");
            earthBlocks.add("CALCITE");
            earthBlocks.add("DRIPSTONE_BLOCK");
            earthBlocks.add("LARGE_AMETHYST_BUD");
            earthBlocks.add("MEDIUM_AMETHYST_BUD");
            earthBlocks.add("SMALL_AMETHYST_BUD");
            earthBlocks.add("DIRT_PATH"); // renamed from grass_path in 1.17
            earthBlocks.add("ROOTED_DIRT");

            earthBlocks.add("ANCIENT_DEBRIS");
            earthBlocks.add("CLAY");
            earthBlocks.add("COARSE_DIRT");
            earthBlocks.add("COBBLESTONE");
            earthBlocks.add("COBBLESTONE_SLAB");
            earthBlocks.add("DIRT");
            earthBlocks.add("GRASS_BLOCK");
            earthBlocks.add("GRAVEL");
            earthBlocks.add("MYCELIUM");
            earthBlocks.add("PODZOL");
            earthBlocks.add("STONE_SLAB");

            final ArrayList<String> metalBlocks = new ArrayList<String>();

            metalBlocks.add("#copper_ores");
            metalBlocks.add("#gold_ores");
            metalBlocks.add("#iron_ores");
            metalBlocks.add("COPPER_BLOCK");
            metalBlocks.add("CUT_COPPER");
            metalBlocks.add("CUT_COPPER_SLAB");
            metalBlocks.add("CUT_COPPER_STAIRS");
            metalBlocks.add("EXPOSED_COPPER");
            metalBlocks.add("EXPOSED_CUT_COPPER");
            metalBlocks.add("EXPOSED_CUT_COPPER_SLAB");
            metalBlocks.add("EXPOSED_CUT_COPPER_STAIRS");
            metalBlocks.add("OXIDIZED_COPPER");
            metalBlocks.add("OXIDIZED_CUT_COPPER");
            metalBlocks.add("OXIDIZED_CUT_COPPER_SLAB");
            metalBlocks.add("OXIDIZED_CUT_COPPER_STAIRS");
            metalBlocks.add("RAW_COPPER_BLOCK");
            metalBlocks.add("RAW_GOLD_BLOCK");
            metalBlocks.add("RAW_IRON_BLOCK");
            metalBlocks.add("WAXED_COPPER_BLOCK");
            metalBlocks.add("WAXED_CUT_COPPER");
            metalBlocks.add("WAXED_CUT_COPPER_SLAB");
            metalBlocks.add("WAXED_CUT_COPPER_STAIRS");
            metalBlocks.add("WAXED_EXPOSED_COPPER");
            metalBlocks.add("WAXED_EXPOSED_CUT_COPPER");
            metalBlocks.add("WAXED_EXPOSED_CUT_COPPER_SLAB");
            metalBlocks.add("WAXED_EXPOSED_CUT_COPPER_STAIRS");
            metalBlocks.add("WAXED_OXIDIZED_COPPER");
            metalBlocks.add("WAXED_OXIDIZED_CUT_COPPER");
            metalBlocks.add("WAXED_OXIDIZED_CUT_COPPER_SLAB");
            metalBlocks.add("WAXED_OXIDIZED_CUT_COPPER_STAIRS");
            metalBlocks.add("WAXED_WEATHERED_COPPER");
            metalBlocks.add("WAXED_WEATHERED_CUT_COPPER");
            metalBlocks.add("WAXED_WEATHERED_CUT_COPPER_SLAB");
            metalBlocks.add("WAXED_WEATHERED_CUT_COPPER_STAIRS");
            metalBlocks.add("WEATHERED_COPPER");
            metalBlocks.add("WEATHERED_CUT_COPPER");
            metalBlocks.add("WEATHERED_CUT_COPPER_SLAB");
            metalBlocks.add("WEATHERED_CUT_COPPER_STAIRS");

            metalBlocks.add("CHAIN");
            metalBlocks.add("GILDED_BLACKSTONE");
            metalBlocks.add("GOLD_BLOCK");
            metalBlocks.add("IRON_BLOCK");
            metalBlocks.add("NETHERITE_BLOCK");
            metalBlocks.add("NETHER_QUARTZ_ORE");
            metalBlocks.add("QUARTZ_BLOCK");

            final ArrayList<String> sandBlocks = new ArrayList<String>();
            sandBlocks.add("#sand");
            sandBlocks.add("RED_SANDSTONE");
            sandBlocks.add("RED_SANDSTONE_SLAB");
            sandBlocks.add("SANDSTONE");
            sandBlocks.add("SANDSTONE_SLAB");

            final ArrayList<String> iceBlocks = new ArrayList<String>();
            iceBlocks.add("#ice");

            final ArrayList<String> plantBlocks = new ArrayList<String>();
            plantBlocks.add("#bee_growables");
            plantBlocks.add("#flowers");
            plantBlocks.add("#leaves");
            plantBlocks.add("#saplings");

            plantBlocks.add("BROWN_MUSHROOM");
            plantBlocks.add("BROWN_MUSHROOM_BLOCK");
            plantBlocks.add("CACTUS");
            plantBlocks.add("CRIMSON_FUNGUS");
            plantBlocks.add("CRIMSON_ROOTS");
            plantBlocks.add("FERN");
            plantBlocks.add("GRASS");
            plantBlocks.add("LARGE_FERN");
            plantBlocks.add("LILY_PAD");
            plantBlocks.add("MELON");
            plantBlocks.add("MELON_STEM");
            plantBlocks.add("MUSHROOM_STEM");
            plantBlocks.add("NETHER_SPROUTS");
            plantBlocks.add("PUMPKIN");
            plantBlocks.add("PUMPKIN_STEM");
            plantBlocks.add("RED_MUSHROOM");
            plantBlocks.add("RED_MUSHROOM_BLOCK");
            plantBlocks.add("SUGAR_CANE");
            plantBlocks.add("TALL_GRASS");
            plantBlocks.add("TWISTING_VINES_PLANT");
            plantBlocks.add("VINE");
            plantBlocks.add("WARPED_FUNGUS");
            plantBlocks.add("WARPED_ROOTS");
            plantBlocks.add("WEEPING_VINES_PLANT");
            plantBlocks.add("BIG_DRIPLEAF");
            plantBlocks.add("HANGING_ROOTS");
            plantBlocks.add("MOSS_BLOCK");
            plantBlocks.add("MOSS_CARPET");
            plantBlocks.add("SMALL_DRIPLEAF");
            plantBlocks.add("SPORE_BLOSSOM");

            final ArrayList<String> snowBlocks = new ArrayList<>();
            snowBlocks.add("#snow"); // added in 1.17

            config.addDefault("Storage.engine", "MySQL");

//            config.addDefault("Storage.MySQL.host", "localhost");
//            config.addDefault("Storage.MySQL.port", 3306);
//            config.addDefault("Storage.MySQL.pass", "");
//            config.addDefault("Storage.MySQL.db", "minecraft");
//            config.addDefault("Storage.MySQL.user", "root");

            //config.addDefault("debug", false);

            // Ability Properties

            // Air
            config.addDefault("Abilities.Air.AirBlast.Enabled", true);
            config.addDefault("Abilities.Air.AirBlast.Cooldown", 10);
            config.addDefault("Abilities.Air.AirBlast.Range", 8);
            config.addDefault("Abilities.Air.AirBlast.HitRadius", 0.5);
            config.addDefault("Abilities.Air.AirBlast.KnockbackStrength", 3);
            config.addDefault("Abilities.Air.AirBlast.Damage", 2);

            config.addDefault("Abilities.Air.AirTribute.Enabled", true);
            config.addDefault("Abilities.Air.AirTribute.Cooldown", 10);
            config.addDefault("Abilities.Air.AirTribute.Duration", 3);

            config.addDefault("Abilities.Air.Cyclone.Enabled", true);
            config.addDefault("Abilities.Air.Cyclone.Cooldown", 30);
            config.addDefault("Abilities.Air.Cyclone.Duration", 10);
            config.addDefault("Abilities.Air.Cyclone.Radius", 5);
            config.addDefault("Abilities.Air.Cyclone.Damage", 3);
            config.addDefault("Abilities.Air.Cyclone.KnockbackStrength", 1);

            config.addDefault("Abilities.Air.FeatherFlight.Enabled", true);
            config.addDefault("Abilities.Air.FeatherFlight.Cooldown", 60);
            config.addDefault("Abilities.Air.FeatherFlight.JumpStrength", 4);

            config.addDefault("Abilities.Air.Stasis.Enabled", true);
            config.addDefault("Abilities.Air.Stasis.Cooldown", 60);
            config.addDefault("Abilities.Air.Stasis.Duration", 5);
            config.addDefault("Abilities.Air.Stasis.Range", 10);
            config.addDefault("Abilities.Air.Stasis.HitRadius", 0.5);
            config.addDefault("Abilities.Air.Stasis.MaxTravelStep", 0.3);

            //Earth
            config.addDefault("Abilities.Earth.BoulderToss.Enabled", true);
            config.addDefault("Abilities.Earth.BoulderToss.Cooldown", 10);
            config.addDefault("Abilities.Earth.BoulderToss.StompBlockDuration", 2);
            config.addDefault("Abilities.Earth.BoulderToss.Speed", 2.0);
            config.addDefault("Abilities.Earth.BoulderToss.ExplosionRadius", 2.0);
            config.addDefault("Abilities.Earth.BoulderToss.Damage", 8);

            config.addDefault("Abilities.Earth.EarthPrison.Enabled", true);
            config.addDefault("Abilities.Earth.EarthPrison.Cooldown", 60);
            config.addDefault("Abilities.Earth.EarthPrison.Range", 24);
            config.addDefault("Abilities.Earth.EarthPrison.HitRadius", 0.5);
            config.addDefault("Abilities.Earth.EarthPrison.Height", 6);
            config.addDefault("Abilities.Earth.EarthPrison.StandTime", 5);
            config.addDefault("Abilities.Earth.EarthPrison.CrumbleTime", 2);

            config.addDefault("Abilities.Earth.EarthTribute.Enabled", true);
            config.addDefault("Abilities.Earth.EarthTribute.Cooldown", 10);
            config.addDefault("Abilities.Earth.EarthTribute.Duration", 3);

            config.addDefault("Abilities.Earth.ShockWave.Enabled", true);
            config.addDefault("Abilities.Earth.ShockWave.Cooldown", 30);
            config.addDefault("Abilities.Earth.ShockWave.Range", 24);
            config.addDefault("Abilities.Earth.ShockWave.ShockRadius", 2);
            config.addDefault("Abilities.Earth.ShockWave.shockOffset", 1.5);
            config.addDefault("Abilities.Earth.ShockWave.KnockUpVelocity", 1.3);
            config.addDefault("Abilities.Earth.ShockWave.Damage", 8);

            // Fire
            config.addDefault("Abilities.Fire.Enchant.Enabled", true);
            config.addDefault("Abilities.Fire.Enchant.Cooldown", 30);
            config.addDefault("Abilities.Fire.Enchant.Duration", 20);
            config.addDefault("Abilities.Fire.Enchant.TrueBurnDuration", 8);
            config.addDefault("Abilities.Fire.Enchant.TickDamage", 2);

            config.addDefault("Abilities.Fire.Firebolt.Enabled", true);
            config.addDefault("Abilities.Fire.Firebolt.Cooldown", 3);
            config.addDefault("Abilities.Fire.Firebolt.Range", 50);
            config.addDefault("Abilities.Fire.Firebolt.HandFlameDuration", 2);
            config.addDefault("Abilities.Fire.Firebolt.BlastRadius", 0.5);
            config.addDefault("Abilities.Fire.Firebolt.CollisionRadius", 0.5);
            config.addDefault("Abilities.Fire.Firebolt.BurnDuration", 1);
            config.addDefault("Abilities.Fire.Firebolt.Damage", 5);

            config.addDefault("Abilities.Fire.Fireball.Enabled", true);
            config.addDefault("Abilities.Fire.Fireball.Cooldown", 30);
            config.addDefault("Abilities.Fire.Fireball.Range", 50);
            config.addDefault("Abilities.Fire.Fireball.HandFlameDuration", 5);
            config.addDefault("Abilities.Fire.Fireball.BlastRadius", 4);
            config.addDefault("Abilities.Fire.Fireball.CollisionRadius", 0.5);
            config.addDefault("Abilities.Fire.Fireball.BurnDuration", 5);
            config.addDefault("Abilities.Fire.Fireball.Damage", 12);

            defaultConfig.save();
        }
    }

    public static FileConfiguration getConfig() {
        return ConfigManager.defaultConfig.get();
    }

    private static boolean hasChatPlugin() {
        List<String> plugins = Arrays.asList("EssentialsChat", "VentureChat", "LPC", "ChatManager", "ChatControl", "DeluxeChat");

        return Arrays.stream(Bukkit.getPluginManager().getPlugins()).anyMatch(pl -> plugins.contains(pl.getName()));
    }
}

