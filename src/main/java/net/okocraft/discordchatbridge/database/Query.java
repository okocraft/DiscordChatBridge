package net.okocraft.discordchatbridge.database;

public enum Query {

    CREATE_TABLE(
            "",
            "CREATE TABLE IF NOT EXISTS links(" +
                    "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "uuid TEXT NOT NULL, " +
                    "name TEXT, " +
                    "discord_user_id BIGINT NOT NULL UNIQUE, " +
                    "created_at BIGINT NOT NULL, " +
                    "updated_at BIGINT NOT NULL" +
                    ");"
    ),

    CREATE_UUID_INDEX(
            "",
            "CREATE INDEX IF NOT EXISTS idx_links_01 ON links(uuid)"
    ),

    CREATE_NAME_INDEX(
            "",
            "CREATE INDEX IF NOT EXISTS idx_links_02 ON links(name)"
    ),

    SELECT_ENTRY_BY_UUID(
            "",
            "SELECT name, discord_user_id FROM links WHERE uuid = ?"
    ),

    SELECT_ENTRY_BY_NAME(
            "",
            "SELECT uuid, discord_user_id FROM links WHERE name = ?"
    ),

    SELECT_ENTRY_BY_DISCORD_USER_ID(
            "",
            "SELECT uuid, name FROM links WHERE discord_user_id = ?"
    ),

    INSERT_ENTRY(
            "",
            "INSERT INTO links(uuid, name, discord_user_id, created_at, updated_at) VALUES(?, ?, ?, ?, ?)"
    ),

    DELETE_ENTRY_BY_DISCORD_USER_ID(
            "",
            "DELETE FROM links WHERE discord_user_id = ?"
    ),

    UPDATE_ENTRY_NAME(
            "",
            "UPDATE links SET name = ?, updated_at = ? WHERE uuid = ?"
    );

    private final String mysqlQuery;
    private final String sqliteQuery;

    Query(String mysqlQuery, String sqliteQuery) {
        this.mysqlQuery = mysqlQuery;
        this.sqliteQuery = sqliteQuery;
    }

    public String getQuery(Type databaseType) {
        if (databaseType == Type.MYSQL) {
            return mysqlQuery;
        } else if (databaseType == Type.SQLITE) {
            return sqliteQuery;
        }

        throw new AssertionError();
    }

    public enum Type {
        SQLITE, MYSQL;
    }
}
