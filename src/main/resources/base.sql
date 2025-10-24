use caesar_market_test;


create table account_followers
(
    AccountID        varchar(36) not null
        primary key,
    AccountFollowing varchar(36) null
);

create table account_status
(
    StatusID   char(36)          not null
        primary key,
    StatusName varchar(50)       null,
    Active     tinyint default 1 null
);

create table accounts
(
    AccountID      char(36)                            not null
        primary key,
    UserName       varchar(20)                         not null,
    IsDeveloper    tinyint    default 0                not null,
    IsVerified     tinyint    default 0                not null,
    AccountStatus  char(36)                            not null,
    AccountCreated mediumtext                          not null,
    eMail          varchar(100)                        null,
    PasswordHashed varchar(200)                        null,
    Description    varchar(2000)                       null,
    LastOnline     mediumtext default unix_timestamp() not null
);

create table badges
(
    ID    varchar(36) default uuid() not null
        primary key,
    Name  varchar(20)                null,
    Color varchar(10)                null
);

create table account_badge_assignment
(
    AccountID varchar(36) null,
    BadgeID   varchar(36) null,
    constraint account_badge_assignment_accounts_AccountID_fk
        foreign key (AccountID) references accounts (AccountID),
    constraint account_badge_assignment_badges_ID_fk
        foreign key (BadgeID) references badges (ID)
);
create table build_types
(
    ID            char(36)     not null
        primary key,
    Name          varchar(60)  not null,
    NameLocalized varchar(100) not null,
    GitHubBranch  varchar(30)  not null
);

create table caesar_software_parts
(
    ID            char(36)     not null
        primary key,
    Name          varchar(60)  not null,
    NameLocalized varchar(100) not null,
    GitHubRepo    varchar(100) not null
);



create table files
(
    FileHash char(36)                                                   not null
        primary key,
    FileType enum ('IMAGE_JPG', 'IMAGE_PNG', 'IMAGE_GIF', 'PLUGIN_JAR') not null,
    FileSize int default 0                                              not null
);

create table image_meta
(
    ImageID  varchar(36)  not null
        primary key,
    DataType varchar(100) null
);

create table plugin_entries
(
    uuid                char(36)                                 not null
        primary key,
    name                varchar(255)                             not null,
    version             varchar(50)                              not null,
    author              char(36)                                 not null,
    description         text                                     null,
    description_long    text                                     null,
    compatible_versions longtext collate utf8mb4_bin             null
        check (json_valid(`compatible_versions`)),
    downloads           int unsigned default 0                   null,
    license             varchar(100)                             null,
    tags                longtext collate utf8mb4_bin             null
        check (json_valid(`tags`)),
    source_code         varchar(2083)                            null,
    sponsor_link        varchar(2083)                            null,
    wiki_link           varchar(2083)                            null,
    last_updated        timestamp                                null,
    date_created        timestamp    default current_timestamp() not null,
    rating              float        default 0                   null,
    screenshots         longtext collate utf8mb4_bin             null
        check (json_valid(`screenshots`))
);

create table plugin_ratings
(
    PluginID       varchar(36)                          not null,
    AccountID      varchar(36)                          not null,
    Rating         int                                  not null,
    ServerVersions longtext collate utf8mb4_bin         null,
    CaesarVersion  varchar(36)                          null,
    CreationDate   datetime default current_timestamp() not null
        check (json_valid(`ServerVersions`)),
    constraint caesar_pl_rate_pluginID_fk
        foreign key (PluginID) references plugin_entries (uuid)
);

