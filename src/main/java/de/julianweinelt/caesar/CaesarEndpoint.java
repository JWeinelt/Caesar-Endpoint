package de.julianweinelt.caesar;

import de.julianweinelt.caesar.api.FileManager;
import de.julianweinelt.caesar.discord.DiscordBot;
import de.julianweinelt.caesar.storage.LocalStorage;
import de.julianweinelt.caesar.storage.MySQL;
import de.julianweinelt.caesar.web.Endpoint;
import lombok.Getter;


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
        instance.start();
    }

    public void start() {
        fileManager = new  FileManager();
        localStorage = new LocalStorage();
        localStorage.loadData();
        endpoint = new Endpoint();
        endpoint.start();
        discordBot = new DiscordBot();
        discordBot.start();
        mySQL = new MySQL();
        mySQL.connect();
        mySQL.createTables();
    }
}