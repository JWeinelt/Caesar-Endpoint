package de.julianweinelt.caesar.web;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.julianweinelt.caesar.CaesarEndpoint;
import de.julianweinelt.caesar.api.FileManager;
import de.julianweinelt.caesar.storage.MySQL;
import de.julianweinelt.caesar.storage.data.AccountStatus;
import de.julianweinelt.caesar.storage.data.PluginManager;
import de.julianweinelt.caesar.storage.data.User;
import de.julianweinelt.caesar.storage.data.UserManager;
import de.julianweinelt.caesar.web.mail.EMailUtil;
import de.julianweinelt.caesar.web.mail.EmailTemplateProvider;
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
import java.util.stream.Stream;

@Slf4j
@SuppressWarnings("SpellCheckingInspection")
public class Endpoint {
    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private Javalin javalin;


    public void start() {
        if (new File("./lang/server").mkdirs()) log.info("Created directory for server language data.");
        if (new File("./lang/client").mkdirs()) log.info("Created directory for client language data.");

        javalin = Javalin.create(javalinConfig -> {
                    MultipartConfig cfg = new MultipartConfig();
                    cfg.maxTotalRequestSize(100, SizeUnit.MB);
                    cfg.maxFileSize(100, SizeUnit.MB);
                    cfg.maxInMemoryFileSize(100, SizeUnit.MB);
                    javalinConfig.showJavalinBanner = false;
                    javalinConfig.startupWatcherEnabled = false;

                    javalinConfig.jetty.multipartConfig = cfg;
                    javalinConfig.staticFiles.add("app", Location.EXTERNAL);
                })
                .before(ctx -> {
                    if (serviceOffline(ctx)) return;
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
                        try (Stream<Path> files = Files.list(Paths.get("./lang/server"))) {
                            List<String> languages = files.map(path ->
                                    path.getFileName().toString().replace(".json", "")).toList();
                            ctx.result(GSON.toJson(languages));
                        }
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

                        try (Stream<Path> files = Files.list(Paths.get("./lang/client"))) {
                            List<String> languages = files.map(path ->
                                    path.getFileName().toString().replace(".json", "")).toList();
                            ctx.result(GSON.toJson(languages));
                        }
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
                .get("/public/download/worker/{version}", ctx -> {
                    File file = new File("downloads/worker/" + ctx.pathParam("version") + "/CaesarWorker.exe");
                    FileInputStream fis = new FileInputStream(file);
                    ctx.result(fis);
                })

                .get("/api/market/profile/{name}", ctx -> {
                    String user = ctx.pathParam("name");
                    UUID uuid;
                    if (user.equalsIgnoreCase("me")) {
                        uuid = getUserIDFromToken(ctx);
                    } else {
                        try {
                            uuid = UUID.fromString(ctx.pathParam("name"));
                        } catch (IllegalArgumentException e) {
                            uuid = UserManager.getInstance().getUserByName(ctx.pathParam("name")).getUniqueID();
                        }
                    }

                    ctx.result(MySQL.getInstance().getProfile(uuid).toString());
                })

                .get("/api/image/{id}", ctx -> {
                    String queryParamType = ctx.queryParam("type");
                    String typeI = queryParamType == null ? "profile" : queryParamType;
                    UUID imageID = UUID.fromString(ctx.pathParam("id"));
                    String type = MySQL.getInstance().getImageType(imageID);
                    if (type == null) {
                        ctx.status(401);
                        ctx.result(createErrorResponse(ErrorType.INTERNAL_ERROR));
                        return;
                    }
                    ctx.contentType(type);
                    switch (typeI) {
                        case "profile" ->
                                ctx.result(new FileInputStream(FileManager.getInstance().getProfileImage(imageID)));
                        case "screenshot" ->
                                ctx.result(new FileInputStream(FileManager.getInstance().getScreenShot(imageID)));
                        case "plogo" ->
                                ctx.result(new FileInputStream(FileManager.getInstance().getPluginLogo(imageID)));
                    }
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

                .patch("/api/market/follower", ctx -> {
                    JsonObject root = JsonParser.parseString(ctx.body()).getAsJsonObject();
                    UUID followedAcc = UUID.fromString(root.get("followed").getAsString());
                    UUID follower = getUserIDFromToken(ctx);

                    if (follower == null) {
                        ctx.result(createErrorResponse(ErrorType.TOKEN_INVALID)).status(HttpStatus.FORBIDDEN); // 403
                        return;
                    }
                    MySQL.getInstance().addFollower(followedAcc, follower);
                })

                .get("/api/market/plugin", ctx -> {
                    ctx.contentType("application/json");

                    String name = ctx.queryParam("name") != null ? ctx.queryParam("name") : null;
                    if (name == null) {
                        ctx.result(GSON.toJson(PluginManager.getInstance().getPlugins())).status(200);
                        return;
                    }
                    UUID id;
                    try {
                        id = UUID.fromString(name);
                    } catch (IllegalArgumentException e) {
                        id = null;
                    }
                    PluginEntry entry = id == null ? PluginManager.getInstance().getPlugin(name) :
                            PluginManager.getInstance().getPlugin(id);
                    if (entry == null) {
                        ctx.status(404);
                        ctx.result("Plugin not found");
                        return;
                    }
                    JsonObject o = new JsonObject();
                    o.add("plugin", GSON.toJsonTree(entry));
                    ctx.result(o.toString());
                    ctx.status(200);
                })
                .get("/api/market/plugin/comment", ctx -> {
                    UUID pluginID = null;
                    String pluginName = null;
                    if (ctx.queryParam("pluginName") != null) pluginName = ctx.queryParam("pluginName");
                    String pluginQueryID = ctx.queryParam("pluginID");
                    if (pluginQueryID != null) pluginID = UUID.fromString(pluginQueryID);
                    if (pluginID == null  && pluginName == null) {
                        return;
                    }
                    if (pluginID == null) {
                        pluginID = MySQL.getInstance().getPluginID(pluginName);
                    }

                    ctx.result(MySQL.getInstance().getPluginComments(pluginID).toString());
                })
                .post("/api/plugin/comment", ctx -> {
                    JsonObject root = JsonParser.parseString(ctx.body()).getAsJsonObject();
                    String pluginName = root.get("pluginName").getAsString();
                    String content = root.get("content").getAsString();
                    UUID author = getUserIDFromToken(ctx);
                    if (author == null) {
                        ctx.status(401).result(createErrorResponse(ErrorType.USER_NOT_FOUND));
                        return;
                    }
                    MySQL.getInstance().createComment(pluginName, author, content);
                    ctx.result(createSuccessResponse());
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

                    UploadedFile pluginLogo = ctx.uploadedFile("pluginLogo");

                    List<UploadedFile> screenshots = ctx.uploadedFiles("pluginScreenshots");

                    Path uploadDir = Paths.get("uploads", pluginName);
                    Files.createDirectories(uploadDir);

                    UUID pluginID = UUID.randomUUID();

                    try (InputStream in = pluginFile.content()) {
                        Files.copy(in, FileManager.getInstance().getFileToUploadPath(pluginID, pluginVersion).toPath(),
                                StandardCopyOption.REPLACE_EXISTING);
                    }

                    if (pluginLogo != null) {
                        try (InputStream in = pluginLogo.content()) {
                            Files.copy(in, FileManager.getInstance().getPluginLogo(pluginID).toPath(), StandardCopyOption.REPLACE_EXISTING);
                            log.info("Saved plugin logo file");
                            MySQL.getInstance().setImageType(pluginID, pluginLogo.contentType());
                        }
                    }

                    List<UUID> screenshotIDs = new ArrayList<>();
                    for (UploadedFile file : screenshots) {
                        try (InputStream in = file.content()) {
                            UUID uuid = UUID.randomUUID();
                            Files.copy(in, FileManager.getInstance().getScreenShot(uuid).toPath(),
                                    StandardCopyOption.REPLACE_EXISTING);
                            MySQL.getInstance().setImageType(uuid, file.contentType());
                            screenshotIDs.add(uuid);
                        }
                    }

                    PluginEntry entry = new PluginEntry();
                    entry.setBackwardsCompatible(backwardsCompatible);
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
                    entry.setWikiLink(wiki);
                    entry.setSponsorLink(sponsor);
                    entry.setSourceCode(github);
                    entry.setState(PluginState.REQUESTED);
                    entry.getCategories().add(pluginCategory);
                    PluginManager.getInstance().createPlugin(entry);
                    ctx.result(createSuccessResponse());
                })
                .post("/api/market/plugin/edit", ctx -> {
                    JsonObject root = JsonParser.parseString(ctx.body()).getAsJsonObject();
                    UUID pluginID = UUID.fromString(root.get("pluginID").getAsString());
                    PluginEntry entry = PluginManager.getInstance().getPlugin(pluginID);
                    if (entry == null) {
                        ctx.result(createErrorResponse(ErrorType.PLUGIN_NOT_FOUND)).status(HttpStatus.BAD_REQUEST);
                        return;
                    }
                    if (root.has("gitHubLink")) entry.setSourceCode(root.get("gitHubLink").getAsString());
                    if (root.has("sponsorLink")) entry.setSponsorLink(root.get("sponsorLink").getAsString());
                    if (root.has("wikiLink")) entry.setWikiLink(root.get("wikiLink").getAsString());
                    if (root.has("descLong")) entry.setDescriptionLong(root.get("descLong").getAsString());
                    if (root.has("descShort")) entry.setDescription(root.get("descShort").getAsString());
                    if (root.has("license")) entry.setLicense(root.get("license").getAsString());

                    MySQL.getInstance().updatePlugin(entry);
                    ctx.result(createSuccessResponse()).status(HttpStatus.CREATED);
                })
                .post("/api/market/plugin/state", ctx -> {
                    JsonObject root = JsonParser.parseString(ctx.body()).getAsJsonObject();
                    UUID userID = getUserIDFromToken(ctx);
                    User u = UserManager.getInstance().getUserByUUID(userID);
                    if (!u.hasPermission("admin")) {
                        ctx.status(HttpStatus.FORBIDDEN).result(createErrorResponse(ErrorType.NO_PERMISSION));
                        return;
                    }
                    try {
                        PluginState state = PluginState.valueOf(root.get("state").getAsString().toUpperCase());
                        UUID pluginID = UUID.fromString(root.get("pluginID").getAsString());
                        boolean success = PluginManager.getInstance().updatePluginState(pluginID, state);
                        if (!success) {
                            ctx.result(createErrorResponse(ErrorType.INTERNAL_ERROR)).status(HttpStatus.IM_USED);
                            return;
                        }
                        ctx.result(createSuccessResponse());
                        ctx.status(HttpStatus.OK);
                    } catch (IllegalArgumentException ignored) {
                        ctx.status(HttpStatus.BAD_REQUEST).result(createErrorResponse(ErrorType.INVALID_BODY));
                    }
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
                    if (UserManager.getInstance().getUserByEmail(eMail) != null) {
                        ctx.status(401).result(createErrorResponse(ErrorType.USER_ALREADY_EXISTS));
                        return;
                    }
                    String password = body.get("password").getAsString();
                    String userName = body.get("username").getAsString();
                    UUID uuid = UserManager.getInstance().createUser(eMail, password, userName);
                    String confirmID = UUID.randomUUID() + "-" + UUID.randomUUID();
                    UserManager.getInstance().getWaitForConfirmAccounts().put(uuid, confirmID);
                    if (uuid == null) {
                        ctx.status(400);
                    } else {
                        ctx.status(HttpStatus.OK);

                        File destination = FileManager.getInstance().userProfilePath(uuid);
                        Files.copy(new File("./app/img/account_profile_placeholder.png").toPath(), destination.toPath());
                        MySQL.getInstance().setImageType(uuid, "image/png");

                        EMailUtil.getInstance().sendEmail(eMail, "Confirm your eMail address",
                                EmailTemplateProvider.welcomeMessage(userName, uuid, confirmID));

                        JsonObject o = new JsonObject();
                        o.addProperty("success", true);
                        o.addProperty("uuid", uuid.toString());
                        o.addProperty("token", JWTUtil.token(uuid));
                        ctx.result(o.toString());
                    }
                })
                .post("/api/market/user/confirm", ctx -> {
                    JsonObject root = JsonParser.parseString(ctx.body()).getAsJsonObject();
                    String confirmID = root.get("confirmID").getAsString();
                    UUID account = UUID.fromString(root.get("account").getAsString());
                    if (confirmID.equals(UserManager.getInstance().getWaitForConfirmAccounts().getOrDefault(account, ""))) {
                        UserManager.getInstance().getWaitForConfirmAccounts().remove(account);
                        UserManager.getInstance().getUserByUUID(account).setAccountStatus(
                                AccountStatus.getStatus(UUID.fromString("8a2a721f-b017-11f0-a242-bc2411718ef7")));
                        ctx.status(200).result(createSuccessResponse());
                    } else ctx.status(404);
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

                    User u = UserManager.getInstance().getUserByEmail(eMail);
                    if (u == null) {
                        ctx.result(createErrorResponse(ErrorType.USER_NOT_FOUND));
                        ctx.status(HttpStatus.UNAUTHORIZED); // 401
                        return;
                    }
                    if (!Objects.equals(u.getPassword(), password)) {
                        ctx.result(createErrorResponse(ErrorType.PASSWORD_INVALID));
                        ctx.status(HttpStatus.UNAUTHORIZED); // 401
                        return;
                    }
                    if (!u.getAccountStatus().equals(AccountStatus.byName("Active"))) {
                        ctx.status(HttpStatus.UNAUTHORIZED); // 401
                        ctx.result(createErrorResponse(ErrorType.USER_DISABLED));
                        return;
                    }

                    EMailUtil.getInstance().sendEmail(u.getEMail(), "New login to your account",
                            EmailTemplateProvider.loginInfo(u.getUniqueID(), ctx.ip(), "Unknown"));

                    JsonObject response = new JsonObject();
                    response.addProperty("success", true);
                    response.addProperty("token", JWTUtil.token(u.getUniqueID()));
                    response.addProperty("username", u.getUserName());
                    response.addProperty("userID", u.getUniqueID().toString());
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
                    //TODO: Add logic
                    ctx.result(createSuccessResponse());
                })
                .get("/api/minecraft/plugin", ctx -> {
                    String market = ctx.queryParam("market");
                    String pluginName = ctx.queryParam("pluginName");
                    if (market == null || pluginName == null) {
                        ctx.status(404).result(ErrorHandler.createError(ErrorHandler.CommonError.MC_PLUGIN_NOT_FOUND));
                        return;
                    }
                    ctx.result(createSuccessResponse("Work in progress")); //TODO: add logic
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
        if (decodedJWT == null) {
            ctx.status(HttpStatus.FORBIDDEN); // 403
            ctx.result(createErrorResponse(ErrorType.TOKEN_INVALID));
            ctx.skipRemainingHandlers();
            return null;
        }
        if (decodedJWT.getExpiresAt().before(Date.from(Instant.now()))) {
            ctx.status(HttpStatus.FORBIDDEN); // 403
            ctx.result(createErrorResponse(ErrorType.TOKEN_EXPIRED));
            ctx.skipRemainingHandlers();
            return null;
        }
        String userID = decodedJWT.getSubject();
        return UUID.fromString(userID);
    }

    private boolean serviceOffline(Context ctx) {
        if (CaesarEndpoint.getInstance().isMaintenance()) {
            ctx.status(HttpStatus.SERVICE_UNAVAILABLE).result(createErrorResponse(ErrorType.MAINTENANCE));
            ctx.skipRemainingHandlers();
            return true;
        }
        return false;
    }

    public void stop() {
        if (javalin == null) {
            log.warn("Web Service not running. Skipping shutdown process.");
            return;
        }

        javalin.stop();
    }

    public enum ErrorType {
        MAINTENANCE,
        TOKEN_EXPIRED,
        TOKEN_INVALID,
        TOKEN_MISSING,
        USERNAME_INVALID,
        PASSWORD_INVALID,
        USER_NOT_FOUND,
        USER_ALREADY_EXISTS,
        USER_DISABLED,
        INVALID_HEADER,
        INVALID_BODY,
        INVALID_SETUP_CODE,
        NO_PERMISSION,
        INTERNAL_ERROR,
        PLUGIN_NOT_FOUND
    }
}