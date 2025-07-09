package de.julianweinelt.caesar.api;

import com.vdurmont.semver4j.Semver;
import de.julianweinelt.caesar.CaesarEndpoint;
import de.julianweinelt.caesar.storage.MySQL;

import java.io.File;
import java.util.UUID;

public class FileManager {
    private final File plugins;
    private final File images;
    private final File versionsClient;
    private final File versionsServer;
    private final File versionsConsoleClient;
    private final File versionsWorker;

    public FileManager() {
        plugins = new File("data/files/plugins");
        images = new File("data/files/images");
        versionsClient = new File("data/files/caesar/client");
        versionsServer = new File("data/files/caesar/server");
        versionsConsoleClient = new File("data/files/caesar/consoleClient");
        versionsWorker = new File("data/files/caesar/worker");

        plugins.mkdirs();
        images.mkdirs();
        versionsClient.mkdirs();
        versionsServer.mkdirs();
        versionsConsoleClient.mkdirs();
        versionsWorker.mkdirs();
    }

    public static FileManager getInstance() {
        return CaesarEndpoint.getInstance().getFileManager();
    }

    public File getFileToUploadPath(UUID plugin, String version) {
        UUID fileID = UUID.randomUUID();
        Semver ver = new Semver(version);
        MySQL.getInstance().recordPluginFile(plugin, ver, fileID);
        return new File(plugins, fileID + ".pl");
    }

    public File getPluginJar(String version, UUID plugin) {
        return new File(plugins, MySQL.getInstance().getPluginFilePath(plugin, new Semver(version)) + ".pl");
    }
}