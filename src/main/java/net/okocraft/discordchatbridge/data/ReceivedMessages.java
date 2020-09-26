package net.okocraft.discordchatbridge.data;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public final class ReceivedMessages {

    private final Set<String> messages = new HashSet<>();

    public void add(@NotNull String message){
        synchronized (messages) {
            messages.add(message);
        }
    }

    public boolean contains(@NotNull String message) {
        return messages.contains(message);
    }

    public void remove(@NotNull String message) {
        synchronized (messages) {
            messages.remove(message);
        }
    }

    public void clear() {
        synchronized (messages) {
            messages.clear();
        }
    }
}
