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

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
