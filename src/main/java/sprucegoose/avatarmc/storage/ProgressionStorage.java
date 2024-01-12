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
    private final Map<UUID, Long> PLAYER_EXP = new HashMap<>();
    private final Map<UUID, Long> PLAYER_EXP_DELTA = new HashMap<>();
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

            String query = "CREATE TABLE player_progression (uuid VARCHAR(36), bend_type VARCHAR(10), exp BIGINT(20), player_name VARCHAR(36), PRIMARY KEY (uuid));";
            db.modifyQuery(query, false);
        }
    }

    public void load(final UUID uuid) {
        this.PLAYER_EXP_DELTA.put(uuid, 0L); // set delta to 0 for this session
        try (ResultSet rs = db.readQuery("SELECT * FROM player_progression WHERE uuid = '" + uuid.toString() +"';"))
        {
            if (rs.next())
            {
                String type = rs.getString("bend_type");
                if (type != null)
                    this.PLAYER_BENDER_TYPES.put(uuid,type);
                long exp = rs.getLong("exp");
                if (exp != 0)
                {
                    this.PLAYER_EXP.put(uuid, exp);
                }
                else
                {
                    this.PLAYER_EXP.put(uuid, 1L);
                }
            }
            else
            {
                this.PLAYER_EXP.put(uuid, 1L);
                this.PLAYER_BENDER_TYPES.put(uuid,"none");
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

    public Long getExp(final UUID uuid)
    {
        if (!this.PLAYER_EXP.containsKey(uuid))
        {
            load(uuid); // try to re-load in the player from the db
        }
        // If the player is offline, pull value from database.
        if (!this.PLAYER_EXP.containsKey(uuid))
        {
            try (ResultSet rs = db.readQuery("SELECT * FROM player_progression WHERE uuid = '" + uuid.toString() +"';")) {
                if (rs.next()) {
                    return rs.getLong("exp");
                }
            } catch (final SQLException e) {
                e.printStackTrace();
            }
            return 0L;
        }
        return this.PLAYER_EXP.get(uuid) + this.PLAYER_EXP_DELTA.get(uuid);
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
                db.modifyQuery("INSERT INTO player_progression (uuid, player_name, exp, bend_type) VALUES ('" + uuid + "', '" + playerName + "', '" + 1L + "', '" + type + "');", async);
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


    public void addExp(UUID uuid, long delta)
    {
        if (!this.PLAYER_EXP.containsKey(uuid)) //Only add to Map if player is loaded in
        {
            load(uuid);
        }
        this.PLAYER_EXP.put(uuid, this.PLAYER_EXP.get((uuid))+ delta);
    }

    public void setExp(UUID uuid, long exp)
    {
        boolean async = false;

        this.PLAYER_EXP.put(uuid, exp);
        this.PLAYER_EXP_DELTA.put(uuid, 0L);

        // Add entry into database regardless
        String playerName = plugin.getServer().getOfflinePlayer(uuid).getName();
        try (ResultSet rs = db.readQuery("SELECT * FROM player_progression WHERE uuid = '" + uuid.toString() + "';")) {
            if (!rs.next())
            {
                db.modifyQuery("INSERT INTO player_progression (uuid, player_name, exp, bend_type) VALUES ('" + uuid + "', '" + playerName + "', '" + exp + "', '" + "none" + "');", async);
                logger.info(playerName + "'s exp set to " + exp + "!");
            }
            else if(!rs.getString("exp").equals(exp))
            {
                db.modifyQuery("UPDATE player_progression SET exp = '"+ exp +"' WHERE uuid = '" + uuid + "';", async);
                logger.info(playerName + "'s exp set to " + exp + "!");
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }
  
    public void unload(final UUID uuid)
    {
        plugin.getLogger().info("Unloading player");
        boolean async = false;
        this.PLAYER_BENDER_TYPES.remove(uuid);

        if(!this.PLAYER_EXP.containsKey(uuid) || !this.PLAYER_EXP_DELTA.containsKey(uuid))
        {
            plugin.getLogger().info("player exp not loaded into plugin so not pushing to db");
            return;
        }

        Long newExp = this.PLAYER_EXP.get(uuid) + this.PLAYER_EXP_DELTA.get(uuid);

        // Update exp in database
        String playerName = plugin.getServer().getOfflinePlayer(uuid).getName();
        try (ResultSet rs = db.readQuery("SELECT * FROM player_progression WHERE uuid = '" + uuid.toString() + "';")) {
            if (!rs.next())
            {
                db.modifyQuery("INSERT INTO player_progression (uuid, player_name, exp, bend_type) VALUES ('" + uuid + "', '" + playerName + "', '" + newExp + "', '" + "none" + "');", async);
                logger.info(playerName + "'s exp set to " + newExp + "!");
            }
            else //if(rs.getLong("exp") != 0)
            {
                db.modifyQuery("UPDATE player_progression SET exp = '"+ newExp +"' WHERE uuid = '" + uuid + "';", async);
                logger.info(playerName + "'s exp set to " + newExp + "!");
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        this.PLAYER_EXP.remove(uuid);
        this.PLAYER_EXP_DELTA.remove(uuid);
    }

    public void unloadAll()
    {
        for(Map.Entry<UUID, Long> entry : PLAYER_EXP_DELTA.entrySet())
        {
            unload(entry.getKey());
        }

    }


}


