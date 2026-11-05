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

package net.okocraft.discordchatbridge.platform.velocity;

import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.proxy.ProxyServer;
import net.okocraft.discordchatbridge.config.ConfigLoader;
import net.okocraft.discordchatbridge.config.GeneralSettings;
import net.okocraft.discordchatbridge.config.FormatSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DiscordChatBridgeVelocityTest {

    @TempDir
    Path directory;

    @Test
    void testRuntimeCleanupDoesNotRemoveProxyLifecycleHandlers() {
        var server = mock(ProxyServer.class);
        var events = mock(EventManager.class);
        when(server.getEventManager()).thenReturn(events);
        when(server.getPluginManager()).thenReturn(mock(PluginManager.class));
        var plugin = new DiscordChatBridgeVelocity(server, mock(Logger.class), directory);
        var registered = ArgumentCaptor.forClass(Object.class);

        plugin.registerListeners();
        verify(events).register(eq(plugin), registered.capture());
        plugin.unregisterListeners();
        plugin.unregisterListeners();

        verify(events).unregisterListener(plugin, registered.getValue());
        verify(events, never()).unregisterListeners(plugin);
    }

    @Test
    void testPartiallyRegisteredRuntimeListenerCanBeRetired() {
        var server = mock(ProxyServer.class);
        var events = mock(EventManager.class);
        when(server.getEventManager()).thenReturn(events);
        when(server.getPluginManager()).thenReturn(mock(PluginManager.class));
        var plugin = new DiscordChatBridgeVelocity(server, mock(Logger.class), directory);
        doThrow(new IllegalStateException("registration failed")).when(events).register(eq(plugin), any(Object.class));

        assertThrows(IllegalStateException.class, plugin::registerListeners);
        var registered = ArgumentCaptor.forClass(Object.class);
        verify(events).register(eq(plugin), registered.capture());
        plugin.unregisterListeners();

        verify(events).unregisterListener(plugin, registered.getValue());
    }
    @Test
    void testAllOwnedListenersAreRetiredEvenWhenBothUnregistrationsFail() {
        var server = mock(ProxyServer.class);
        var events = mock(EventManager.class);
        var plugins = mock(PluginManager.class);
        when(server.getEventManager()).thenReturn(events);
        when(server.getPluginManager()).thenReturn(plugins);
        when(plugins.isLoaded("lunachat")).thenReturn(true);
        var plugin = new DiscordChatBridgeVelocity(server, mock(Logger.class), directory);
        plugin.setSettings(new ConfigLoader.ConfigPair(new GeneralSettings(), new FormatSettings()));
        plugin.registerListeners();
        doAnswer(invocation -> {
            throw new IllegalStateException("unregister failed");
        }).when(events).unregisterListener(eq(plugin), any());

        var failure = assertThrows(IllegalStateException.class, plugin::unregisterListeners);

        assertEquals(1, failure.getSuppressed().length);
        verify(events, times(2)).unregisterListener(eq(plugin), any());
        doNothing().when(events).unregisterListener(eq(plugin), any());
        plugin.unregisterListeners();
        plugin.unregisterListeners();
        verify(events, times(4)).unregisterListener(eq(plugin), any());
        verify(events, never()).unregisterListeners(plugin);
    }

}
