/*
 *     Copyright (c) 2026 Okocraft
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerListFormatterTest {

    @Test
    void testFormatReplacesPlaceholdersAndKeepsPlayerOrder() {
        var players = List.of("Bob", "Alice", "プレイヤー😀");

        var result = PlayerListFormatter.format(
                "%server_name% (%player_count%): %player_list% / %player_count%", "survival", players);

        assertEquals("survival (3): Bob, Alice, プレイヤー😀 / 3", result);
    }

    @Test
    void testFormatHandlesEmptyPlayerList() {
        assertEquals("survival (0): ", PlayerListFormatter.format(
                "%server_name% (%player_count%): %player_list%", "survival", List.of()));
    }

    @Test
    void testFormatPreservesLiteralTextAndUnknownPlaceholders() {
        var format = "  Player list\n%unknown%  ";
        assertEquals(format, PlayerListFormatter.format(format, "survival", List.of("Alice")));
    }
    @Test
    void testMessagePreservesMultipleServerLinesAndClosesCodeBlock() {
        var newline = Constants.LINE_SEPARATOR;
        assertEquals("Players (3)" + newline + "```" + newline
                        + "survival: Alice, Bob" + newline + "creative: Carol" + newline + newline + "```",
                PlayerListFormatter.formatMessage("Players (3)", List.of("survival: Alice, Bob", "creative: Carol")));
    }

    @Test
    void testMessageWithNoServerLinesHasClosedEmptyCodeBlock() {
        var newline = Constants.LINE_SEPARATOR;
        assertEquals("Players (0)" + newline + "```" + newline + newline + "```",
                PlayerListFormatter.formatMessage("Players (0)", List.of()));
    }

    @ParameterizedTest
    @ValueSource(ints = {1999, 2000, 2001})
    void testMessageRespectsExactLengthBoundary(int originalLength) {
        var newline = Constants.LINE_SEPARATOR;
        var overhead = "Players".length() + 6 + 4 * newline.length();
        var line = "a".repeat(originalLength - overhead);

        var result = PlayerListFormatter.formatMessage("Players", List.of(line));

        assertEquals(Math.min(originalLength, Message.MAX_CONTENT_LENGTH), result.codePointCount(0, result.length()));
        assertTrue(result.endsWith(newline + "```"));
        if (originalLength <= Message.MAX_CONTENT_LENGTH) {
            assertTrue(result.contains(line + newline));
            assertFalse(result.contains("…"));
        } else {
            assertTrue(result.endsWith("…" + newline + "```"));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {2000, 2001})
    void testHeadingOnlyUsesAvailableSpaceBeforeTruncation(int originalLength) {
        var overhead = 6 + 3 * Constants.LINE_SEPARATOR.length();
        var heading = "a".repeat(originalLength - overhead);

        var result = PlayerListFormatter.formatMessage(heading, List.of());

        assertEquals(Message.MAX_CONTENT_LENGTH, result.codePointCount(0, result.length()));
        assertEquals(originalLength > Message.MAX_CONTENT_LENGTH, result.contains("…"));
        assertTrue(result.endsWith(Constants.LINE_SEPARATOR + "```"));
    }

    @Test
    void testMessageTruncatesManyServerLinesInOriginalOrder() {
        var servers = java.util.stream.IntStream.range(0, 1000)
                .mapToObj(i -> "server-" + i + ": Alice, Bob")
                .toList();

        var result = PlayerListFormatter.formatMessage("Players", servers);

        assertEquals(Message.MAX_CONTENT_LENGTH, result.codePointCount(0, result.length()));
        assertTrue(result.contains(servers.get(0) + Constants.LINE_SEPARATOR + servers.get(1)));
        assertTrue(result.endsWith("…" + Constants.LINE_SEPARATOR + "```"));
    }

    @Test
    void testMessageTruncatesLongHeadingAndKeepsClosedCodeBlock() {
        var result = PlayerListFormatter.formatMessage("😀".repeat(2100), List.of("survival: Alice"));

        assertEquals(Message.MAX_CONTENT_LENGTH, result.codePointCount(0, result.length()));
        assertEquals(2, result.split("```", -1).length - 1);
        assertFalse(result.contains("\ufffd"));
        assertTrue(result.endsWith("…" + Constants.LINE_SEPARATOR + "```"));
    }

    @Test
    void testMessageTruncationPreservesSupplementaryCodePoints() {
        var result = PlayerListFormatter.formatMessage("日本語😀", List.of("😀".repeat(2100)));

        assertEquals(Message.MAX_CONTENT_LENGTH, result.codePointCount(0, result.length()));
        assertTrue(result.codePoints().noneMatch(cp -> cp >= 0xd800 && cp <= 0xdfff));
        assertTrue(result.endsWith("…" + Constants.LINE_SEPARATOR + "```"));
    }

    @Test
    void testMessageSeparatesEmbeddedBackticksInHeadingAndBody() {
        var result = PlayerListFormatter.formatMessage("Head ```", List.of("server: ``````", "normal `tick`"));

        assertEquals(2, result.split("```", -1).length - 1);
        assertTrue(result.contains("`\u200b`\u200b`"));
        assertTrue(result.contains("normal `tick`"));
        assertTrue(result.endsWith(Constants.LINE_SEPARATOR + "```"));
    }

}
