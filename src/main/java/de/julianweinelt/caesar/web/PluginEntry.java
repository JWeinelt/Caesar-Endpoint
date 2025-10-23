package de.julianweinelt.caesar.web;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
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

    private boolean waitingForAppoval = true;

    public PluginEntry(UUID uniqueId, String name, String version,
                       String author, String description, String descriptionLong,
                       String[] compatibleVersions, int downloads, String license,
                       String[] tags, String sourceCode, String sponsorLink,
                       String wikiLink, Date lastUpdated, Date dateCreated,
                       float rating, UUID[] screenshots) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.version = version;
        this.author = author;
        this.description = description;
        this.descriptionLong = descriptionLong;
        this.compatibleVersions = compatibleVersions;
        this.downloads = downloads;
        this.license = license;
        this.tags = tags;
        this.sourceCode = sourceCode;
        this.sponsorLink = sponsorLink;
        this.wikiLink = wikiLink;
        this.lastUpdated = lastUpdated;
        this.dateCreated = dateCreated;
        this.rating = rating;
        this.screenshots = screenshots;
    }

    public PluginEntry() {}
}