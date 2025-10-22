package de.julianweinelt.caesar.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vdurmont.semver4j.Semver;
import de.julianweinelt.caesar.CaesarEndpoint;
import de.julianweinelt.caesar.web.PluginEntry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Getter
public class MySQL {
    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private Connection conn;

    private Configuration data;

    public static MySQL getInstance() {
        return CaesarEndpoint.getInstance().getMySQL();
    }

    public void connect() {
        data = LocalStorage.getInstance().getData();

        final String DRIVER = "com.mysql.cj.jdbc.Driver";
        final String PARAMETERS = "?useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&autoReconnect=true";
        final String URL = "jdbc:mysql://" + data.getDatabaseHost() + ":" + data.getDatabasePort() + "/" +
                           data.getDatabaseName() + PARAMETERS;
        final String USER = data.getDatabaseUser();
        final String PASSWORD = data.getDatabasePassword();

        try {
            Class.forName(DRIVER);

            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            log.info("Connected to MySQL database: {}", URL);
            conn.createStatement().execute("USE " + data.getDatabaseName());
        } catch (Exception e) {
            log.error("Failed to connect to MySQL database: {}", e.getMessage());
        }
    }


    public void disconnect() {
        try {
            log.info("Closing remaining transactions...");
            conn.commit();
            log.info("Closing connection...");
            conn.close();
        } catch (SQLException e) {
            log.error("Failed to disconnect from MySQL database: {}", e.getMessage());
        }
    }

    public void checkConnection() {
        try {
            if (conn == null || conn.isClosed()) connect();
        } catch (SQLException e) {
            log.error("Failed to check connection: {}", e.getMessage());
        }
    }

    public void createTables() {}

    public PluginEntry getPlugin(UUID uuid) {
        Gson gson = new Gson();
        try {
            PreparedStatement pS = conn.prepareStatement("SELECT * FROM plugin_entries AS p " +
                    "LEFT JOIN accounts AS a ON p.author = a.AccountID WHERE uuid = ?");
            pS.setString(1, uuid.toString());
            ResultSet set = pS.executeQuery();
            if (set.next()) {
                PluginEntry entry = new PluginEntry();

                entry.setName(set.getString("name"));
                entry.setVersion(set.getString("version"));
                entry.setAuthor(set.getString("UserName"));
                entry.setDescription(set.getString("description"));
                entry.setDescriptionLong(set.getString("description_long"));
                entry.setCompatibleVersions(gson.fromJson(set.getString("compatible_versions"), String[].class));
                entry.setDownloads(set.getInt("downloads"));
                entry.setLicense(set.getString("license"));
                entry.setTags(gson.fromJson(set.getString("tags"), String[].class));
                entry.setSourceCode(set.getString("source_code"));
                entry.setSponsorLink(set.getString("sponsor_link"));
                entry.setWikiLink(set.getString("wiki_link"));
                entry.setLastUpdated(set.getTimestamp("last_updated"));
                entry.setDateCreated(set.getTimestamp("date_created"));
                entry.setRating(getPluginRating(uuid));
                entry.setScreenshots(gson.fromJson(set.getString("screenshots"), UUID[].class));

                return entry;
            }
        } catch (SQLException e) {
            log.error("Failed to get plugin: {}", e.getMessage());
        }
        return null;
    }

    public void importPlugin(PluginEntry entry) {
        String sql = "INSERT INTO plugin_entries (" +
                "uuid, name, version, author, description, description_long, " +
                "compatible_versions, downloads, license, tags, source_code, " +
                "sponsor_link, wiki_link, last_updated, date_created, rating, screenshots" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Gson gson = new Gson();

        try (PreparedStatement pS = conn.prepareStatement(sql)) {
            pS.setString(1, entry.getUniqueId().toString());
            pS.setString(2, entry.getName());
            pS.setString(3, entry.getVersion());
            pS.setString(4, entry.getAuthor());
            pS.setString(5, entry.getDescription());
            pS.setString(6, entry.getDescriptionLong());
            pS.setString(7, gson.toJson(entry.getCompatibleVersions()));
            pS.setInt(8, entry.getDownloads());
            pS.setString(9, entry.getLicense());
            pS.setString(10, gson.toJson(entry.getTags()));
            pS.setString(11, entry.getSourceCode());
            pS.setString(12, entry.getSponsorLink());
            pS.setString(13, entry.getWikiLink());
            pS.setTimestamp(14, new Timestamp(entry.getLastUpdated().getTime()));
            pS.setTimestamp(15, new Timestamp(entry.getDateCreated().getTime()));
            pS.setFloat(16, entry.getRating());
            pS.setString(17, gson.toJson(entry.getScreenshots())); // UUID[] → JSON

            pS.executeUpdate();
        } catch (SQLException e) {
            log.error("Error while importing plugin: {}", e.getMessage());
        }
    }

