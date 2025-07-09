package de.julianweinelt.caesar.storage;

import com.google.gson.Gson;
import com.vdurmont.semver4j.Semver;
import de.julianweinelt.caesar.CaesarEndpoint;
import de.julianweinelt.caesar.web.PluginEntry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.UUID;

@Slf4j
@Getter
public class MySQL {
    private Connection conn;

    private Configuration data;

    public static MySQL getInstance() {
        return CaesarEndpoint.getInstance().getMySQL();
    }

    public boolean connect() {
        data = LocalStorage.getInstance().getData();

        final String DRIVER = "com.mysql.cj.jdbc.Driver";
        final String PARAMETEset = "?useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        final String URL = "jdbc:mysql://" + data.getDatabaseHost() + ":" + data.getDatabasePort() + "/" +
                           data.getDatabaseName() + PARAMETEset;
        final String USER = data.getDatabaseUser();
        final String PASSWORD = data.getDatabasePassword();

        try {
            Class.forName(DRIVER);

            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            log.info("Connected to MySQL database: {}", URL);
            conn.createStatement().execute("USE " + data.getDatabaseName());
            return true;
        } catch (Exception e) {
            log.error("Failed to connect to MySQL database: {}", e.getMessage());
        }
        return false;
    }


    public void disconnect() {
        try {
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

    public void createTables() {
        try {
            conn.createStatement().execute("""
            
                CREATE TABLE plugin_entries (
                uuid                CHAR(36) NOT NULL PRIMARY KEY,
                name                VARCHAR(255) NOT NULL,
                version             VARCHAR(50) NOT NULL,
                author              VARCHAR(255) NOT NULL,
                description         TEXT,
                description_long    TEXT,
                compatible_versions JSON,
                downloads           INT UNSIGNED DEFAULT 0,
                license             VARCHAR(100),
                tags                JSON,
                source_code         VARCHAR(2083),
                sponsor_link        VARCHAR(2083),
                wiki_link           VARCHAR(2083),
                last_updated        TIMESTAMP NULL DEFAULT NULL,
                date_created        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                rating              FLOAT DEFAULT 0,
                screenshots         JSON
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
            
            """);
        } catch (SQLException e) {
            log.error("Failed to create tables: {}", e.getMessage());
        }
    }

    public PluginEntry getPlugin(UUID uuid) {
        Gson gson = new Gson();
        try {
            PreparedStatement pS = conn.prepareStatement("SELECT * FROM plugin_entries WHERE uuid = ?");
            pS.setString(1, uuid.toString());
            ResultSet set = pS.executeQuery();
            if (set.next()) {
                PluginEntry entry = new PluginEntry();

                entry.setName(set.getString("name"));
                entry.setVersion(set.getString("version"));
                entry.setAuthor(set.getString("author"));
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
                entry.setRating(set.getFloat("rating"));
                entry.setScreenshots(gson.fromJson(set.getString("screenshots"), UUID[].class));

                return entry;
            }
        } catch (SQLException e) {
            log.error("Failed to get plugin: {}", e.getMessage());
        }
        return null;
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
}