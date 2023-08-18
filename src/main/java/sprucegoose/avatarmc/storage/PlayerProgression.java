package sprucegoose.avatarmc.storage;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public class PlayerProgression
{
    MySQL db;
    JavaPlugin plugin;
    Logger logger;

    private final Map<UUID, Set<String>> PLAYER_ABILITIES = new HashMap<>();

    public PlayerProgression(JavaPlugin plugin, MySQL db)
    {
        this.plugin = plugin;
        logger = plugin.getLogger();
        this.db = db;
        this.setup();
    }

    public void setup() {
        // Create pk_statKeys table.
        if (!db.tableExists("player_abilities"))
        {
            logger.info("Creating player_abilities table");

            String query = "CREATE TABLE player_abilities (uuid VARCHAR(36), ability VARCHAR(32), player_name VARCHAR(36), PRIMARY KEY (uuid, ability));";
            db.modifyQuery(query, false);
        }
    }

    public void load(final UUID uuid) {
        this.PLAYER_ABILITIES.put(uuid, new HashSet<>());
        try (ResultSet rs = db.readQuery("SELECT * FROM player_abilities WHERE uuid = '" + uuid.toString() +"';"))
        {
            while (rs.next())
            {
                this.PLAYER_ABILITIES.get(uuid).add(rs.getString("ability"));
                logger.info("loaded into memory");
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public Set<String> getAbilities(Player player)
    {
        return getAbilities(player.getUniqueId());
    }

    public Set<String> getAbilities(final UUID uuid) {
        // If the player is offline, pull value from database.
        if (!this.PLAYER_ABILITIES.containsKey(uuid)) {
            try (ResultSet rs = db.readQuery("SELECT * FROM player_abilities WHERE uuid = '" + uuid.toString() +"';")) {
                Set<String> abilities = new HashSet<>();
                while (rs.next()) {
                    abilities.add(rs.getString("ability"));
                }
                logger.info("retrieved abilities using database");
                return abilities;
            } catch (final SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
        logger.info("retrieved abilities using memory");
        return this.PLAYER_ABILITIES.get(uuid);
    }

    // Create seperate method for offline player
    public void giveAbility(final UUID uuid, String ability, final boolean async) {
        if (this.PLAYER_ABILITIES.containsKey(uuid)) //Only add to Map if player is loaded in
        {
            this.PLAYER_ABILITIES.get(uuid).add(ability);
        }

        // Add entry into database regardless
        try (ResultSet rs = db.readQuery("SELECT * FROM player_abilities WHERE uuid = '" + uuid.toString() + "' AND ability = '" + ability + "';")) {
            if (!rs.next()) {
                String playerName = plugin.getServer().getOfflinePlayer(uuid).getName();
                db.modifyQuery("INSERT INTO player_abilities (uuid, player_name, ability) VALUES ('" + uuid + "', '"+ playerName + "', '" + ability + "');", async);
                logger.info(playerName +" learnt "+ ability +".");
            } else {
                logger.warning("Tried to add "+ ability +"to player with uuid="+ uuid.toString() +", but they already have it!");
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    // Create seperate method for offline player
    public void removeAbility(final UUID uuid, String ability, final boolean async) {
        if (this.PLAYER_ABILITIES.containsKey(uuid)) //Only add to Map if player is loaded in
            this.PLAYER_ABILITIES.get(uuid).remove(ability);

        // Remove entry from database regardless
        try (ResultSet rs = db.readQuery("SELECT * FROM player_abilities WHERE uuid = '" + uuid.toString() + "' AND ability = '" + ability +"';")) {
            if (rs.next()) {
                logger.info("removing ability with query");
                String playerName = plugin.getServer().getOfflinePlayer(uuid).getName();
                db.modifyQuery("DELETE FROM player_abilities WHERE uuid = '" + uuid + "' AND ability = '" + ability + "';", async);
                logger.info(playerName +" unlearnt "+ ability +".");
            } else {
                logger.warning("Tried to remove "+ ability +"from player with uuid="+ uuid.toString() +", but they didn't have it!");
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public void unload(final UUID uuid) {
        this.PLAYER_ABILITIES.remove(uuid);
    }
}

