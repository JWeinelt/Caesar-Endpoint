package de.julianweinelt.caesar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.Javalin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CaesarEndpoint {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String CLIENT_LANG_VERSION = "1.0.0";
    private static final String SERVER_LANG_VERSION = "1.0.0";

    private static final String CLIENT_MAIN_VERSION = "1.0.0";
    private static final String SERVER_MAIN_VERSION = "1.0.0";

    public static void main(String[] args) {
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
    }

    private static String loadLanguageFile(String language, boolean server) throws IOException {
        String p = "server";
        if (!server) p = "client";
        return Files.readString(Paths.get("./lang/" + p + "/" + language + ".json"));
    }
}