create table plugin_versions
(
    PluginID    char(36)    not null,
    VersionName varchar(20) null,
    VMajor      int         null,
    VMinor      int         null,
    VPatch      int         null,
    FileHash    char(36)    not null,
    constraint caesar_pl_ver_file_fk
        foreign key (FileHash) references files (FileHash),
    constraint caesar_pl_ver_pluginID_fk
        foreign key (PluginID) references plugin_entries (uuid)
);
create table caesar_versions
(
    SoftwareID char(36)    not null,
    BuildType  char(36)    not null,
    VersionID  varchar(20) null,
    VMajor     int         null,
    VMinor     int         null,
    VPatch     int         null,
    Latest     tinyint     null,
    FileHash   char(36)    null,
    constraint caesar_versions_build_fk
        foreign key (BuildType) references build_types (ID),
    constraint caesar_versions_file_hash_fk
        foreign key (FileHash) references files (FileHash),
    constraint caesar_versions_software_id_fk
        foreign key (SoftwareID) references caesar_software_parts (ID)
);

CREATE TABLE IF NOT EXISTS plugin_comments (
    RecordID varchar(36) NOT NULL PRIMARY KEY DEFAULT UUID(),
    PluginID varchar(36) NOT NULL,
    AuthorID varchar(36) NOT NULL,
    AuthorName varchar(36) NOT NULL,
    Content text NOT NULL,
    ThumbsUp int NOT NULL DEFAULT 0,
    ThumbsDown int NOT NULL DEFAULT 0,
    Creation bigint NOT NULL
);

ALTER TABLE plugin_comments
    ADD FOREIGN KEY (PluginID) REFERENCES plugin_entries(uuid),
    ADD FOREIGN KEY (AuthorID) REFERENCES accounts(AccountID);

/*--------------------------------*/

