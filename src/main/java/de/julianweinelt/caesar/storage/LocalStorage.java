package de.julianweinelt.caesar.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.julianweinelt.caesar.CaesarEndpoint;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class LocalStorage {
    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    private final File configFile = new File("config.json");

    @Getter
    private Configuration data = new Configuration();


    public static LocalStorage getInstance() {
        return CaesarEndpoint.getInstance().getLocalStorage();
    }


    public void loadData() {
        log.info("Loading local storage...");
        if (!configFile.exists()) saveData();
        try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
            StringBuilder jsonStringBuilder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                jsonStringBuilder.append(line);
            }
            data = GSON.fromJson(jsonStringBuilder.toString(), new TypeToken<Configuration>(){}.getType());
        } catch (IOException e) {
            log.error("Failed to load local storage.", e);
        }
    }


    public void saveData() {
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(GSON.toJson(data));
        } catch (IOException e) {
            log.error("Failed to save object: {}", e.getMessage(), e);
        }
        log.info("Local storage saved.");
    }
}
