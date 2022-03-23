/*
 *     Copyright (c) 2022 Okocraft
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

import net.okocraft.discordchatbridge.constant.Placeholders;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class PlayerListFormatter {

    public static String format(@NotNull String format, @NotNull String serverName,
                                @NotNull Collection<String> players) {
        return format
                .replace(Placeholders.PLAYER_COUNT, String.valueOf(players.size()))
                .replace(Placeholders.SERVER_NAME, serverName)
                .replace(Placeholders.PLAYER_LIST, String.join(", ", players));
    }

    private PlayerListFormatter() {
        throw new UnsupportedOperationException();
    }
}
