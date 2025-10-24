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

