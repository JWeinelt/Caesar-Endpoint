package de.julianweinelt.caesar.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.julianweinelt.caesar.api.FileManager;
import de.julianweinelt.caesar.storage.MySQL;
import io.javalin.Javalin;
import io.javalin.http.UploadedFile;
import io.javalin.http.staticfiles.Location;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
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

        Javalin app = Javalin.create(javalinConfig -> {
                    javalinConfig.showJavalinBanner = false;
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
                .post("/api/market/plugin", ctx -> {
                    JsonObject body = JsonParser.parseString(ctx.body()).getAsJsonObject();

                })
                .put("/api/market/plugin/upload/{id}/{version}", ctx -> {
                    UUID pluginID = UUID.fromString(ctx.pathParam("id"));
                    String version = ctx.pathParam("version");
                    UploadedFile uploadedFile = ctx.uploadedFile("file");
                    if (uploadedFile == null) {
                        ctx.status(400).result(ErrorHandler.createError(ErrorHandler.CommonError.FILE_NOT_ATTACHED));
                        return;
                    }

                    InputStream content = uploadedFile.content();

                    File file = FileManager.getInstance().getFileToUploadPath(pluginID, version);
                    try (FileOutputStream out = new FileOutputStream(file)) {
                        content.transferTo(out);
                    }

                    ctx.result("File has been uploaded.");
                })

                .get("/download/plugin/{pluginId}/{version}", ctx -> {
                    UUID pluginID = UUID.fromString(ctx.pathParam("pluginId"));
                    String version = ctx.pathParam("version");
                    File file = FileManager.getInstance().getFileToUploadPath(pluginID, version);
                    FileInputStream fis = new FileInputStream(file);

                    ctx.result(fis);
                })

                .patch("/api/minecraft/rate", ctx -> {
                    JsonObject body = JsonParser.parseString(ctx.body()).getAsJsonObject();

                })
                .get("/api/minecraft/plugin", ctx -> {
                    String market = ctx.queryParam("market");
                    String pluginName = ctx.queryParam("pluginName");
                    if (market == null || pluginName == null) {
                        ctx.status(404).result(ErrorHandler.createError(ErrorHandler.CommonError.MC_PLUGIN_NOT_FOUND));
                        return;
                    }

                })
                .start(48009);
    }

    private String loadLanguageFile(String language, boolean server) throws IOException {
        String p = "server";
        if (!server) p = "client";
        return Files.readString(Paths.get("./lang/" + p + "/" + language + ".json"));
    }
}