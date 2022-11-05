package net.okocraft.discordchatbridge.database;

public enum Query {

    CREATE_TABLE(
            "",
            "CREATE TABLE IF NOT EXISTS links(" +
                    "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "uuid TEXT NOT NULL UNIQUE, " +
                    "name TEXT UNIQUE, " +
                    "discord_user_id BIGINT NOT NULL UNIQUE, " +
                    "created_at BIGINT NOT NULL, " +
                    "updated_at BIGINT NOT NULL DEFAULT -1" +
                    ");"
    ),

    SELECT_ENTRY_BY_UUID(
            "",
            "SELECT id, name, discord_user_id FROM links WHERE uuid = ?"
    ),

    SELECT_ENTRY_BY_NAME(
            "",
            "SELECT id, uuid, discord_user_id FROM links WHERE name = ?"
    ),

    SELECT_ENTRY_BY_DISCORD_USER_ID(
            "",
            "SELECT id, uuid, name FROM links WHERE discord_user_id = ?"
    ),

    INSERT_ENTRY(
            "",
            "INSERT INTO links(uuid, name, discord_user_id, created_at) VALUES(?, ?, ?, ?)"
    ),

    DELETE_ENTRY_BY_DISCORD_USER_ID(
            "",
            "DELETE FROM links WHERE discord_user_id = ?"
    ),

    UPDATE_ENTRY_NAME(
            "",
            "UPDATE links SET name = ? WHERE uuid = ?"
    ),

    UPDATE_DISCORD_USER_ID(
            "",
            "UPDATE links SET discord_user_id = ? WHERE uuid = ?"
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
