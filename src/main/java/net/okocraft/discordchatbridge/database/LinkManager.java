package net.okocraft.discordchatbridge.database;

import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public interface LinkManager {

    void init() throws Exception;
    Optional<LinkedUser> getLinkByUUID(UUID uuid);
    Optional<LinkedUser> getLinkByName(String name);
    Optional<LinkedUser> getLinkByDiscordUserId(long discordUserId);
    LinkedUser link(UUID uuid, String name, long discordUserId);
    boolean updateLink(LinkedUser user, @Nullable String name);
    boolean addDiscordUserId(LinkedUser user, long discordUserId);
    boolean removeDiscordUserId(LinkedUser user, long discordUserId);
    default void shutdown() {}

}
