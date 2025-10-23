package de.julianweinelt.caesar.storage.data;

import de.julianweinelt.caesar.CaesarEndpoint;
import de.julianweinelt.caesar.storage.MySQL;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class UserManager {
    private final List<User> users = new ArrayList<>();
    @Getter
    private final HashMap<UUID, List<String>> permissions = new HashMap<>();

    @Getter
    private final HashMap<UUID, String> waitForConfirmAccounts = new HashMap<>();

    public static UserManager getInstance() {
        return CaesarEndpoint.getInstance().getUserManager();
    }

    public void getData() {
        users.clear();
        users.addAll(MySQL.getInstance().getAccounts());
        permissions.clear();
        permissions.putAll(MySQL.getInstance().getUserPermissions());
    }

    public void addUserInternal(User user) {
        users.add(user);
    }
    public UUID createUser(String username, String eMail, String password) {
        UUID id = MySQL.getInstance().createAccount(eMail, password, username);
        User u = new User(username, password, eMail, id);
        u.setAccountStatus(AccountStatus.getStatus(UUID.fromString("8a2a6d9f-b017-11f0-a242-bc2411718ef7")));
        addUserInternal(u);
        return id;
    }

    public User getUserByName(String userName) {
        for (User user : users) if (user.getUserName().equals(userName)) return user;
        return null;
    }
    public User getUserByUUID(UUID uuid) {
        for (User u : users) if (u.getUniqueID().equals(uuid)) return u;
        return null;
    }
    public User getUserByEmail(String email) {
        for (User u : users) if (u.getEMail().equals(email)) return u;
        return null;
    }
}
