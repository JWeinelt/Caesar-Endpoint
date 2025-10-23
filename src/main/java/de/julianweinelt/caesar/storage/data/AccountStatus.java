package de.julianweinelt.caesar.storage.data;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record AccountStatus(UUID id, String name, boolean active) {
    @Getter
    private static final List<AccountStatus> accountStatuses = new ArrayList<>();

    public static void addStatus(AccountStatus accountStatus) {
        accountStatuses.add(accountStatus);
    }
    public static AccountStatus getStatus(UUID id) {
        for (AccountStatus accountStatus : accountStatuses) if (accountStatus.id.equals(id)) return accountStatus;
        return null;
    }
    public static AccountStatus byName(String name) {
        for (AccountStatus a : accountStatuses) if (a.name.equals(name)) return a;
        return null;
    }
}