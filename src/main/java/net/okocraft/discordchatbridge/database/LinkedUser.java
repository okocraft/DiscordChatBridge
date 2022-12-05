package net.okocraft.discordchatbridge.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.UnmodifiableView;

public class LinkedUser {

    private final UUID uuid;
    private String name;
    private final List<Long> discordUserIds;

    LinkedUser(UUID uuid, String name, List<Long> discordUserIds) {
        this.uuid = uuid;
        this.name = name;
        this.discordUserIds = new ArrayList<>(discordUserIds);
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    @UnmodifiableView
    public List<Long> getDiscordUserIds() {
        return Collections.unmodifiableList(discordUserIds);
    }

    void setName(String name) {
        this.name = name;
    }

    void addDiscordUserId(long newDiscordUserId) {
        discordUserIds.add(newDiscordUserId);
    }

    boolean removeDiscordUserId(long discordUserId) {
        return discordUserIds.remove(discordUserId);
    }
}
