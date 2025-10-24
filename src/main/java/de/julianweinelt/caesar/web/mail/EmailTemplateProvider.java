package de.julianweinelt.caesar.web.mail;

import de.julianweinelt.caesar.CaesarEndpoint;
import de.julianweinelt.caesar.storage.data.User;
import de.julianweinelt.caesar.storage.data.UserManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

public class EmailTemplateProvider {
    public static String welcomeMessage(String username, UUID account, String confirmID) {
        String headline = "Welcome to the Caesar Marketplace!";
        String para1 = "Thanks for creating an account on our Marketplace!";
        String para2 = "To verify that it's you, please confirm your email address by clicking the button.";
        String ctaTitle = "Verify account";
        String ctaLink = "https://market.caesarnet.cloud/verify?id=" + confirmID + "&account=" + account;
        String alternativeText = "If you can't use it for some reason, try this link: ";
        String footer = "This wasn't you? Don't worry! Just do nothing, we'll delete the newly created account in 7 days.";

        String message = loadContent("default");
        message = message.replace("%datum%", getDate()).replace("%headline%", headline).replace("%paragraph1%", para1)
                .replace("%name%", username).replace("%paragraph2%", para2).replace("%alternativeText%", alternativeText)
                .replace("%alt-link-name%", ctaLink).replace("%footer1%", footer)
                .replace("%cta-title", ctaTitle).replace("%cta-link%", ctaLink)
                .replace("%alt-link", ctaLink);
        return message;
    }

    public static String passwordReset(UUID account, UUID requestID) {
        User u = UserManager.getInstance().getUserByUUID(account);
        String ctaLink = "https://market.caesarnet.cloud/account/reset-pw?account=" + account + "&request=" + requestID;
        String ctaTitle = "Reset Password";
        String alternativeText = "If you can't use it for some reason, try this link: ";
        String footer = "";
        String data = loadContent("default");
        data = data.replace("%datum%", getDate()).replace("%headline%", "Reset your password")
                .replace("%paragraph1%", "You've requested to reset your password.").replace("%name%", u.getUserName())
                .replace("%paragraph2%", "Click to following link to set a new password. This wasn't " +
                        "you? You can just ignore this message.").replace("%alternativeText%", alternativeText)
                .replace("%alt-link-name%", ctaLink).replace("%footer1%", footer)
                .replace("%cta-title", ctaTitle).replace("%cta-link%", ctaLink)
                .replace("%alt-link", ctaLink);
        return data;
    }
    public static String loginInfo(UUID account, String ipAddress, String location) {
        User u = UserManager.getInstance().getUserByUUID(account);

        String data = loadContent("default-no-link");
        data = data.replace("%datum%", getDate()).replace("%headline%", "New login")
                .replace("%paragraph2%", "There was a new login into your account by " + ipAddress + " from "
                        + location + ".<br>" +
                        "<b>If this wasn't you,</b> please immediately change your password and check your current account sessions.")
                .replace("%name%", u.getUserName())
                .replace("%paragraph1%", "");
        return data;
    }

    private static String loadContent(String template) {
        String path = "/mail-templates/" + template + ".html";
        try (InputStream input = CaesarEndpoint.class.getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalArgumentException("Resource not found: " + path);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load resource: " + path, e);
        }
    }

    private static String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(new Date());
    }
}
