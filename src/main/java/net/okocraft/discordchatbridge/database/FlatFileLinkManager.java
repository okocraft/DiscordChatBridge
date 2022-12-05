package net.okocraft.discordchatbridge.database;

import com.github.siroshun09.configapi.api.Configuration;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlatFileLinkManager extends LinkManagerImpl {

    private final YamlConfiguration configuration;

    public FlatFileLinkManager(DiscordChatBridgePlugin plugin) {
        super(plugin);
        configuration = YamlConfiguration.create(plugin.getDataDirectory().resolve("data.yml"));
    }

    @Override
    public void init() throws Exception {
        if (Files.notExists(plugin.getDataDirectory())) {
            Files.createDirectories(plugin.getDataDirectory());
            Files.createFile(configuration.getPath());
        }

        configuration.load();

        for (String key : configuration.getKeyList()) {
            try {
                Configuration data = configuration.getOrCreateSection(key);
                UUID uuid = UUID.fromString(key);
                String name = data.getString("name");
                List<Long> discordUserIds = data.getLongList("discord-user-id");
                if (discordUserIds.isEmpty()) {
                    continue;
                }
                LinkedUser user = new LinkedUser(uuid, name, discordUserIds);
                linkCacheByUUID.put(UUID.fromString(key), user);
                linkCacheByName.put(name, user);
                discordUserIds.forEach(id -> linkCacheByDiscordUserId.put(id, user));
            } catch (IllegalArgumentException ignored) {
            }

        }
    }

    @Override
    public void shutdown() {
        configuration.clear();
        linkCacheByUUID.forEach((uuid, user) -> {
            configuration.set(uuid + ".name", user.getName());
            configuration.set(uuid + ".discord-user-id", user.getDiscordUserIds());
        });

        try {
            configuration.save();
        } catch (IOException e) {
            plugin.getWrappedLogger().error(e.getMessage(), e);
        }
    }

    @Override
    protected Optional<LinkedUser> queryLinkByUUID(@NotNull UUID uuid) {
        return Optional.empty();
    }

    @Override
    protected Optional<LinkedUser> queryLinkByName(String name) {
        return Optional.empty();
    }

    @Override
    protected Optional<LinkedUser> queryLinkByDiscordUserId(long discordUserId) {
        return Optional.empty();
    }

    @Override
    protected LinkedUser insertLink(UUID uuid, String name, long discordUserId) {
        return new LinkedUser(uuid, name, List.of(discordUserId));
    }

    @Override
    protected boolean updateName(LinkedUser user, @Nullable String name) {
        user.setName(name);
        return true;
    }

    @Override
    protected boolean execAddDiscordUserId(LinkedUser user, long discordUserId) {
        user.addDiscordUserId(discordUserId);
        return true;
    }

    @Override
    protected boolean execRemoveDiscordUserId(LinkedUser user, long discordUserId) {
        user.removeDiscordUserId(discordUserId);
        return true;
    }

}
