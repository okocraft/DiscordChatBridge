package net.okocraft.discordchatbridge;

import org.jetbrains.annotations.NotNull;

public class LinkedChannel {

    private final String channelName;
    private final long id;

    public LinkedChannel(@NotNull String channelName, long id) {
        this.channelName = channelName;
        this.id = id;
    }

    @NotNull
    public String getChannelName() {
        return channelName;
    }

    public long getId() {
        return id;
    }
}
