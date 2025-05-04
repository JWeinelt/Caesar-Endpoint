package de.julianweinelt.caesar.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
                .get("/public/language/server/{language}", ctx -> {
                    ctx.contentType("application/json");
                    String language = ctx.pathParam("language");
                    if (language.equals("available")) {
                        List<String> languages = Files.list(Paths.get("./lang/server")).map(path ->
                                path.getFileName().toString().replace(".json", "")).toList();
                        ctx.result(GSON.toJson(languages));
                        return;
                    }
                    File file = new File("./lang/server" + language + ".json");
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

                .start(49850);


        File pageFolder = new File("./market");

        try (Stream<Path> paths = Files.walk(Paths.get(pageFolder.toURI()))) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".html"))
                    .forEach(path -> {
                        String filename = path.getFileName().toString();
                        String route = "/" + filename.replace(".html", "");

                        app.get(route, ctx -> {
                            ctx.result(Files.readString(path));  // Lese die HTML-Datei und gebe sie als Response zurück
                            ctx.contentType("text/html");
                        });
                    });
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        app.get("/{page}.html", ctx -> {
            ctx.redirect("/" + ctx.pathParam("page"));
        });
        app.get("/", ctx -> {
            Path indexPath = Paths.get("./market/index.html");
            if (Files.exists(indexPath)) {
                ctx.result(Files.readString(indexPath));
                ctx.contentType("text/html");
            } else {
                ctx.status(404).result("index.html not found");
            }
        });
    }

    private String loadLanguageFile(String language, boolean server) throws IOException {
        String p = "server";
        if (!server) p = "client";
        return Files.readString(Paths.get("./lang/" + p + "/" + language + ".json"));
    }
}