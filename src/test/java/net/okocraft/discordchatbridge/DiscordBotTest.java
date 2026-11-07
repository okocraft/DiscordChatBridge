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
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.okocraft.discordchatbridge.logger.LoggerWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscordBotTest {

    private final DiscordChatBridgePlugin plugin = mock(DiscordChatBridgePlugin.class);
    private final JDA jda = mock(JDA.class);
    private DiscordBot bot;

    @AfterEach
    void tearDown() {
        bot.shutdown();
    }

    @Test
    void testMessageSentWhileConnectingIsDeliveredAfterReady() throws InterruptedException {
        var ready = new CountDownLatch(1);
        var channel = mock(MessageChannel.class);
        var action = mock(MessageCreateAction.class);
        when(jda.awaitReady()).thenAnswer(invocation -> {
            ready.await();
            return jda;
        });
        when(jda.getChannelById(MessageChannel.class, 1L)).thenReturn(channel);
        when(channel.canTalk()).thenReturn(true);
        when(channel.sendMessage(any(MessageCreateData.class))).thenReturn(action);
        bot = new DiscordBot(plugin, () -> jda);

        bot.sendMessage(1L, "hello");

        verify(channel, never()).sendMessage(any(MessageCreateData.class));
        ready.countDown();
        verify(action, timeout(5_000)).queue(isNull(), any());
    }

    @Test
    void testLoginFailureIsLoggedAndLaterMessagesAreDiscarded() {
        var logger = mock(LoggerWrapper.class);
        var failure = new IllegalStateException("invalid token");
        when(plugin.getWrappedLogger()).thenReturn(logger);
        bot = new DiscordBot(plugin, () -> {
            throw failure;
        });

        verify(logger, timeout(5_000)).error("Could not log in to Discord", failure);
        assertDoesNotThrow(() -> bot.sendMessage(1L, "hello"));
    }

    @Test
    void testShutdownWhileAwaitingReadyClosesJda() throws InterruptedException {
        var connecting = new CountDownLatch(1);
        when(jda.awaitReady()).thenAnswer(invocation -> {
            connecting.countDown();
            new CountDownLatch(1).await();
            return jda;
        });
        bot = new DiscordBot(plugin, () -> jda);
        connecting.await();

        bot.shutdown();

        verify(jda).shutdown();
    }

    @Test
    void testShutdownBeforeJdaIsCreatedClosesItOnceCreated() throws InterruptedException {
        var building = new CountDownLatch(1);
        bot = new DiscordBot(plugin, () -> {
            building.countDown();
            try {
                new CountDownLatch(1).await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return jda;
        });
        building.await();

        bot.shutdown();

        verify(jda, timeout(5_000)).shutdownNow();
        verify(jda, never()).awaitReady();
    }
}
