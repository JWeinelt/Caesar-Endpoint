package de.julianweinelt.caesar.discord;

import de.julianweinelt.caesar.CaesarEndpoint;

public class DiscordBot {
    public static DiscordBot getInstance() {
        return CaesarEndpoint.getInstance().getDiscordBot();
    }

    public void start() {

    }
}