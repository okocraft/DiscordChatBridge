package net.okocraft.discordchatbridge.database;

import com.github.siroshun09.configapi.api.Configuration;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FlatFileLinkManager extends LinkManagerImpl {

    private final YamlConfiguration configuration;

    public FlatFileLinkManager(DiscordChatBridgePlugin plugin) {
        super(plugin);
        configuration = YamlConfiguration.create(plugin.getDataDirectory().resolve("data.yml"));
    }

    @Override
    public void init() {
        load();
    }

    @Override
    public void shutdown() {
        flush();
    }

    @Override
    public void flush() {
        try (var yaml = configuration.copy()) {
            exportLinkedUsers(yaml);
            yaml.save();
        } catch (IOException e) {
            plugin.getWrappedLogger().error("Could not save to data.yml", e);
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

    private void load() {
        try (var yaml = configuration.copy()) {
            yaml.load();
            restoreLinkedUsers(yaml);
        } catch (IOException e) {
            plugin.getWrappedLogger().error("Could not load data.yml", e);
        }
    }

    private void restoreLinkedUsers(@NotNull Configuration yaml) {
        for (String key : yaml.getKeyList()) {
            UUID uuid;

            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                plugin.getWrappedLogger().warning("Invalid UUID in data.yml: " + key);
                continue;
            }

            List<Long> discordUserIds = yaml.getLongList(key + ".discord-user-id");

            if (discordUserIds.isEmpty()) {
                continue;
            }

            String name = yaml.getString(key + ".name");

            LinkedUser user = new LinkedUser(uuid, name, discordUserIds);

            linkCacheByUUID.put(UUID.fromString(key), user);
            linkCacheByName.put(name, user);

            discordUserIds.forEach(id -> linkCacheByDiscordUserId.put(id, user));
        }
    }

    private void exportLinkedUsers(@NotNull Configuration target) throws IOException {
        linkCacheByUUID.forEach((uuid, user) -> {
            target.set(uuid + ".name", user.getName());
            target.set(uuid + ".discord-user-id", user.getDiscordUserIds());
        });
    }
}
