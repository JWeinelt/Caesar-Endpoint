package de.julianweinelt.caesar;

import de.julianweinelt.caesar.discord.DiscordBot;
import de.julianweinelt.caesar.storage.LocalStorage;
import de.julianweinelt.caesar.web.Endpoint;
import lombok.Getter;


public class CaesarEndpoint {

    @Getter
    private static CaesarEndpoint instance;
    @Getter
    private LocalStorage localStorage;
    private Endpoint endpoint;
    private DiscordBot discordBot;

    public static void main(String[] args) {
        instance = new CaesarEndpoint();
        instance.start();
    }

    public void start() {
        localStorage = new LocalStorage();
        localStorage.loadData();
        endpoint = new Endpoint();
        endpoint.start();
        discordBot = new DiscordBot();
        discordBot.start();
    }
}
