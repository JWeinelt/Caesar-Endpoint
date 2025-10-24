package de.julianweinelt.caesar.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vdurmont.semver4j.Semver;
import de.julianweinelt.caesar.CaesarEndpoint;
import de.julianweinelt.caesar.storage.data.AccountStatus;
import de.julianweinelt.caesar.storage.data.PluginManager;
import de.julianweinelt.caesar.storage.data.User;
import de.julianweinelt.caesar.storage.data.UserManager;
import de.julianweinelt.caesar.web.PluginEntry;
import de.julianweinelt.caesar.web.PluginState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
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
            log.info("Using {} as a database.", data.getDatabaseName());
            PluginManager.getInstance().getData();
            UserManager.getInstance().getData();
            getAccountStatuses();
        } catch (SQLException e) {
            log.error("Failed to connect to MySQL database: {}", e.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("Could not load MySQL driver: {}", e.getMessage());
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

    protected PluginEntry getPlugin(UUID uuid) {
        Gson gson = new Gson();
        try {
            PreparedStatement pS = conn.prepareStatement("SELECT * FROM plugin_entries AS p " +
                    "LEFT JOIN accounts AS a ON p.author = a.AccountID WHERE uuid = ?");
            pS.setString(1, uuid.toString());
            ResultSet set = pS.executeQuery();
            if (set.next()) {
                PluginEntry entry = new PluginEntry();
                entry.setUniqueId(uuid);
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
                entry.setState(PluginState.valueOf(set.getString("State")));

                entry.setWaitingForAppoval(waitingForAppoval(uuid));

                return entry;
            }
        } catch (SQLException e) {
            log.error("Failed to get plugin: {}", e.getMessage());
        }
        return null;
    }

    public HashMap<UUID, List<String>> getUserPermissions() {
        checkConnection();

        HashMap<UUID, List<String>> permissions = new HashMap<>();
        try (PreparedStatement pS = conn.prepareStatement("SELECT AccountID, PermissionName FROM account_permissions")) {
            ResultSet set = pS.executeQuery();
            while (set.next()) {
                UUID u = UUID.fromString(set.getString(1));
                permissions.computeIfAbsent(u, list -> new ArrayList<>()).add(set.getString(1));
            }
        } catch (SQLException e) {
            log.error("Failed to get user permissions: {}", e.getMessage());
        }
        return permissions;
    }

    public int updatePluginState(UUID plugin, PluginState state) {
        checkConnection();

        try (PreparedStatement pS = conn.prepareStatement("UPDATE plugin_entries SET State = ? WHERE uuid = ?")) {
            pS.setString(1, state.name());
            pS.setString(2, plugin.toString());
            return pS.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to update plugin state: {}", e.getMessage());
            return 0;
        }
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

    public void updatePlugin(PluginEntry entry) {
        String sql = "UPDATE plugin_entries SET " +
                "name = ?, version = ?, author = ?, description = ?, description_long = ?, " +
                "compatible_versions = ?, downloads = ?, license = ?, tags = ?, source_code = ?, " +
                "sponsor_link = ?, wiki_link = ?, last_updated = ?, date_created = ?, rating = ?, screenshots = ? " +
                "WHERE uuid = ?";

        try (PreparedStatement pS = conn.prepareStatement(sql)) {
            pS.setString(1, entry.getName());
            pS.setString(2, entry.getVersion());
            pS.setString(3, entry.getAuthor());
            pS.setString(4, entry.getDescription());
            pS.setString(5, entry.getDescriptionLong());
            pS.setString(6, GSON.toJson(entry.getCompatibleVersions()));
            pS.setInt(7, entry.getDownloads());
            pS.setString(8, entry.getLicense());
            pS.setString(9, GSON.toJson(entry.getTags()));
            pS.setString(10, entry.getSourceCode());
            pS.setString(11, entry.getSponsorLink());
            pS.setString(12, entry.getWikiLink());
            pS.setTimestamp(13, new Timestamp(entry.getLastUpdated().getTime()));
            pS.setTimestamp(14, new Timestamp(entry.getDateCreated().getTime()));
            pS.setFloat(15, entry.getRating());
            pS.setString(16, GSON.toJson(entry.getScreenshots()));
            pS.setString(17, entry.getUniqueId().toString());

            int affectedRows = pS.executeUpdate();
            if (affectedRows == 0) {
                log.warn("No plugin entry updated (UUID not found): {}", entry.getUniqueId());
            } else {
                log.info("Plugin entry updated successfully: {}", entry.getUniqueId());
            }
        } catch (SQLException e) {
            log.error("Error while updating plugin: {}", e.getMessage(), e);
        }
    }


    private void getAccountStatuses() {
        checkConnection();

        try (PreparedStatement pS = conn.prepareStatement("SELECT * FROM account_status")) {
            ResultSet set = pS.executeQuery();
            while (set.next()) {
                AccountStatus.addStatus(new AccountStatus(UUID.fromString(set.getString(1)),
                        set.getString(2), set.getBoolean(3)));
            }
        } catch (SQLException e) {
            log.error("Error while getting account_statuses: {}", e.getMessage());
        }
    }

    public void setPluginWaitingApproval(UUID plugin, boolean approved) {
        checkConnection();

        String sql = (approved) ? "DELETE FROM plugins_awaiting_approval WHERE PluginID = ?"
                : "INSERT INTO plugins_awaiting_approval VALUES (?)";

        try (PreparedStatement pS = conn.prepareStatement(sql)) {
            pS.setString(1, plugin.toString());
            pS.execute();
        } catch (SQLException e) {
            log.error("Error while setting plugin waiting approval: {}", e.getMessage());
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
    public boolean waitingForAppoval(UUID plugin) {
        checkConnection();

        try (PreparedStatement pS = conn.prepareStatement("SELECT * FROM plugins_awaiting_approval WHERE PluginID = ?")) {
            pS.setString(1, plugin.toString());
            ResultSet set = pS.executeQuery();
            return set.next();
        } catch (SQLException e) {
            return true;
        }
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

    public List<User> getAccounts() {
        checkConnection();
        List<User> users = new ArrayList<>();

        try (PreparedStatement pS = conn.prepareStatement("SELECT AccountID, UserName, IsVerified, " +
                "AccountStatus, AccountCreated, eMail, PasswordHashed, Description, LastOnline FROM accounts")) {
            ResultSet set = pS.executeQuery();
            while (set.next()) {
                log.info("Loading account {}", set.getString("AccountID"));
                User u = new User(
                        set.getString(2),
                        set.getString(7),
                        set.getString(6),
                        UUID.fromString(set.getString(1)),
                        set.getString(8),
                        set.getBoolean(9),
                        set.getString(4).isBlank() ? null :
                                AccountStatus.getStatus(UUID.fromString(set.getString(4))),
                        set.getLong(5),
                        set.getLong(9)
                );
                users.add(u);
            }
        } catch (SQLException e) {
            log.error("Failed to get accounts: {}", e.getMessage());
        }
        return users;
    }

    public JsonObject getProfile(UUID uuid) {
        if (uuid == null) return new JsonObject();
        List<PluginEntry> pluginEntries = getPlugins();
        List<PluginEntry> userPlugins = new ArrayList<>();
        for (PluginEntry p : pluginEntries) if (p.getAuthor().equals(UserManager.getInstance().getUserByUUID(uuid).getUserName())) userPlugins.add(p);

        JsonObject o = new JsonObject();
        o.add("user", GSON.toJsonTree(UserManager.getInstance().getUserByUUID(uuid)));
        o.addProperty("followers", getFollowers(uuid));
        o.add("plugins", GSON.toJsonTree(userPlugins));
        o.add("badges", getProfileBadges(uuid));
        return o;
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

    public JsonArray getPluginComments(UUID plugin) {
        checkConnection();

        JsonArray array = new JsonArray();

        try (PreparedStatement pS = conn.prepareStatement("SELECT RecordID, PluginID, AuthorID, AuthorName, " +
                "Content, ThumbsUp, ThumbsDown, Creation FROM plugin_comments WHERE PluginID = ?")) {
            pS.setString(1, plugin.toString());

            ResultSet set = pS.executeQuery();
            while (set.next()) {
                JsonObject o = new JsonObject();
                o.addProperty("RecordID", set.getString(1));
                o.addProperty("PluginID", set.getString(2));
                o.addProperty("AuthorID", set.getString(3));
                o.addProperty("AuthorName", set.getString(4));
                o.addProperty("Content", set.getString(5));
                o.addProperty("ThumbsUp", set.getInt(6));
                o.addProperty("ThumbsDown", set.getInt(7));
                o.addProperty("Created", set.getLong(8));

                array.add(o);
            }
        } catch (SQLException e) {
            log.error("Failed to get plugin comments: {}", e.getMessage());
        }
        return array;
    }

    public void createComment(String pluginName, UUID author, String content) {
        checkConnection();

        UUID pluginID = getPluginID(pluginName);
        String authorName = UserManager.getInstance().getUserByUUID(author).getUserName();

        try (PreparedStatement pS = conn.prepareStatement("INSERT INTO plugin_comments " +
                "(PluginID, AuthorID, AuthorName, Content, Creation) VALUES (?, ?, ?, ?, UNIX_TIMESTAMP())")) {
            pS.setString(1, pluginID.toString());
            pS.setString(2, author.toString());
            pS.setString(3, authorName);
            pS.setString(4, content);

            pS.execute();
        } catch (SQLException e) {
            log.error("Failed to create comment: {}", e.getMessage());
        }
    }
}