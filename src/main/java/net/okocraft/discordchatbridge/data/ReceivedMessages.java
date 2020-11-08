/*
 *     Copyright (c) 2020 Okocraft
 *
 *     This file is part of DiscordChatBridge.
 *
 *     DiscordChatBridge is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     DiscordChatBridge is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with DiscordChatBridge. If not, see <https://www.gnu.org/licenses/>.
 */

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
