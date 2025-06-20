package de.julianweinelt.caesar.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import de.julianweinelt.caesar.storage.MySQL;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
public class Endpoint {
    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final String CLIENT_LANG_VERSION = "1.0.0";
    private final String SERVER_LANG_VERSION = "1.0.0";

    private final String CLIENT_MAIN_VERSION = "1.0.0";
    private final String SERVER_MAIN_VERSION = "1.0.0";
    private final String CONNECTOR_VERSION = "0.0.1";


    public void start() {
        new File("./lang/server").mkdirs();
        new File("./lang/client").mkdirs();

        new File("./downloads/worker").mkdirs();
        new File("./downloads/client").mkdirs();
        new File("./downloads/server").mkdirs();
        new File("./downloads/misc").mkdirs();
        Javalin app = Javalin.create(javalinConfig -> {
                    javalinConfig.showJavalinBanner = false;
                    javalinConfig.staticFiles.add(staticFileConfig -> {
                        staticFileConfig.directory = "./downloads";
                        staticFileConfig.hostedPath = "/download";
                        staticFileConfig.location = Location.EXTERNAL;
                    });
                })
                .get("/versions", ctx -> {
                    JsonObject json = new JsonObject();
                    json.addProperty("client", "1.0.0");
                    json.addProperty("server", "1.0.0");
                    json.addProperty("connector", "1.0.0");
                    ctx.result(json.toString()).status(200);
                })
                .get("/public/language/server/{language}", ctx -> {
                    ctx.contentType("application/json");
                    String language = ctx.pathParam("language");
                    if (language.equals("available")) {
                        List<String> languages = Files.list(Paths.get("./lang/server")).map(path ->
                                path.getFileName().toString().replace(".json", "")).toList();
                        ctx.result(GSON.toJson(languages));
                        return;
                    }
                    File file = new File("./lang/server/" + language + ".json");
                    if (!file.exists()) {
                        ctx.status(404);
                        ctx.result("Language not found");
                        ctx.skipRemainingHandlers();
                    }
                    ctx.result(loadLanguageFile(language, true));
                })
                .get("/public/language/client/{language}", ctx -> {
                    ctx.contentType("application/json");
                    String language = ctx.pathParam("language");
                    if (language.equals("available")) {
                        List<String> languages = Files.list(Paths.get("./lang/client")).map(path ->
                                path.getFileName().toString().replace(".json", "")).toList();
                        ctx.result(GSON.toJson(languages));
                        return;
                    }
                    File file = new File("./lang/client" + language + ".json");
                    if (!file.exists()) {
                        ctx.status(404);
                        ctx.result("Language not found");
                        ctx.skipRemainingHandlers();
                    }
                    ctx.result(loadLanguageFile(language, false));
                })

                .get("/api/market/plugin", ctx -> {
                    ctx.contentType("application/json");

                    String name = ctx.queryParam("name") != null ? ctx.queryParam("name") : null;
                    UUID uuid = MySQL.getInstance().getPluginID(name);
                    if (uuid == null) {
                        ctx.status(404);
                        ctx.result("Plugin not found");
                        return;
                    }
                    PluginEntry entry = MySQL.getInstance().getPlugin(uuid);
                    if (entry == null) {
                        ctx.status(404);
                        ctx.result("Plugin not found");
                        return;
                    }
                    ctx.result(GSON.toJson(entry));
                    ctx.status(200);
                })
                .start(48009);
    }

    private String loadLanguageFile(String language, boolean server) throws IOException {
        String p = "server";
        if (!server) p = "client";
        return Files.readString(Paths.get("./lang/" + p + "/" + language + ".json"));
    }
}