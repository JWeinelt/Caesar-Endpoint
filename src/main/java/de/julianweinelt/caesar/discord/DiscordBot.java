package de.julianweinelt.caesar.discord;

import de.julianweinelt.caesar.CaesarEndpoint;
import de.julianweinelt.caesar.storage.Configuration;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DiscordBot {
    public static DiscordBot getInstance() {
        return CaesarEndpoint.getInstance().getDiscordBot();
    }

    private JDA jda;

    public void start() {
        if (Configuration.getInstance().getDiscordSecret() == null || Configuration.getInstance().getDiscordSecret().isBlank()) return;
        try {
            log.info("Starting Discord Bot");
            jda = JDABuilder.createDefault(Configuration.getInstance().getDiscordSecret(), Arrays.asList(GatewayIntent.values()))
                    .build();
            try {jda.awaitReady();} catch (InterruptedException ignored) {}
            jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.customStatus("Working at Caesar Marketplace"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        log.info("Shutting down Discord instance...");
        if (jda == null) {
            log.warn("Discord websocket instance seems not be started. Skipping shutdown process...");
            return;
        }
        try {
            log.info("Waiting 5 seconds for clean shutdown...");
            jda.shutdown();
            if (!jda.awaitShutdown(5, TimeUnit.SECONDS)) {
                log.warn("Discord websocket shutdown exceed the time limit. Interrupting...");
                jda.shutdownNow();
                log.warn("Discord websocket connection has been interrupted.");
            }
        } catch (InterruptedException e) {
            log.info("Discord websocket instance was interrupted while shutting down!");
        }
        log.info("Discord websocket instance has been shut down.");
    }
}