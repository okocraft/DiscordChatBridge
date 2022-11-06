package net.okocraft.discordchatbridge.session;

import java.util.UUID;

public class LinkRequestEntry {
    private final UUID minecraftUuid;
    private final String minecraftName;

    LinkRequestEntry(UUID minecraftUuid, String minecraftName) {
        this.minecraftUuid = minecraftUuid;
        this.minecraftName = minecraftName;
    }

    public UUID getMinecraftUuid() {
        return this.minecraftUuid;
    }

    public String getMinecraftName() {
        return this.minecraftName;
    }
}
