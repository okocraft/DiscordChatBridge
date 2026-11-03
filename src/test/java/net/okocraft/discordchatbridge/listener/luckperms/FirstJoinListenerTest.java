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

package net.okocraft.discordchatbridge.listener.luckperms;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.user.UserFirstLoginEvent;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.util.FirstJoinPlayerHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FirstJoinListenerTest {

    private final DiscordChatBridgePlugin plugin = mock(DiscordChatBridgePlugin.class);
    private final LuckPerms luckPerms = mock(LuckPerms.class);
    private final EventBus bus = mock(EventBus.class);
    @SuppressWarnings("unchecked")
    private final EventSubscription<UserFirstLoginEvent> subscription = mock(EventSubscription.class);
    private final FirstJoinPlayerHolder players = new FirstJoinPlayerHolder();
    private final UserFirstLoginEvent event = mock(UserFirstLoginEvent.class);
    private final UUID uuid = new UUID(0, 1);
    private Consumer<? super UserFirstLoginEvent> handler;

    @BeforeEach
    void setUp() {
        when(plugin.getFirstJoinPlayers()).thenReturn(players);
        when(luckPerms.getEventBus()).thenReturn(bus);
        when(event.getUniqueId()).thenReturn(uuid);
        when(bus.subscribe(eq(plugin), eq(UserFirstLoginEvent.class), any())).thenAnswer(invocation -> {
            this.handler = invocation.getArgument(2);
            return subscription;
        });
    }

    @Test
    void testDuplicateFirstLoginEventsProduceOnePendingPlayer() {
        try (var provider = mockStatic(LuckPermsProvider.class)) {
            provider.when(LuckPermsProvider::get).thenReturn(luckPerms);
            new FirstJoinListener(plugin);

            handler.accept(event);
            handler.accept(event);

            assertTrue(players.remove(uuid));
            assertFalse(players.remove(uuid));
        }
    }

    @Test
    void testUnsubscribeClearsPendingStateAndIgnoresLateEvent() {
        try (var provider = mockStatic(LuckPermsProvider.class)) {
            provider.when(LuckPermsProvider::get).thenReturn(luckPerms);
            var listener = new FirstJoinListener(plugin);
            handler.accept(event);

            listener.unsubscribe();
            assertFalse(players.remove(uuid));
            handler.accept(event);

            assertFalse(players.remove(uuid));
            verify(subscription).close();
        }
    }

    @Test
    void testPreviousSubscriptionCannotPolluteNewRuntime() {
        try (var provider = mockStatic(LuckPermsProvider.class)) {
            provider.when(LuckPermsProvider::get).thenReturn(luckPerms);
            var previous = new FirstJoinListener(plugin);
            var oldHandler = handler;
            previous.unsubscribe();
            var currentPlayers = new FirstJoinPlayerHolder();
            when(plugin.getFirstJoinPlayers()).thenReturn(currentPlayers);
            new FirstJoinListener(plugin);

            oldHandler.accept(event);
            assertFalse(currentPlayers.remove(uuid));
            handler.accept(event);

            assertTrue(currentPlayers.remove(uuid));
            assertFalse(players.remove(uuid));
        }
    }

    @Test
    void testStateIsRetiredEvenIfSubscriptionCloseFails() {
        try (var provider = mockStatic(LuckPermsProvider.class)) {
            provider.when(LuckPermsProvider::get).thenReturn(luckPerms);
            var listener = new FirstJoinListener(plugin);
            handler.accept(event);
            doThrow(new IllegalStateException("close failed")).when(subscription).close();

            assertThrows(IllegalStateException.class, listener::unsubscribe);
            handler.accept(event);

            assertFalse(players.remove(uuid));
        }
    }
}
