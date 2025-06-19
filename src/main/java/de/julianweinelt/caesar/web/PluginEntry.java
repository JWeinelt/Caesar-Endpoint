package de.julianweinelt.caesar.web;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class PluginEntry {
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
    private String[] screenshots;
}