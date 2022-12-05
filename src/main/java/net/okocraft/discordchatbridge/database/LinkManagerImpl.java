package net.okocraft.discordchatbridge.database;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import org.jetbrains.annotations.Nullable;

public abstract class LinkManagerImpl implements LinkManager {

    protected final DiscordChatBridgePlugin plugin;

    protected final Map<UUID, LinkedUser> linkCacheByUUID = new HashMap<>();
    protected final Map<String, LinkedUser> linkCacheByName = new HashMap<>();
    protected final Map<Long, LinkedUser> linkCacheByDiscordUserId = new HashMap<>();

    public LinkManagerImpl(DiscordChatBridgePlugin plugin) {
        this.plugin = plugin;
    }

    protected abstract Optional<LinkedUser> queryLinkByUUID(UUID uuid);

    protected abstract Optional<LinkedUser> queryLinkByName(String name);

    protected abstract Optional<LinkedUser> queryLinkByDiscordUserId(long discordUserId);

    public Optional<LinkedUser> getLinkByUUID(UUID uuid) {
        return Optional.ofNullable(linkCacheByUUID.computeIfAbsent(uuid, u -> queryLinkByUUID(u).orElse(null)));
    }

    public Optional<LinkedUser> getLinkByName(String name) {
        return Optional.ofNullable(linkCacheByName.computeIfAbsent(name, n -> queryLinkByName(n).orElse(null)));
    }

    public Optional<LinkedUser> getLinkByDiscordUserId(long discordUserId) {
        return Optional.ofNullable(linkCacheByDiscordUserId.computeIfAbsent(
                discordUserId,
                did -> queryLinkByDiscordUserId(did).orElse(null)
        ));
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
                if (addDiscordUserId(existingUuid.get(), discordUserId)) {
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

                LinkedUser created = insertLink(uuid, name, discordUserId);
                linkCacheByUUID.put(uuid, created);
                linkCacheByName.put(name, created);
                linkCacheByDiscordUserId.put(discordUserId, created);
                return created;
            }
        }
    }

    protected abstract LinkedUser insertLink(UUID uuid, String name, long discordUserId);

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

        return updateName(user, name);
    }

    protected abstract boolean updateName(LinkedUser user, @Nullable String name);

    public boolean addDiscordUserId(LinkedUser user, long discordUserId) {
        Optional<LinkedUser> existing = getLinkByDiscordUserId(discordUserId);
        if (existing.isPresent()) {
            return existing.get().getUniqueId().equals(user.getUniqueId());
        }

        if (execAddDiscordUserId(user, discordUserId)) {
            linkCacheByDiscordUserId.put(discordUserId, user);
            linkCacheByName.put(user.getName(), user);
            linkCacheByUUID.put(user.getUniqueId(), user);
            return true;
        } else {
            return false;
        }
    }

    protected abstract boolean execAddDiscordUserId(LinkedUser user, long discordUserId);

    @Override
    public boolean removeDiscordUserId(LinkedUser user, long discordUserId) {
        Optional<LinkedUser> existing = getLinkByDiscordUserId(discordUserId);
        if (existing.isEmpty()) {
            return true;
        }
        if (!existing.get().getUniqueId().equals(user.getUniqueId())) {
            return false;
        }
        if (execRemoveDiscordUserId(user, discordUserId)) {
            if (existing.get().getDiscordUserIds().isEmpty()) {
                linkCacheByDiscordUserId.remove(discordUserId);
                linkCacheByName.remove(user.getName());
                linkCacheByUUID.remove(user.getUniqueId());
            }
            return true;
        } else {
            return false;
        }
    }

    protected abstract boolean execRemoveDiscordUserId(LinkedUser user, long discordUserId);
}
