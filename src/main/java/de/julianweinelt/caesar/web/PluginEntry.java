package de.julianweinelt.caesar.web;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class PluginEntry {
    private UUID uniqueId;
    private String name;
    private String version;
    private String author;
    private String description;
    private String descriptionLong;
    private String[] compatibleVersions;
    private boolean backwardsCompatible;
    private int downloads;
    private String license;
    private String[] tags;
    private String sourceCode;
    private String sponsorLink;
    private String wikiLink;
    private Date lastUpdated;
    private Date dateCreated;
    private float rating;
    private UUID[] screenshots;
    private PluginState state;
    private List<String> categories;

    private boolean waitingForAppoval = true;

    public PluginEntry() {}
}