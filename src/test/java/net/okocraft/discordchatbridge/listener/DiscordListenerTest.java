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

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.okocraft.discordchatbridge.DiscordBot;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.chat.ChatSystem;
import net.okocraft.discordchatbridge.config.FormatSettings;
import net.okocraft.discordchatbridge.config.GeneralSettings;
import net.okocraft.discordchatbridge.platform.PlatformInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DiscordListenerTest {

    private final DiscordChatBridgePlugin plugin = mock(DiscordChatBridgePlugin.class);
    private final DiscordBot bot = mock(DiscordBot.class);
    private final PlatformInfo platform = mock(PlatformInfo.class);
    private final ChatSystem chat = mock(ChatSystem.class);
    private final GeneralSettings config = new GeneralSettings();
    private final MessageReceivedEvent event = mock(MessageReceivedEvent.class);
    private final MessageChannelUnion channel = mock(MessageChannelUnion.class);
    private final Message message = mock(Message.class);
    private final User author = mock(User.class);
    private final Member member = mock(Member.class);

    @BeforeEach
    void setUp() {
        config.channels = Map.of("global", 10L, "disabled", 0L);
        config.systemChannel = 20L;
        when(plugin.getGeneralConfig()).thenReturn(config);
        when(event.isFromGuild()).thenReturn(true);
        when(event.getAuthor()).thenReturn(author);
        when(event.getMember()).thenReturn(member);
        when(event.getChannel()).thenReturn(channel);
    }

    @ParameterizedTest
    @ValueSource(longs = {10, 20})
    void testPlayerListIsAvailableToOrdinaryMemberInConfiguredChannel(long channelId) {
        when(channel.getIdLong()).thenReturn(channelId);
        when(event.getMessage()).thenReturn(message);
        when(message.getContentDisplay()).thenReturn("!playerlist");
        when(channel.canTalk()).thenReturn(true);
        when(plugin.getBot()).thenReturn(bot);
        when(plugin.getPlatformInfo()).thenReturn(platform);
        when(plugin.getFormatConfig()).thenReturn(new FormatSettings());
        when(platform.getNumberOfPlayers()).thenReturn(2);
        when(platform.getPlayerListsPerServer()).thenReturn(List.of("survival: Alice, Bob"));

        new DiscordListener(plugin).onMessageReceived(event);

        verify(bot).sendMessage(eq(channel), contains("survival: Alice, Bob"));
        verifyNoInteractions(chat);
    }

    @Test
    void testUnlinkedChannelIsIgnoredBeforeReadingMessageOrPlayerInformation() {
        when(channel.getIdLong()).thenReturn(30L);

        new DiscordListener(plugin).onMessageReceived(event);

        verify(event, never()).getMessage();
        verify(plugin, never()).getPlatformInfo();
        verify(plugin, never()).getBot();
        verify(plugin, never()).getChatSystem();
    }

    @Test
    void testNoConfiguredChannelsDoesNotAllowPlayerList() {
        config.channels = Map.of();
        config.systemChannel = 0;
        when(channel.getIdLong()).thenReturn(20L);

        new DiscordListener(plugin).onMessageReceived(event);

        verify(event, never()).getMessage();
        verify(plugin, never()).getPlatformInfo();
        verify(plugin, never()).getBot();
        verify(plugin, never()).getChatSystem();
    }

    @Test
    void testBotMessageIsIgnoredEvenInConfiguredChannel() {
        when(author.isBot()).thenReturn(true);

        new DiscordListener(plugin).onMessageReceived(event);

        verify(event, never()).getChannel();
        verify(plugin, never()).getBot();
        verify(plugin, never()).getPlatformInfo();
    }

    @Test
    void testDirectMessageIsIgnored() {
        when(event.isFromGuild()).thenReturn(false);
        when(event.getMember()).thenReturn(null);

        new DiscordListener(plugin).onMessageReceived(event);

        verify(event, never()).getChannel();
        verify(plugin, never()).getBot();
        verify(plugin, never()).getPlatformInfo();
    }

    @Test
    void testGuildMessageWithoutMemberIsIgnored() {
        when(event.getMember()).thenReturn(null);

        new DiscordListener(plugin).onMessageReceived(event);

        verify(event, never()).getChannel();
        verify(plugin, never()).getBot();
        verify(plugin, never()).getPlatformInfo();
    }

    @Test
    void testPlayerListIsNotSentWhenBotCannotTalk() {
        when(channel.getIdLong()).thenReturn(10L);
        when(event.getMessage()).thenReturn(message);
        when(message.getContentDisplay()).thenReturn("!playerlist");
        when(channel.canTalk()).thenReturn(false);
        when(plugin.getBot()).thenReturn(bot);
        when(plugin.getPlatformInfo()).thenReturn(platform);
        when(plugin.getFormatConfig()).thenReturn(new FormatSettings());
        when(platform.getPlayerListsPerServer()).thenReturn(List.of("survival: Alice"));

        new DiscordListener(plugin).onMessageReceived(event);

        verify(bot, never()).sendMessage(any(MessageChannelUnion.class), anyString());
    }

    @Test
    void testSystemChannelDoesNotBecomeChatBridge() {
        when(channel.getIdLong()).thenReturn(20L);
        when(event.getMessage()).thenReturn(message);
        when(message.getContentDisplay()).thenReturn("hello");
        when(plugin.getChatSystem()).thenReturn(chat);

        new DiscordListener(plugin).onMessageReceived(event);

        verifyNoInteractions(chat);
        verify(plugin, never()).getBot();
    }

    @Test
    void testLinkedChatContinuesToBeForwarded() {
        when(channel.getIdLong()).thenReturn(10L);
        when(event.getMessage()).thenReturn(message);
        when(message.getContentDisplay()).thenReturn("hello");
        when(message.getAttachments()).thenReturn(List.of());
        when(member.getEffectiveName()).thenReturn("Alice");
        config.rolePrefix = Map.of("enable", "false");
        when(plugin.getChatSystem()).thenReturn(chat);

        new DiscordListener(plugin).onMessageReceived(event);

        verify(chat).sendChat("global", "Alice", "Dis", "hello");
        verify(plugin, never()).getBot();
    }
}
