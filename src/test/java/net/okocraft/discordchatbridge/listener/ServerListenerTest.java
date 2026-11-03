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

package net.okocraft.discordchatbridge.listener;

import net.okocraft.discordchatbridge.DiscordBot;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.config.FormatSettings;
import net.okocraft.discordchatbridge.config.GeneralSettings;
import net.okocraft.discordchatbridge.util.FirstJoinPlayerHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.*;

class ServerListenerTest {

    private final DiscordChatBridgePlugin plugin = mock(DiscordChatBridgePlugin.class);
    private final DiscordBot bot = mock(DiscordBot.class);
    private final FirstJoinPlayerHolder players = new FirstJoinPlayerHolder();
    private final UUID uuid = new UUID(0, 1);
    private final ServerListener listener = new ServerListener(plugin) {};

    @BeforeEach
    void setUp() {
        var general = new GeneralSettings();
        general.systemChannel = 42;
        var format = new FormatSettings();
        format.server.firstJoin = "first %player_name%";
        format.server.join = "join %player_name%";
        when(plugin.getGeneralConfig()).thenReturn(general);
        when(plugin.getFormatConfig()).thenReturn(format);
        when(plugin.getBot()).thenReturn(bot);
        when(plugin.getFirstJoinPlayers()).thenReturn(players);
    }

    @Test
    void testDuplicateMarkersDoNotRepeatFirstJoinNotification() {
        players.add(uuid);
        players.add(uuid);

        listener.processJoin(uuid, "Alice", "Alice");
        listener.processJoin(uuid, "Alice", "Alice");

        verify(bot).sendMessage(42, "first Alice");
        verify(bot).sendMessage(42, "join Alice");
    }

    @Test
    void testJoinWithoutFirstLoginMarkerUsesNormalNotification() {
        listener.processJoin(uuid, "Alice", "Alice");

        verify(bot).sendMessage(42, "join Alice");
        verify(bot, never()).sendMessage(42, "first Alice");
    }

    @Test
    void testReplacedStateDoesNotUsePreviousPendingMarker() {
        players.add(uuid);
        players.close();
        when(plugin.getFirstJoinPlayers()).thenReturn(new FirstJoinPlayerHolder());

        listener.processJoin(uuid, "Alice", "Alice");

        verify(bot).sendMessage(42, "join Alice");
        verify(bot, never()).sendMessage(42, "first Alice");
    }
}
