package net.okocraft.discordchatbridge.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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

        boolean autoCommit = connection.getAutoCommit();
        if (autoCommit) {
            connection.setAutoCommit(false);
        }

        try (Statement statement = connection.createStatement()) {
            statement.addBatch(Query.CREATE_TABLE.getQuery(databaseType));
            statement.addBatch(Query.CREATE_UUID_INDEX.getQuery(databaseType));
            statement.addBatch(Query.CREATE_NAME_INDEX.getQuery(databaseType));
            statement.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
        }

        if (autoCommit) {
            connection.setAutoCommit(true);
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
            List<Long> discordUserIds = new ArrayList<>();
            while (rs.next()) {
                discordUserIds.add(rs.getLong("discord_user_id"));
            }
            return Optional.of(new LinkedUser(uuid, rs.getString("name"), discordUserIds));

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
            List<Long> discordUserIds = new ArrayList<>();
            while (rs.next()) {
                discordUserIds.add(rs.getLong("discord_user_id"));
            }
            try {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                return Optional.of(new LinkedUser(uuid, name, discordUserIds));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return Optional.empty();
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
                    String name = rs.getString("name");
                    return Optional.of(new LinkedUser(uuid, name, List.of(discordUserId)));
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
            statement.setLong(5, System.currentTimeMillis());
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
            statement.setLong(2, System.currentTimeMillis());
            statement.setString(3, user.getUniqueId().toString());
            statement.executeUpdate();
            user.setName(name);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected boolean execAddDiscordUserId(LinkedUser user, long discordUserId) {
        if (!user.getDiscordUserIds().contains(discordUserId)) {
            insertLink(user.getUniqueId(), user.getName(), discordUserId);
            user.addDiscordUserId(discordUserId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean execRemoveDiscordUserId(LinkedUser user, long discordUserId) {
        try (PreparedStatement statement = connection.prepareStatement(
                Query.DELETE_ENTRY_BY_DISCORD_USER_ID.getQuery(databaseType))) {
            statement.setLong(1, discordUserId);
            statement.executeUpdate();
            user.removeDiscordUserId(discordUserId);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
