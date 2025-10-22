package de.julianweinelt.caesar;

import de.julianweinelt.caesar.api.FileManager;
import de.julianweinelt.caesar.discord.DiscordBot;
import de.julianweinelt.caesar.storage.Configuration;
import de.julianweinelt.caesar.storage.LocalStorage;
import de.julianweinelt.caesar.storage.MySQL;
import de.julianweinelt.caesar.web.Endpoint;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;


@Slf4j
public class CaesarEndpoint {

    @Getter
    private static CaesarEndpoint instance;
    @Getter
    private LocalStorage localStorage;
    @Getter
    private Endpoint endpoint;
    @Getter
    private DiscordBot discordBot;

    @Getter
    private FileManager fileManager;

    @Getter
    private MySQL mySQL;

    public static void main(String[] args) {
        instance = new CaesarEndpoint();
        instance.start(args);
    }

    public void start(String[] args) {
        if (!Arrays.stream(args).toList().contains("--intended-use")) {
            log.info("""
                    _____________________________________________________________________________
                    |                                                                           |
                    |      _____                                                                |
                    |     / / \\ \\                                                             |
                    |    / /| |\\ \\                                                            |
                    |   / / | | \\ \\                                                           |
                    |  / /  |_|  \\ \\                                                          |
                    | /_/___(_)___\\_\\                                                         |
                    |                                                                           |
                    |  This software should NOT be hosted by yourself!                          |
                    |  It provides the API for the Caesar Marketplace.                          |
                    |  You won't be able to use most of features and you don't have access      |
                    |  to any live data by the Caesar service.                                  |
                    |                                                                           |
                    |  Proceed at your own risk.                                                |
                    |  Caesar Endpoint is starting in 5 seconds...                              |
                    |___________________________________________________________________________|
                    """);
            try {Thread.sleep(5000);} catch (InterruptedException ignored) {}
        }

        log.info("Starting up Caesar Endpoint public service...");
        log.info("Preparing files...");
        fileManager = new  FileManager();
        log.info("Preparing local storage provider...");
        localStorage = new LocalStorage();
        localStorage.loadData();
        log.info("Starting Web Endpoint...");
        endpoint = new Endpoint();
        endpoint.start();
        log.info("Web Endpoint started.");
        log.info("Starting Discord integration...");
        discordBot = new DiscordBot();
        discordBot.start();
        log.info("Connecting to database...");
        mySQL = new MySQL();
        mySQL.connect();
        mySQL.createTables();
        log.info("Done! Caesar Endpoint has been started.");
        log.info("All services are up and running.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Detected stop request!");
            log.info("Performing clean shutdown...");
            stop();
        }));
    }

    public synchronized void stop() {
        log.info("Disconnecting from database...");
        mySQL.disconnect();
        log.info("Done!");
        log.info("Shutting down webservice.");
        endpoint.stop();
        discordBot.stop();
        log.info("Everything has been stopped.");
        log.info("Closing java application...");
    }
}