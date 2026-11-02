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

package net.okocraft.discordchatbridge.platform.bukkit;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.okocraft.discordchatbridge.DiscordBot;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.config.GeneralSettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

class PaperChatListenerTest {

    @Test
    void testCancelledChatDoesNotReadPlayerOrForward() {
        var plugin = mock(DiscordChatBridgePlugin.class);
        var event = mock(AsyncChatEvent.class);
        when(event.isCancelled()).thenReturn(true);

        new PaperChatListener(plugin).onChat(event);

        verify(event).isCancelled();
        verifyNoMoreInteractions(event);
        verifyNoInteractions(plugin);
    }

    @Test
    void testOrdinaryChatIsForwardedOnceWithoutBroadcastOrEventMutation() {
        var plugin = mock(DiscordChatBridgePlugin.class);
        var bot = mock(DiscordBot.class);
        var player = mock(Player.class);
        var event = mock(AsyncChatEvent.class);
        var config = new GeneralSettings();
        config.systemChannel = 42;
        when(plugin.getGeneralConfig()).thenReturn(config);
        when(plugin.getBot()).thenReturn(bot);
        when(event.getPlayer()).thenReturn(player);
        when(event.message()).thenReturn(Component.text("hello"));
        when(player.getName()).thenReturn("Alice");
        when(player.displayName()).thenReturn(Component.text("Display Alice"));

        try (var bukkit = mockStatic(Bukkit.class)) {
            new PaperChatListener(plugin).onChat(event);

            bukkit.verifyNoInteractions();
        }

        verify(bot).sendChat(42, "hello", "Alice", "Display Alice");
        verifyNoMoreInteractions(bot);
        verify(event, never()).setCancelled(anyBoolean());
        verify(event, never()).renderer(any());
        verify(event, never()).viewers();
        verify(event, never()).message(any());
    }

    @Test
    void testEditedMessageIsForwardedWithoutRenderingMinecraftPrefix() {
        var plugin = mock(DiscordChatBridgePlugin.class);
        var bot = mock(DiscordBot.class);
        var player = mock(Player.class);
        var event = mock(AsyncChatEvent.class);
        var config = new GeneralSettings();
        config.systemChannel = 42;
        when(plugin.getGeneralConfig()).thenReturn(config);
        when(plugin.getBot()).thenReturn(bot);
        when(event.getPlayer()).thenReturn(player);
        when(event.message()).thenReturn(Component.text("edited ").append(Component.text("本文😀", NamedTextColor.GREEN)));
        when(player.getName()).thenReturn("Alice");
        when(player.displayName()).thenReturn(Component.text("Display Alice", NamedTextColor.AQUA));

        new PaperChatListener(plugin).onChat(event);

        verify(bot).sendChat(42, "edited 本文😀", "Alice", "Display Alice");
        verifyNoMoreInteractions(bot);
        verify(event, never()).originalMessage();
        verify(event, never()).renderer();
        verify(event, never()).renderer(any());
        verify(event, never()).setCancelled(anyBoolean());
    }

    @Test
    void testDisabledSystemChannelDoesNotForwardOrChangeEvent() {
        var plugin = mock(DiscordChatBridgePlugin.class);
        var player = mock(Player.class);
        var event = mock(AsyncChatEvent.class);
        when(plugin.getGeneralConfig()).thenReturn(new GeneralSettings());
        when(event.getPlayer()).thenReturn(player);
        when(event.message()).thenReturn(Component.text("hello"));
        when(player.getName()).thenReturn("Alice");
        when(player.displayName()).thenReturn(Component.text("Display Alice"));

        try (var bukkit = mockStatic(Bukkit.class)) {
            new PaperChatListener(plugin).onChat(event);

            bukkit.verifyNoInteractions();
        }

        verify(plugin, never()).getBot();
        verify(event, never()).setCancelled(anyBoolean());
        verify(event, never()).renderer(any());
        verify(event, never()).viewers();
        verify(event, never()).message(any());
    }

    @Test
    void testHandlerObservesFinalChatWithoutReceivingCancelledEvents() throws Exception {
        var handler = PaperChatListener.class.getMethod("onChat", AsyncChatEvent.class)
                .getAnnotation(EventHandler.class);

        assertEquals(EventPriority.MONITOR, handler.priority());
        assertTrue(handler.ignoreCancelled());
    }
}
