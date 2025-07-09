package de.julianweinelt.caesar.web;

import com.google.gson.JsonObject;

public class ErrorHandler {
    public static String createError(CommonError err) {
        JsonObject o = new JsonObject();
        o.addProperty("success", false);
        o.addProperty("message", err.message);
        o.addProperty("location", err.location);
        return o.toString();
    }

    public enum CommonError {
        PLUGIN_NOT_FOUND("This plugin could not be found in Caesar Marketplace.", "Marketplace"),
        PLUGIN_NO_LICENSE("This plugin is marked as commercial and needs a license to use it.", "Marketplace"),
        MC_PLUGIN_PROVIDER_NOT_FOUND("", ""),
        MC_PLUGIN_PROVIDER_EMPTY("", ""),
        MC_PLUGIN_NOT_FOUND("", ""),
        MC_PLUGIN_QUERY_EMPTY("", ""),
        FILE_NOT_ATTACHED("There was no file attached.", "Marketplace"),
        RATE_LIMIT_EXCEEDED("", "");

        public final String message;
        public final String location;

        CommonError(String message, String location) {
            this.message = message;
            this.location = location;
        }
    }
}
