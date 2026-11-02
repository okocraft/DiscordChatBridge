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

import net.dv8tion.jda.api.entities.Message;
import net.okocraft.discordchatbridge.constant.Constants;
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

    public static String formatMessage(@NotNull String top, @NotNull Collection<String> lists) {
        var newline = Constants.LINE_SEPARATOR;
        var opening = newline + "```" + newline;
        var closing = newline + "```";
        var body = new StringBuilder();
        for (var list : lists) {
            body.append(list).append(newline);
        }

        // Separate embedded backticks so only the generated fences delimit the code block.
        top = top.replaceAll("(?<=`)(?=`)", "\u200b");
        var content = body.toString().replaceAll("(?<=`)(?=`)", "\u200b");
        var maxContent = Message.MAX_CONTENT_LENGTH - opening.length() - closing.length();
        top = truncate(top, maxContent - (content.isEmpty() ? 0 : 1));
        content = truncate(content, maxContent - top.codePointCount(0, top.length()));
        return top + opening + content + closing;
    }

    private static String truncate(String text, int maxCodePoints) {
        if (text.codePointCount(0, text.length()) <= maxCodePoints) {
            return text;
        }
        return text.substring(0, text.offsetByCodePoints(0, maxCodePoints - 1)) + "…";
    }

    private PlayerListFormatter() {
        throw new UnsupportedOperationException();
    }
}