    public int getFollowers(UUID uuid) {
        checkConnection();

        try {
            PreparedStatement pS = conn.prepareStatement("SELECT COUNT(DISTINCT AccountFollowing) AS Followers FROM account_followers WHERE AccountID = ?");
            pS.setString(1, uuid.toString());
            ResultSet set = pS.executeQuery();
            if (set.next()) return set.getInt(1);
        } catch (SQLException e) {
            log.error("Error while loading follower count: {}", e.getMessage());
        }
        return 0;
    }


    public List<PluginEntry> getPlugins() {
        List<PluginEntry> entries = new ArrayList<>();
        try {
            PreparedStatement pS = conn.prepareStatement("SELECT uuid FROM plugin_entries");
            ResultSet set = pS.executeQuery();
            while (set.next()) {
                PluginEntry entry = getPlugin(UUID.fromString(set.getString("uuid")));

                entries.add(entry);
            }
        } catch (SQLException e) {
            log.error("Failed to get plugins: {}", e.getMessage());
        }
        log.debug("Loaded {} plugins", entries.size());
        return entries;
    }

    public float getPluginRating(UUID uuid) {
        checkConnection();
        try {
            PreparedStatement pS = conn.prepareStatement("SELECT AVG(Rating) AS Rating FROM plugin_ratings WHERE PluginID = ?");
            pS.setString(1, uuid.toString());
            ResultSet set = pS.executeQuery();
            if (set.next()) return set.getFloat(1);
        } catch (SQLException e) {
            log.error("Failed to get plugin ratings: {}", e.getMessage());
        }
        return 0.0F;
    }

    public UUID getPluginID(String name) {
        try {
            PreparedStatement pS = conn.prepareStatement("SELECT uuid FROM plugin_entries WHERE name = ?");
            pS.setString(1, name);
            ResultSet set = pS.executeQuery();
            if (set.next()) {
                return UUID.fromString(set.getString("uuid"));
            }
        } catch (SQLException e) {
            log.error("Failed to get plugin uuid: {}", e.getMessage());
        }
        return null;
    }

    public void recordPluginFile(UUID plugin, Semver version, UUID fileID) {
        checkConnection();

        try {
            PreparedStatement pS = conn.prepareStatement("INSERT IGNORE INTO plugin_versions (PluginID, VersionName, " +
                    "VMajor, VMinor, VPatch, FileHash) VALUES (?, ?, ?, ?, ?, ?)");
            pS.setString(1, plugin.toString());
            pS.setString(2, version.getOriginalValue());
            pS.setInt(3, version.getMajor());
            pS.setInt(4, version.getMinor());
            pS.setInt(5, version.getPatch());
            pS.setString(6, fileID.toString());

            pS.execute();
        } catch (SQLException e) {
            log.error("Failed to record plugin file: {}", e.getMessage());
        }
    }

    public UUID getPluginFilePath(UUID plugin, Semver version) {
        checkConnection();

        try {
            PreparedStatement pS = conn.prepareStatement("SELECT FileHash FROM plugin_versions WHERE PluginID = ?" +
                    " AND VersionName = ?");
            pS.setString(1, plugin.toString());
            pS.setString(2, version.getOriginalValue());
            ResultSet set = pS.executeQuery();
            if (set.next()) return UUID.fromString(set.getString("FileHash"));
        } catch (SQLException e) {
            log.error("Failed to get plugin file: {}", e.getMessage());
        }
        return null;
    }

    public UUID createAccount(String eMail, String password, String username) {
        checkConnection();

        try {
            PreparedStatement pS = conn.prepareStatement("INSERT INTO accounts (AccountID, UserName, " +
                    "IsDeveloper, IsVerified, AccountStatus, AccountCreated, eMail, PasswordHashed)" +
                    " VALUES (?, ?, 0, 0, '', CURRENT_TIMESTAMP, ?, ?)");
            UUID uuid = UUID.randomUUID();
            pS.setString(1, uuid.toString());
            pS.setString(2, username);
            pS.setString(3, eMail);
            pS.setString(4, password);
            int rows = pS.executeUpdate();
            log.info("Created new account {}", uuid);
            log.info("Updated {} rows", rows);
            return uuid;
        } catch (SQLException e) {
            log.error("Failed to create account: {}", e.getMessage());
        }
        return null;
    }

