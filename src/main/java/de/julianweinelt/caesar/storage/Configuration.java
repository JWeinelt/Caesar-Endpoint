package de.julianweinelt.caesar.storage;

import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
public class Configuration {
    public static Configuration getInstance() {
        return LocalStorage.getInstance().getData();
    }

    private String databaseHost = "localhost";
    private int databasePort = 3306;
    private String databaseName = "caesar";
    private String databaseUser = "caesar";
    private String databasePassword = "secret";

    private String discordSecret = "secret";
    private final HashMap<String, String> discordChannels = new HashMap<>();
    private String discordGuild = "";

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Configuration c) {
            return c.databaseHost.equals(databaseHost) && c.databasePort == databasePort
                    && c.databaseName.equals(databaseName) && c.databaseUser.equals(databaseUser)
                    && c.databasePassword.equals(databasePassword) && c.discordSecret.equals(discordSecret)
                    && c.discordGuild.equals(discordGuild) && c.discordChannels.equals(discordChannels);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return databaseHost.hashCode();
    }

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}