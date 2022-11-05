package net.okocraft.discordchatbridge.database;

import java.util.UUID;

public class LinkedUser {

    private final int internalId;
    private final UUID uuid;
    private String name;
    private long discordUserId;

    LinkedUser(int internalId, UUID uuid, String name, long discordUserId) {
        this.internalId = internalId;
        this.uuid = uuid;
        this.name = name;
        this.discordUserId = discordUserId;
    }

    int getInternalId() {
        return internalId;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public long getDiscordUserId() {
        return discordUserId;
    }

    void setName(String name) {
        this.name = name;
    }

    void setDiscordUserId(long discordUserId) {
        this.discordUserId = discordUserId;
    }
}
