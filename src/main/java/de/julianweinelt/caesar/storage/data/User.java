package de.julianweinelt.caesar.storage.data;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.UUID;

@Getter
public class User {
    @Setter
    private String userName;
    @Setter
    private String password;
    @Setter
    private String eMail;
    private final UUID uniqueID;

    @Setter
    private String description;
    @Setter
    private boolean verified;
    @Setter
    private AccountStatus accountStatus;
    private long created;
    @Setter
    private long lastOnline;

    public User(String userName, String password,
                String eMail, UUID uniqueID, String description, boolean verified, AccountStatus accountStatus, long created, long lastOnline) {
        this.userName = userName;
        this.password = password;
        this.eMail = eMail;
        this.uniqueID = uniqueID;
        this.description = description;
        this.verified = verified;
        this.accountStatus = accountStatus;
        this.created = created;
        this.lastOnline = lastOnline;
    }

    public User(String userName, String password, String eMail, UUID uniqueID) {
        this.userName = userName;
        this.password = password;
        this.eMail = eMail;
        this.uniqueID = uniqueID;
        description = "";
        verified = false;
    }

    public boolean hasPermission(String permission) {
        return UserManager.getInstance().getPermissions().getOrDefault(uniqueID, new ArrayList<>()).contains(permission);
    }
}
