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

package net.okocraft.discordchatbridge.listener.chat;

import com.github.ucchyocean.lc3.member.ChannelMember;
import net.okocraft.discordchatbridge.DiscordBot;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.config.GeneralSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.*;

class LunaChatListenerTest {

    private final DiscordChatBridgePlugin plugin = mock(DiscordChatBridgePlugin.class);
    private final DiscordBot bot = mock(DiscordBot.class);
    private final ChannelMember member = mock(ChannelMember.class);
    private final GeneralSettings config = new GeneralSettings();
    private final LunaChatListener listener = new LunaChatListener(plugin) {};

    @BeforeEach
    void setUp() {
        when(plugin.getGeneralConfig()).thenReturn(config);
    }

    @Test
    void testChannelLinkedAfterCreationIsForwarded() {
        config.channels = Map.of("trade", 30L);
        when(plugin.getBot()).thenReturn(bot);
        when(member.getName()).thenReturn("Alice");
        when(member.getDisplayName()).thenReturn("Alice");

        listener.processChat("trade", "Alice", member, "hello");

        verify(bot).sendChat(30L, "hello", "Alice", "Alice");
    }

    @Test
    void testChannelLinkedToZeroIsIgnored() {
        config.channels = Map.of("trade", 0L);

        listener.processChat("trade", "Alice", member, "hello");

        verifyNoInteractions(bot);
    }
}
