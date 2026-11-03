/*
 *     Copyright (c) 2025 Okocraft
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

package net.okocraft.discordchatbridge.util;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class FirstJoinPlayerHolder implements AutoCloseable {

    private final Set<UUID> players = new HashSet<>();
    private boolean closed;

    public synchronized void add(@NotNull UUID uuid) {
        if (!this.closed) {
            this.players.add(uuid);
        }
    }

    public synchronized boolean remove(@NotNull UUID uuid) {
        return this.players.remove(uuid);
    }

    @Override
    public synchronized void close() {
        this.closed = true;
        this.players.clear();
    }
}