CREATE TABLE IF NOT EXISTS accounts_awaiting_u_confirm (
    AccountID varchar(36) NOT NULL PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS plugins_awaiting_approval (
    PluginID varchar(36) NOT NULL PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS audit_log (
    UserID varchar(36) NOT NULL,
    Action varchar(36) NOT NULL,
    OldValue text,
    NewValue text,
    CreationDate bigint
);

CREATE TABLE IF NOT EXISTS account_permissions (
    AccountID varchar(36) NOT NULL PRIMARY KEY,
    PermissionName text NOT NULL
);

INSERT INTO account_status (StatusID, StatusName, Active) VALUES
                                                              (UUID(), 'Created', 1),
                                                              (UUID(), 'Active', 1),
                                                              (UUID(), 'Banned', 1),
                                                              (UUID(), 'Deleted', 1);

ALTER TABLE plugin_entries
    ADD COLUMN State enum('REQUESTED', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'REQUESTED';

/*-------------------------------------*/

CREATE TABLE IF NOT EXISTS mc_plugins (
    PluginID char(36) NOT NULL PRIMARY KEY,
    Name varchar(100) NOT NULL,
    Status enum('REQUESTED', 'APPROVED'),
    Author varchar(100) NOT NULL,
    INDEX (Author),
    INDEX (Name),
    INDEX (Status)
);

CREATE TABLE IF NOT EXISTS mc_plugin_sources (
     PluginID CHAR(36) NOT NULL,
     Source ENUM('SPIGOT', 'MODRINTH', 'HANGAR') NOT NULL,
     ResourceID VARCHAR(50) NOT NULL,
     PRIMARY KEY (PluginID, Source),
     FOREIGN KEY (PluginID) REFERENCES mc_plugins (PluginID),
     INDEX(ResourceID)
);

CREATE TABLE IF NOT EXISTS mc_server_software (
    SoftwareID char(36) NOT NULL PRIMARY KEY,
    Name varchar(100) NOT NULL,
    ForkOf char(36) NULL,
    API enum('BUKKIT', 'NUKKIT', 'WATERDOG', 'MINESTOM', 'FORGE', 'NEOFORGE', 'FABRIC', 'BUNGEECORD', 'VELOCITY', 'CUSTOM'),
    Legacy tinyint NOT NULL DEFAULT 0,
    Experimental tinyint NOT NULL DEFAULT 0,
    BedrockEdition tinyint NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS mc_operating_system (
    SystemID char(36) NOT NULL PRIMARY KEY,
    Name varchar(100) NOT NULL,
    VersionName varchar(30) NULL,
    NumericalVersion int NULL,
    Architecture enum('WINDOWS', 'LINUX', 'MACOS', 'BSD', 'UNKNOWN'),
    Legacy tinyint NOT NULL DEFAULT 0,
    Experimental tinyint NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS mc_version (
    VersionID char(36) NOT NULL PRIMARY KEY,
    VersionName varchar(50) NOT NULL,
    UpdateName varchar(100) NULL,
    ReleaseDate bigint NOT NULL,
    Supported tinyint NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS mc_plugin_rating (
    PluginID char(36) NOT NULL,
    VersionName varchar(100) NOT NULL,
    OperatingSystem char(36) NULL,
    MinecraftVersion char(36) NOT NULL,
    ServerSoftware char(36) NOT NULL,
    ServerSoftwareAdditionalInfo varchar(300) NULL,
    InstalledPlugins text NULL, /* Syntax: <pl-id>:<versionString>;... */
    Rating float NOT NULL,
    StatusDetail enum('CRASHING', 'BUGS', 'PERFORMANCE', 'RUNNING'),
    FOREIGN KEY (PluginID) REFERENCES mc_plugins(PluginID),
    FOREIGN KEY (OperatingSystem) REFERENCES mc_operating_system (SystemID),
    FOREIGN KEY (MinecraftVersion) REFERENCES mc_version (VersionID),
    FOREIGN KEY (ServerSoftware) REFERENCES mc_server_software (SoftwareID),
    INDEX (PluginID),
    INDEX (PluginID, VersionName),
    INDEX (MinecraftVersion),
    INDEX (ServerSoftware),
    INDEX (OperatingSystem)
);

CREATE VIEW IF NOT EXISTS 'mc_plugin_platform_ids' AS
    SELECT p.PluginID, Name,
           CASE WHEN s.Source = 'SPIGOT' THEN s.ResourceID ELSE NULL END AS SpigotID,
           CASE WHEN s.Source = 'MODRINTH' THEN s.ResourceID ELSE NULL END AS ModRinthID,
           CASE WHEN s.Source = 'HANGAR' THEN s.ResourceID ELSE NULL END AS HangarID
    FROM mc_plugins AS p LEFT JOIN mc_plugin_sources AS s ON s.PluginID = p.PluginID;

CREATE VIEW IF NOT EXISTS 'mc_plugin_rating_group' AS
    SELECT r.PluginID, r.VersionName, r.OperatingSystem, r.ServerSoftware, r.MinecraftVersion, AVG(r.Rating)
    FROM mc_plugin_rating AS r
GROUP BY r.PluginID, r.VersionName, r.OperatingSystem, r.ServerSoftware, r.MinecraftVersion;

CREATE VIEW IF NOT EXISTS 'mc_plugin_statuses' AS
    SELECT PluginID,
           SUM(CASE WHEN StatusDetail = 'CRASHING' THEN 1 ELSE 0 END) AS TimesCrashing,
           SUM(CASE WHEN StatusDetail = 'PERFORMANCE' THEN 1 ELSE 0 END) AS TimesPerformance,
           SUM(CASE WHEN StatusDetail = 'BUGS' THEN 1 ELSE 0 END) AS TimesBugs,
           SUM(CASE WHEN StatusDetail = 'RUNNING' THEN 1 ELSE 0 END) AS TimesRunning,
           COUNT(StatusDetail) AS TimesRecorded
    FROM mc_plugin_rating;

CREATE VIEW IF NOT EXISTS 'mc_plugin_operating_systems' AS
    SELECT PluginID, VersionName, OperatingSystem, COUNT(OperatingSystem) AS Amount
    FROM mc_plugin_rating
    GROUP BY PluginID, VersionName, OperatingSystem;

CREATE VIEW IF NOT EXISTS 'mc_plugin_server_softwares' AS
    SELECT PluginID, VersionName, ServerSoftware, COUNT(ServerSoftware) AS Amount
    FROM mc_plugin_rating
    GROUP BY PluginID, VersionName, ServerSoftware;

