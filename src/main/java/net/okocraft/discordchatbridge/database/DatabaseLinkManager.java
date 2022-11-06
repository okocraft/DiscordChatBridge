package net.okocraft.discordchatbridge.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import org.jetbrains.annotations.Nullable;

public class DatabaseLinkManager extends LinkManagerImpl {

    private final Query.Type databaseType;

    private Connection connection;

    public DatabaseLinkManager(DiscordChatBridgePlugin plugin, Query.Type type) {
        super(plugin);
        this.databaseType = type;
    }

    public void init() throws Exception {
        Path dbFilePath = plugin.getDataDirectory().resolve("data.db");
            if (Files.notExists(dbFilePath)) {
                Files.createDirectories(dbFilePath.getParent());
                Files.createFile(dbFilePath);
            }
        this.connection = createSQLiteConnection(dbFilePath);
        try (PreparedStatement statement = connection.prepareStatement(Query.CREATE_TABLE.getQuery(databaseType))) {
            statement.executeUpdate();
        }
    }

    private Connection createSQLiteConnection(Path path) throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection("jdbc:sqlite:" + path.toString());
    }

    @Override
    public void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected Optional<LinkedUser> queryLinkByUUID(UUID uuid) {
        try (PreparedStatement statement = connection.prepareStatement(
                Query.SELECT_ENTRY_BY_UUID.getQuery(databaseType))) {
            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int internalId = rs.getInt("id");
                String name = rs.getString("name");
                int discordUserId = rs.getInt("discord_user_id");
                return Optional.of(new LinkedUser(internalId, uuid, name, discordUserId));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<LinkedUser> queryLinkByName(String name) {
        try (PreparedStatement statement = connection.prepareStatement(
                Query.SELECT_ENTRY_BY_NAME.getQuery(databaseType))) {
            statement.setString(1, name);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                try {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    int internalId = rs.getInt("id");
                    int discordUserId = rs.getInt("discord_user_id");
                    return Optional.of(new LinkedUser(internalId, uuid, name, discordUserId));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<LinkedUser> queryLinkByDiscordUserId(long discordUserId) {
        try (PreparedStatement statement = connection.prepareStatement(
                Query.SELECT_ENTRY_BY_DISCORD_USER_ID.getQuery(databaseType))) {
            statement.setLong(1, discordUserId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                try {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    int internalId = rs.getInt("id");
                    String name = rs.getString("name");
                    return Optional.of(new LinkedUser(internalId, uuid, name, discordUserId));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    protected LinkedUser insertLink(UUID uuid, String name, long discordUserId) {
        try (PreparedStatement statement = connection.prepareStatement(
                Query.INSERT_ENTRY.getQuery(databaseType))) {
            statement.setString(1, uuid.toString());
            statement.setString(2, name);
            statement.setLong(3, discordUserId);
            statement.setLong(4, System.currentTimeMillis());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return getLinkByUUID(uuid).orElseThrow(
                () -> new IllegalArgumentException("Failed to register account linkage."));
    }

    @Override
    protected boolean updateName(LinkedUser user, @Nullable String name) {
        try (PreparedStatement statement = connection.prepareStatement(
                Query.UPDATE_ENTRY_NAME.getQuery(databaseType))) {
            statement.setString(1, name);
            statement.setString(2, user.getUniqueId().toString());
            statement.setLong(3, System.currentTimeMillis());
            statement.executeUpdate();
            user.setName(name);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected boolean updateDiscordUserId(LinkedUser user, long discordUserId) {

        try (PreparedStatement statement = connection.prepareStatement(
                Query.UPDATE_DISCORD_USER_ID.getQuery(databaseType))) {
            statement.setLong(1, discordUserId);
            statement.setString(2, user.getUniqueId().toString());
            statement.setLong(3, System.currentTimeMillis());
            statement.executeUpdate();
            user.setDiscordUserId(discordUserId);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}
