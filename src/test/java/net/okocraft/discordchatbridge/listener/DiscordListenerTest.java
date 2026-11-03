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

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
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
        when(plugin.enabled()).thenReturn(true);
        config.channels = Map.of("global", 10L, "disabled", 0L);
        config.systemChannel = 20L;
        when(plugin.getGeneralConfig()).thenReturn(config);
        when(event.isFromGuild()).thenReturn(true);
        when(event.getAuthor()).thenReturn(author);
        when(event.getMember()).thenReturn(member);
        when(event.getChannel()).thenReturn(channel);
    }

    @Test
    void testInactiveBridgeDoesNotReadDiscordEvent() {
        when(plugin.enabled()).thenReturn(false);

        new DiscordListener(plugin).onMessageReceived(event);

        verifyNoInteractions(event);
    }

    @ParameterizedTest
    @ValueSource(longs = {10, 20})
    void testPlayerListIsAvailableToOrdinaryMemberInConfiguredChannel(long channelId) {
        preparePlayerListCommand();
        when(channel.getIdLong()).thenReturn(channelId);

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
    void testUnavailableChannelDoesNotConsumeCooldownOrReadPlayers() {
        preparePlayerListCommand();
        when(channel.canTalk()).thenReturn(false);
        var listener = new DiscordListener(plugin, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));

        listener.onMessageReceived(event);

        verifyNoInteractions(bot, platform);
        when(channel.canTalk()).thenReturn(true);
        listener.onMessageReceived(event);
        verify(bot).sendMessage(eq(channel), anyString());
    }

    @Test
    void testCooldownExpiresAtExactlyFiveSeconds() {
        preparePlayerListCommand();
        var clock = mock(Clock.class);
        when(clock.millis()).thenReturn(0L, 4999L, 5000L);
        var listener = new DiscordListener(plugin, clock);

        listener.onMessageReceived(event);
        listener.onMessageReceived(event);
        listener.onMessageReceived(event);

        verify(bot, times(2)).sendMessage(eq(channel), anyString());
        verify(platform, times(2)).getPlayerListsPerServer();
    }

    @Test
    void testCooldownIsSharedAcrossConfiguredChannels() {
        preparePlayerListCommand();
        var listener = new DiscordListener(plugin, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));

        listener.onMessageReceived(event);
        when(channel.getIdLong()).thenReturn(20L);
        listener.onMessageReceived(event);

        verify(bot).sendMessage(eq(channel), anyString());
        verify(platform).getPlayerListsPerServer();
    }

    @Test
    void testUnauthorizedRequestDoesNotConsumeCooldown() {
        preparePlayerListCommand();
        when(channel.getIdLong()).thenReturn(30L);
        var listener = new DiscordListener(plugin, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));

        listener.onMessageReceived(event);

        verifyNoInteractions(bot, platform);
        when(channel.getIdLong()).thenReturn(10L);
        listener.onMessageReceived(event);
        verify(bot).sendMessage(eq(channel), anyString());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testProcessingOrSendFailureKeepsCooldown(boolean sendFailure) {
        preparePlayerListCommand();
        if (sendFailure) {
            doThrow(new IllegalStateException("send failed")).when(bot).sendMessage(eq(channel), anyString());
        } else {
            doThrow(new IllegalStateException("update failed")).when(bot).updateGame();
        }
        var listener = new DiscordListener(plugin, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));

        assertThrows(IllegalStateException.class, () -> listener.onMessageReceived(event));
        assertDoesNotThrow(() -> listener.onMessageReceived(event));

        verify(bot).updateGame();
    }

    @Test
    void testConcurrentRequestsOnlyFetchAndSendOnePlayerList() throws Exception {
        preparePlayerListCommand();
        var listener = new DiscordListener(plugin, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        var ready = new CountDownLatch(8);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(8)) {
            var requests = new ArrayList<Future<?>>();
            for (int i = 0; i < 8; i++) {
                requests.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Requests were not started");
                    }
                    listener.onMessageReceived(event);
                    return null;
                }));
            }
            try {
                assertTrue(ready.await(10, TimeUnit.SECONDS));
            } finally {
                start.countDown();
            }
            for (var request : requests) {
                request.get(10, TimeUnit.SECONDS);
            }
        }

        verify(bot).updateGame();
        verify(bot).sendMessage(eq(channel), anyString());
        verify(platform).getNumberOfPlayers();
        verify(platform).getPlayerListsPerServer();
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

    private void preparePlayerListCommand() {
        when(channel.getIdLong()).thenReturn(10L);
        when(event.getMessage()).thenReturn(message);
        when(message.getContentDisplay()).thenReturn("!playerlist");
        when(channel.canTalk()).thenReturn(true);
        when(plugin.getBot()).thenReturn(bot);
        when(plugin.getPlatformInfo()).thenReturn(platform);
        when(plugin.getFormatConfig()).thenReturn(new FormatSettings());
        when(platform.getNumberOfPlayers()).thenReturn(2);
        when(platform.getPlayerListsPerServer()).thenReturn(List.of("survival: Alice, Bob"));
    }
}
