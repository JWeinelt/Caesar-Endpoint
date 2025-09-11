package de.julianweinelt.caesar.web;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.julianweinelt.caesar.api.FileManager;
import de.julianweinelt.caesar.storage.MySQL;
import io.javalin.Javalin;
import io.javalin.config.MultipartConfig;
import io.javalin.config.SizeUnit;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.UploadedFile;
import io.javalin.http.staticfiles.Location;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;

@Slf4j
public class Endpoint {
    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private Javalin javalin;


    public void start() {
        new File("./lang/server").mkdirs();
        new File("./lang/client").mkdirs();

        javalin = Javalin.create(javalinConfig -> {
                    MultipartConfig cfg = new MultipartConfig();
                    cfg.maxTotalRequestSize(100, SizeUnit.MB);
                    cfg.maxFileSize(100, SizeUnit.MB);
                    cfg.maxInMemoryFileSize(100, SizeUnit.MB);
                    javalinConfig.showJavalinBanner = false;
                    javalinConfig.startupWatcherEnabled = false;

                    javalinConfig.jetty.multipartConfig = cfg;
                    javalinConfig.staticFiles.add("", Location.EXTERNAL);
                })
                .before(ctx -> {
                    ctx.header("Access-Control-Allow-Origin", "*");
                    ctx.header("Access-Control-Allow-Credentials", "true");
                })
                .get("/versions", ctx -> {
                    String content = new String(Files.readAllBytes(Paths.get("versions.json")));
                    ctx.result(content).status(200);
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
                .get("/public/download/emojis", ctx -> {
                    File file = new File("downloads/misc/emojis.zip");
                    FileInputStream fis = new FileInputStream(file);
                    ctx.result(fis);
                })
                .get("/public/download/mc-icons", ctx -> {
                    File file = new File("downloads/misc/minecraft-icons.zip");
                    FileInputStream fis = new FileInputStream(file);
                    ctx.result(fis);
                })
                .get("/public/download/client/{version}", ctx -> {
                    File file = new File("downloads/client/" + ctx.pathParam("version") + ".zip");
                    FileInputStream fis = new FileInputStream(file);
                    ctx.result(fis);
                })

                .get("/api/market/profile/{name}", ctx -> {
                    String user = ctx.pathParam("name");
                    UUID uuid;
                    if (user.equalsIgnoreCase("me")) {
                        uuid = getUserIDFromToken(ctx);
                    } else {
                        uuid = UUID.fromString(ctx.pathParam("name"));
                    }

                    ctx.result(MySQL.getInstance().getProfile(uuid).toString());
                })

                .get("/api/image/{id}", ctx -> {
                    UUID imageID = UUID.fromString(ctx.pathParam("id"));
                    log.info(imageID.toString());
                    String type = MySQL.getInstance().getImageType(imageID);
                    ctx.contentType(type);
                    ctx.result(new FileInputStream(FileManager.getInstance().getProfileImage(imageID)));
                })
                .post("/api/image/screenshot/{plugin}", ctx -> {
                    UploadedFile uploadedFile = ctx.uploadedFile("file");
                    if (uploadedFile == null) {
                        ctx.status(400).result(ErrorHandler.createError(ErrorHandler.CommonError.FILE_NOT_ATTACHED));
                        return;
                    }

                    InputStream content = uploadedFile.content();

                    File file = FileManager.getInstance().getImages();
                    try (FileOutputStream out = new FileOutputStream(file)) {
                        content.transferTo(out);
                    }
                })
                .post("/api/image/user-profile", ctx -> {
                    UploadedFile uploadedFile = ctx.uploadedFile("file");
                    if (uploadedFile == null) {
                        ctx.status(400).result(ErrorHandler.createError(ErrorHandler.CommonError.FILE_NOT_ATTACHED));
                        return;
                    }

                    UUID userID;
                    String user = ctx.queryParam("user");
                    if (user == null) userID = getUserIDFromToken(ctx);
                    else userID = UUID.fromString(user);
                    MySQL.getInstance().setImageType(userID, uploadedFile.contentType());


                    InputStream content = uploadedFile.content();

                    File file = FileManager.getInstance().userProfilePath(userID);
                    try (FileOutputStream out = new FileOutputStream(file)) {
                        content.transferTo(out);
                    }
                    ctx.result(createSuccessResponse());
                })

                .get("/api/market/plugin", ctx -> {
                    ctx.contentType("application/json");

                    String name = ctx.queryParam("name") != null ? ctx.queryParam("name") : null;
                    if (name == null) {
                        ctx.result(GSON.toJson(MySQL.getInstance().getPlugins())).status(200);
                        return;
                    }
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
                .post("/api/market/submit-plugin", ctx -> {
                    String pluginName = ctx.formParam("pluginName");
                    String shortDescription = ctx.formParam("pluginDescription");
                    String longDescription = ctx.formParam("pluginDescriptionLong");
                    String pluginVersion = ctx.formParam("pluginversion");
                    boolean backwardsCompatible = ctx.formParam("backwards") != null;
                    String pluginCategory = ctx.formParam("pluginCategory");
                    String github = ctx.formParam("githublink");
                    String wiki = ctx.formParam("wikilink");
                    String sponsor = ctx.formParam("sponsorlink");

                    UploadedFile pluginFile = ctx.uploadedFile("pluginFile");
                    if (pluginFile == null || !pluginFile.filename().endsWith(".jar")) {
                        ctx.status(400).result("You must upload a .jar file.");
                        return;
                    }

                    UploadedFile pluginLogo = ctx.uploadedFile("pluginLogo"); // Achtung! HTML-Feld heißt `pluginScreenshots`, sollte aber `pluginLogo` heißen

                    List<UploadedFile> screenshots = ctx.uploadedFiles("pluginScreenshots");

                    Path uploadDir = Paths.get("uploads", pluginName);
                    Files.createDirectories(uploadDir);

                    UUID pluginID = UUID.randomUUID();

                    try (InputStream in = pluginFile.content()) {
                        Files.copy(in, FileManager.getInstance().getFileToUploadPath(pluginID, "1.0.0").toPath(),
                                StandardCopyOption.REPLACE_EXISTING);
                    }

                    if (pluginLogo != null) {
                        try (InputStream in = pluginLogo.content()) {
                            Files.copy(in, FileManager.getInstance().getPluginLogo(pluginID).toPath(), StandardCopyOption.REPLACE_EXISTING);
                            MySQL.getInstance().setImageType(pluginID, pluginLogo.contentType());
                        }
                    }

                    int count = 1;
                    List<UUID> screenshotIDs = new ArrayList<>();
                    for (UploadedFile file : screenshots) {
                        try (InputStream in = file.content()) {
                            UUID uuid = UUID.randomUUID();
                            Files.copy(in, FileManager.getInstance().getScreenShot(uuid).toPath(),
                                    StandardCopyOption.REPLACE_EXISTING);
                            MySQL.getInstance().setImageType(uuid, file.contentType());
                            screenshotIDs.add(uuid);
                            count++;
                        }
                    }

                    PluginEntry entry = new PluginEntry();
                    entry.setUniqueId(pluginID);
                    entry.setDateCreated(Date.from(Instant.now()));
                    entry.setLastUpdated(Date.from(Instant.now()));
                    entry.setTags(new String[]{"Library"});
                    entry.setLicense("MIT");
                    entry.setDownloads(0);
                    entry.setVersion("1.0.0");
                    entry.setDescription(shortDescription);
                    entry.setDescriptionLong(longDescription);
                    entry.setAuthor(getUserIDFromToken(ctx).toString());
                    entry.setName(pluginName);
                    entry.setScreenshots(screenshotIDs.toArray(new UUID[0]));
                    entry.setRating(0);
                    MySQL.getInstance().importPlugin(entry);
                    ctx.result(createSuccessResponse());
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
                .post("/api/market/user/register", ctx -> {
                    JsonObject body = JsonParser.parseString(ctx.body()).getAsJsonObject();
                    log.info(body.toString());
                    String eMail = body.get("email").getAsString();
                    String password = body.get("password").getAsString();
                    String userName = body.get("username").getAsString();
                    UUID uuid = MySQL.getInstance().createAccount(eMail, password, userName);
                    if (uuid == null) {
                        ctx.status(400);
                    } else {
                        ctx.status(HttpStatus.OK);
                        JsonObject o = new JsonObject();
                        o.addProperty("success", true);
                        o.addProperty("uuid", uuid.toString());
                        o.addProperty("token", JWTUtil.token(uuid));
                        ctx.result(o.toString());
                    }
                })
                .post("/api/market/user/login", ctx -> {

                    String authBasic = ctx.header("Authorization");
                    if (authBasic == null || !authBasic.startsWith("Basic ")) {
                        ctx.status(HttpStatus.UNAUTHORIZED); // 401
                        ctx.result(createErrorResponse(ErrorType.INVALID_HEADER)).skipRemainingHandlers();
                        return;
                    }
                    String base64 = authBasic.substring(6);

                    byte[] base64DecodedBytes = Base64.getDecoder().decode(base64);
                    String decodedString = new String(base64DecodedBytes);
                    String eMail = decodedString.split(":")[0];
                    String password = decodedString.split(":")[1];

                    JsonObject o = MySQL.getInstance().getAccount(eMail);
                    if (o == null) {
                        ctx.result(createErrorResponse(ErrorType.USER_NOT_FOUND));
                        ctx.status(HttpStatus.UNAUTHORIZED); // 401
                        return;
                    }
                    /*if (!user.isActive()) {
                        ctx.result(createErrorResponse(ErrorType.USER_DISABLED));
                        ctx.status(HttpStatus.UNAUTHORIZED); // 401
                        return;
                    }*/
                    if (!Objects.equals(o.get("password").getAsString(), decodedString.split(":")[1])) {
                        ctx.result(createErrorResponse(ErrorType.PASSWORD_INVALID));
                        ctx.status(HttpStatus.UNAUTHORIZED); // 401
                        return;
                    }

                    JsonObject response = new JsonObject();
                    response.addProperty("success", true);
                    response.addProperty("token", JWTUtil.token(UUID.fromString(o.get("ID").getAsString())));
                    ctx.result(response.toString());
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


    public String createErrorResponse(ErrorType type ) {
        JsonObject o = new JsonObject();
        o.addProperty("success", false);
        o.addProperty("reason", type.name());
        return o.toString();
    }

    public String createSuccessResponse(String message) {
        JsonObject o = new JsonObject();
        o.addProperty("success", true);
        o.addProperty("message", message);
        return o.toString();
    }

    public String createSuccessResponse() {
        JsonObject o = new JsonObject();
        o.addProperty("success", true);
        return o.toString();
    }

    public UUID getUserIDFromToken(Context ctx) {

        String token = ctx.header("Authorization");
        if (token == null || token.isEmpty()) {
            ctx.status(HttpStatus.FORBIDDEN); // 403
            ctx.result(createErrorResponse(ErrorType.TOKEN_MISSING));
            ctx.skipRemainingHandlers();
            return null;
        }
        token = token.replace("Bearer ", "");
        if (!JWTUtil.verify(token)) {
            ctx.status(HttpStatus.FORBIDDEN); // 403
            ctx.result(createErrorResponse(ErrorType.TOKEN_INVALID));
            ctx.skipRemainingHandlers();
            return null;
        }
        DecodedJWT decodedJWT = JWTUtil.decode(token);
        if (decodedJWT.getExpiresAt().before(Date.from(Instant.now()))) {
            ctx.status(HttpStatus.FORBIDDEN); // 403
            ctx.result(createErrorResponse(ErrorType.TOKEN_EXPIRED));
            ctx.skipRemainingHandlers();
            return null;
        }
        String userID = decodedJWT.getSubject();
        return UUID.fromString(userID);
    }

    public void stop() {
        if (javalin == null) {
            log.warn("Web Service not running. Skipping shutdown process.");
            return;
        }

        javalin.stop();
    }

    public enum ErrorType {
        TOKEN_EXPIRED,
        TOKEN_INVALID,
        TOKEN_MISSING,
        USERNAME_INVALID,
        PASSWORD_INVALID,
        USER_NOT_FOUND,
        USER_DISABLED,
        INVALID_HEADER,
        INVALID_SETUP_CODE,
        NO_PERMISSION
    }
}