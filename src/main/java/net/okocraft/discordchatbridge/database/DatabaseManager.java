package net.okocraft.discordchatbridge.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import org.jetbrains.annotations.Nullable;

public class DatabaseManager {

    private final Map<UUID, LinkedUser> linkCacheByUUID = new HashMap<>();
    private final Map<String, LinkedUser> linkCacheByName = new HashMap<>();
    private final Map<Long, LinkedUser> linkCacheByDiscordUserId = new HashMap<>();

    private final Query.Type databaseType;

    private final Connection connection;

    public DatabaseManager(DiscordChatBridgePlugin plugin) {
        this.databaseType = Query.Type.SQLITE;
        Path dbFilePath = plugin.getDataDirectory().resolve("data.db");
        try {
            if (Files.notExists(dbFilePath)) {
                Files.createDirectories(dbFilePath.getParent());
                Files.createFile(dbFilePath);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        this.connection = createSQLiteConnection(dbFilePath)
                .orElseThrow(() -> new IllegalStateException("Cannot create sqlite database connection."));

        try (PreparedStatement statement = connection.prepareStatement(Query.CREATE_TABLE.getQuery(databaseType))) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public Optional<Connection> createSQLiteConnection(Path path) {
        try {
            Class.forName("org.sqlite.JDBC");
            return Optional.of(DriverManager.getConnection("jdbc:sqlite:" + path.toString()));
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Optional<LinkedUser> getLinkByUUID(UUID uuid) {
        return Optional.ofNullable(linkCacheByUUID.computeIfAbsent(uuid, u -> queryLinkByUUID(u).orElse(null)));
    }

    private Optional<LinkedUser> queryLinkByUUID(UUID uuid) {
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

    public Optional<LinkedUser> getLinkByName(String name) {
        return Optional.ofNullable(linkCacheByName.computeIfAbsent(name, n -> queryLinkByName(n).orElse(null)));
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

    public Optional<LinkedUser> getLinkByDiscordUserId(long discordUserId) {
        return Optional.ofNullable(linkCacheByDiscordUserId.computeIfAbsent(
                discordUserId,
                did -> queryLinkByDiscordUserId(did).orElse(null)
        ));
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

    public LinkedUser link(UUID uuid, String name, long discordUserId) {
        Optional<LinkedUser> existingUuid = getLinkByUUID(uuid);
        Optional<LinkedUser> existingDiscordUserId = getLinkByDiscordUserId(discordUserId);

        if (existingUuid.isPresent()) {
            if (existingDiscordUserId.isPresent()) {
                // uuid conflict, discordUserId conflict
                boolean isSame = existingUuid.get().getUniqueId() == existingDiscordUserId.get().getUniqueId();
                if (isSame && !existingUuid.get().getName().equals(name)) {
                    updateLink(existingUuid.get(), name);
                }
                return isSame ? existingUuid.get() : null;
            } else {
                // uuid conflict, discordUserId ok
                if (updateLink(existingUuid.get(), discordUserId)) {
                    if (!existingUuid.get().getName().equals(name)) {
                        updateLink(existingUuid.get(), name);
                    }
                    return existingUuid.get();
                } else {
                    return null;
                }
            }
        } else {
            if (existingDiscordUserId.isPresent()) {
                // uuid ok, discordUserId conflict
                return null;
            } else {
                // uuid ok, discordUserId ok
                getLinkByName(name).ifPresent(linkedUser -> updateLink(linkedUser, null));

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
        }
    }

    public boolean updateLink(LinkedUser user, @Nullable String name) {
        if (name != null) {
            Optional<LinkedUser> existing = getLinkByName(name);
            if (existing.isPresent()) {
                if (existing.get().getUniqueId().equals(user.getUniqueId())) {
                    return true;
                } else {
                    updateLink(existing.get(), null);
                }
            }
        }

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

    public boolean updateLink(LinkedUser user, long discordUserId) {
        Optional<LinkedUser> existing = getLinkByDiscordUserId(discordUserId);
        if (existing.isPresent()) {
            return existing.get().getUniqueId().equals(user.getUniqueId());
        }

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
