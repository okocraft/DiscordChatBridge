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

package net.okocraft.discordchatbridge;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.okocraft.discordchatbridge.config.GeneralSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DiscordBotTest {

    private final DiscordChatBridgePlugin plugin = mock(DiscordChatBridgePlugin.class);
    private final JDA jda = mock(JDA.class);
    private final ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
    private final DiscordBot bot = new DiscordBot(plugin, jda, scheduler);

    @BeforeEach
    void setUp() {
        when(plugin.enabled()).thenReturn(true);
        when(jda.getStatus()).thenReturn(JDA.Status.CONNECTED);
    }

    @Test
    void testShutdownCancelsBothResourcesOnlyOnce() {
        bot.shutdown();
        bot.shutdown();

        verify(scheduler).shutdownNow();
        verify(jda).shutdownNow();
        assertFalse(bot.connected());
    }

    @Test
    void testJdaIsClosedEvenIfExecutorShutdownFails() {
        when(scheduler.shutdownNow()).thenThrow(new IllegalStateException("executor failed")).thenReturn(java.util.List.of());

        assertThrows(IllegalStateException.class, bot::shutdown);
        assertDoesNotThrow(bot::shutdown);
        assertDoesNotThrow(bot::shutdown);

        verify(jda, times(2)).shutdownNow();
    }

    @Test
    void testDisconnectedAndStoppedBotDoNotQueueAnyOutgoingWork() {
        when(jda.getStatus()).thenReturn(JDA.Status.DISCONNECTED);
        var channel = mock(MessageChannel.class);
        bot.sendMessage(channel, "text");
        bot.sendChat(1, "text", "Alice", "Alice");
        bot.addReaction(mock(Message.class), "U+26A0");
        bot.updateGame();
        verifyNoInteractions(scheduler, channel);

        bot.shutdown();
        when(jda.getStatus()).thenReturn(JDA.Status.CONNECTED);
        assertDoesNotThrow(() -> bot.sendMessage(channel, "text"));
        verify(scheduler, never()).schedule(any(Runnable.class), anyLong(), any());
    }

    @Test
    void testQueuedWorkIsDroppedIfConnectionIsLostBeforeExecution() {
        var channel = mock(MessageChannel.class);
        bot.sendMessage(channel, "text");
        var action = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).schedule(action.capture(), eq(0L), eq(TimeUnit.MILLISECONDS));
        when(jda.getStatus()).thenReturn(JDA.Status.DISCONNECTED);

        action.getValue().run();

        verifyNoInteractions(channel);
    }

    @Test
    void testQueuedWorkIsDroppedAfterShutdown() {
        var channel = mock(MessageChannel.class);
        bot.sendMessage(channel, "text");
        var action = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).schedule(action.capture(), eq(0L), eq(TimeUnit.MILLISECONDS));
        bot.shutdown();

        action.getValue().run();

        verifyNoInteractions(channel);
    }

    @Test
    void testReconnectedBotCanSendNewMessage() {
        var channel = mock(MessageChannel.class);
        var delivery = mock(MessageCreateAction.class);
        when(channel.canTalk()).thenReturn(true);
        when(channel.sendMessage(any(MessageCreateData.class))).thenReturn(delivery);
        when(jda.getStatus()).thenReturn(JDA.Status.DISCONNECTED);
        bot.sendMessage(channel, "discarded");
        when(jda.getStatus()).thenReturn(JDA.Status.CONNECTED);
        bot.sendMessage(channel, "new message");
        var action = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).schedule(action.capture(), eq(0L), eq(TimeUnit.MILLISECONDS));

        action.getValue().run();

        verify(channel).sendMessage(argThat((MessageCreateData data) -> data.getContent().equals("new message")));
        verify(delivery).queue();
    }

    @Test
    void testInactiveBridgeDoesNotScheduleWork() {
        when(plugin.enabled()).thenReturn(false);

        bot.sendChat(1, "text", "Alice", "Alice");
        bot.updateGame();

        verifyNoInteractions(scheduler);
    }

    @Test
    void testInterruptedLoginClosesJdaAndPreservesInterrupt() throws Exception {
        var builder = mock(JDABuilder.class, RETURNS_SELF);
        when(plugin.getGeneralConfig()).thenReturn(new GeneralSettings());
        when(builder.build()).thenReturn(jda);
        when(jda.awaitReady()).thenThrow(new InterruptedException("interrupted"));
        try (var builders = mockStatic(JDABuilder.class)) {
            builders.when(() -> JDABuilder.createDefault(anyString())).thenReturn(builder);
            try {
                assertThrows(IllegalStateException.class, () -> DiscordBot.login(plugin));
                assertTrue(Thread.currentThread().isInterrupted());
                verify(jda).shutdownNow();
            } finally {
                Thread.interrupted();
            }
        }
    }

    @Test
    void testFailureAfterReadyClosesJdaAndExecutor() throws Exception {
        var builder = mock(JDABuilder.class, RETURNS_SELF);
        when(plugin.getGeneralConfig()).thenReturn(new GeneralSettings());
        when(builder.build()).thenReturn(jda);
        when(jda.awaitReady()).thenReturn(jda);
        when(jda.getGuilds()).thenThrow(new IllegalStateException("member load failed"));
        try (var builders = mockStatic(JDABuilder.class); var executors = mockStatic(Executors.class)) {
            builders.when(() -> JDABuilder.createDefault(anyString())).thenReturn(builder);
            executors.when(() -> Executors.newSingleThreadScheduledExecutor(any())).thenReturn(scheduler);

            assertThrows(IllegalStateException.class, () -> DiscordBot.login(plugin));

            verify(jda).shutdownNow();
            verify(scheduler).shutdownNow();
        }
    }
}
