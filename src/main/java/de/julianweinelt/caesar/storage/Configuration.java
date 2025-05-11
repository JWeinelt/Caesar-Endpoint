package de.julianweinelt.caesar.storage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Configuration {
    private String databaseHost = "localhost";
    private int databasePort = 3306;
    private String databaseName = "caesar";
    private String databaseUser = "caesar";
    private String databasePassword = "secret";
}