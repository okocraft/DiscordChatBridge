package net.okocraft.discordchatbridge.data;

import com.github.siroshun09.mcmessage.util.Colorizer;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public final class ReceivedMessages {

    private final Set<String> messages = new HashSet<>();

    public void add(@NotNull String message){
        message = Colorizer.stripMarkedColorCode(message);
        message = Colorizer.stripColorCode(message);
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
