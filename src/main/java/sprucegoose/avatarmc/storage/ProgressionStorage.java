package sprucegoose.avatarmc.storage;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public class ProgressionStorage
{
    MySQL db;
    JavaPlugin plugin;
    Logger logger;

    private final Map<UUID, String> PLAYER_BENDER_TYPES = new HashMap<>();

    public ProgressionStorage(JavaPlugin plugin, MySQL db)
    {
        this.plugin = plugin;
        logger = plugin.getLogger();
        this.db = db;
        this.setup();
    }

    public void setup() {
        // Create player_progression table.
        if (!db.tableExists("player_progression"))
        {
            logger.info("Creating player_progression table");

            String query = "CREATE TABLE player_progression (uuid VARCHAR(36), bend_type VARCHAR(10), player_name VARCHAR(36), PRIMARY KEY (uuid));";
            db.modifyQuery(query, false);
        }
    }

    public void load(final UUID uuid) {
        try (ResultSet rs = db.readQuery("SELECT * FROM player_progression WHERE uuid = '" + uuid.toString() +"';"))
        {
            while (rs.next())
            {
                String type = rs.getString("bend_type");
                if (type != null)
                    this.PLAYER_BENDER_TYPES.put(uuid,type);
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public String getBenderType(final UUID uuid) {
        // If the player is offline, pull value from database.
        if (!this.PLAYER_BENDER_TYPES.containsKey(uuid)) {
            try (ResultSet rs = db.readQuery("SELECT * FROM player_progression WHERE uuid = '" + uuid.toString() +"';")) {
                if (rs.next()) {
                    return rs.getString("bend_type");
                }
            } catch (final SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
        return this.PLAYER_BENDER_TYPES.get(uuid);
    }

    public void setBenderType(final UUID uuid, String type, final boolean async) {
        if (this.PLAYER_BENDER_TYPES.containsKey(uuid)) //Only add to Map if player is loaded in
        {
            this.PLAYER_BENDER_TYPES.put(uuid, type);
        }

        // Add entry into database regardless
        String playerName = plugin.getServer().getOfflinePlayer(uuid).getName();
        try (ResultSet rs = db.readQuery("SELECT * FROM player_progression WHERE uuid = '" + uuid.toString() + "';")) {
            if (!rs.next())
            {
                db.modifyQuery("INSERT INTO player_progression (uuid, player_name, bend_type) VALUES ('" + uuid + "', '" + playerName + "', '" + type + "');", async);
                logger.info(playerName + " bending type set to " + type + "!");
            }
            else if(!rs.getString("bend_type").equals(type))
            {
                db.modifyQuery("UPDATE player_progression SET bend_type = '"+ type +"' WHERE uuid = '" + uuid + "';", async);
                logger.info(playerName + " bending type set to " + type + "!");
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeBenderType(final UUID uuid, final boolean async) {
        // Remove from loaded players map if it is loaded in
        this.PLAYER_BENDER_TYPES.remove(uuid);

        // Remove entry from database regardless
        String playerName = plugin.getServer().getOfflinePlayer(uuid).getName();
        try (ResultSet rs = db.readQuery("SELECT * FROM player_progression WHERE uuid = '" + uuid.toString() + "';")) {
            if (rs.next()) {

                db.modifyQuery("DELETE FROM player_abilities WHERE uuid = ';", async);
                logger.info(playerName +" is no longer a bender!");
            } else {
                logger.warning("Tried to remove bending from"+ playerName +", but they weren't a bender!");
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public void unload(final UUID uuid) {
        this.PLAYER_BENDER_TYPES.remove(uuid);
    }
}