    public JsonObject getAccount(String eMail) {
        checkConnection();

        try {
            PreparedStatement pS = conn.prepareStatement("SELECT * FROM accounts WHERE eMail = ?");
            pS.setString(1, eMail);
            ResultSet set = pS.executeQuery();
            if (set.next()) {
                JsonObject o = new JsonObject();
                o.addProperty("ID", set.getString("AccountID"));
                o.addProperty("username", set.getString("UserName"));
                o.addProperty("password", set.getString("PasswordHashed"));
                return o;
            }
        } catch (SQLException e) {
            log.error("Failed to get account by mail: {}", e.getMessage());
        }
        return null;
    }

    public JsonObject getProfile(UUID uuid) {
        checkConnection();
        List<PluginEntry> pluginEntries = getPlugins();
        List<PluginEntry> userPlugins = new ArrayList<>();
        for (PluginEntry p : pluginEntries) if (p.getAuthor().equals(uuid.toString())) userPlugins.add(p);

        try {
            PreparedStatement pS = conn.prepareStatement("SELECT * FROM accounts WHERE AccountID = ?");
            pS.setString(1, uuid.toString());
            ResultSet set = pS.executeQuery();
            if (set.next()) {
                JsonObject o = new JsonObject();
                o.addProperty("ID", set.getString("AccountID"));
                o.addProperty("username", set.getString("UserName"));
                o.addProperty("description", set.getString("Description"));
                o.addProperty("verified", set.getBoolean("IsVerified"));
                o.addProperty("developer", set.getBoolean("IsDeveloper"));
                o.addProperty("created", set.getString("AccountCreated"));
                o.addProperty("lastOnline", set.getString("LastOnline"));
                o.addProperty("followers", getFollowers(uuid));
                o.add("plugins", GSON.toJsonTree(userPlugins));
                o.add("badges", getProfileBadges(uuid));
                return o;
            }
        } catch (SQLException e) {
            log.error("Failed to get profile: {}", e.getMessage());
        }
        return null;
    }

    public void addFollower(UUID account, UUID follower) {
        checkConnection();

        try {
            PreparedStatement pS = conn.prepareStatement("INSERT INTO account_followers (AccountID, AccountFollowing) VALUES (?, ?)");
            pS.setString(1, account.toString());
            pS.setString(2, follower.toString());
            pS.execute();
        } catch (SQLException e) {
            log.error("Failed to add follower: {}", e.getMessage());
        }
    }

    public JsonArray getProfileBadges(UUID user) {
        checkConnection();
        JsonArray badges = new JsonArray();

        try {
            PreparedStatement pS = conn.prepareStatement("SELECT b.ID AS BadgeID, b.Name, b.Color " +
                    "FROM account_badge_assignment AS a " +
                    "LEFT OUTER JOIN badges AS b ON a.BadgeID = b.ID WHERE a.AccountID = ?");
            pS.setString(1, user.toString());
            ResultSet set = pS.executeQuery();
            while (set.next()) {
                JsonObject o = new JsonObject();
                o.addProperty("id", set.getString(1));
                o.addProperty("name", set.getString(2));
                o.addProperty("color", set.getString(3));
                badges.add(o);
            }
        } catch (SQLException e) {
            log.error("Failed to get profile badges: {}", e.getMessage());
        }
        return badges;
    }

    public void setImageType(UUID uuid, String imageType) {
        checkConnection();
        try {
            PreparedStatement pS = conn.prepareStatement("INSERT INTO image_meta (ImageID, DataType) VALUES (?, ?)");
            pS.setString(1, uuid.toString());
            pS.setString(2, imageType);
            pS.execute();
        } catch (SQLException e) {
            log.error("Failed to set image type: {}", e.getMessage());
        }
    }

    public String getImageType(UUID imageID) {
        checkConnection();

        try {
            PreparedStatement pS = conn.prepareStatement("SELECT DataType FROM image_meta WHERE ImageID = ?");
            pS.setString(1, imageID.toString());
            ResultSet set = pS.executeQuery();
            if (set.next()) return set.getString(1);
        } catch (SQLException e) {
            log.error("Failed to get image type: {}", e.getMessage());
        }
        return null;
    }
}