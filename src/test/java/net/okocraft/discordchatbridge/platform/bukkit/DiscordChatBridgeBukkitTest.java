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

import net.okocraft.discordchatbridge.DiscordBot;
import net.okocraft.discordchatbridge.BridgeLifecycle;
import net.okocraft.discordchatbridge.config.ConfigLoader;
import net.okocraft.discordchatbridge.config.GeneralSettings;
import net.okocraft.discordchatbridge.config.FormatSettings;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscordChatBridgeBukkitTest {

    @Test
    void testEnableInitializesLoggerBeforeConfigurationAndCanDisable() {
        var plugin = mock(DiscordChatBridgeBukkit.class, CALLS_REAL_METHODS);
        doReturn(new BridgeLifecycle(plugin)).when(plugin).getLifecycle();
        var logger = mock(Logger.class);
        var server = mock(Server.class);
        var manager = mock(PluginManager.class);
        doReturn(logger).when(plugin).getLogger();
        doReturn(server).when(plugin).getServer();
        when(server.getPluginManager()).thenReturn(manager);
        doAnswer(invocation -> {
            plugin.getWrappedLogger().warning("loading configuration");
            plugin.setSettings(new ConfigLoader.ConfigPair(new GeneralSettings(), new FormatSettings()));
            return true;
        }).when(plugin).load();
        var bot = mock(DiscordBot.class);
        when(bot.connected()).thenReturn(true);
        doNothing().when(plugin).registerListeners();
        doNothing().when(plugin).unregisterListeners();

        try (var login = mockStatic(DiscordBot.class)) {
            login.when(() -> DiscordBot.login(plugin)).thenReturn(bot);
            plugin.onEnable();

            verify(logger).warning("loading configuration");
            assertTrue(plugin.enabled());
            assertInstanceOf(PaperChatSystem.class, plugin.getChatSystem());
            verify(manager, never()).disablePlugin(plugin);
            plugin.onDisable();
            verify(bot).shutdown();
            verify(plugin).unregisterListeners();
        }
    }

    @Test
    void testConfigurationFailureCanLogAndDisableWithoutStartup() {
        var plugin = mock(DiscordChatBridgeBukkit.class, CALLS_REAL_METHODS);
        doReturn(new BridgeLifecycle(plugin)).when(plugin).getLifecycle();
        var logger = mock(Logger.class);
        var server = mock(Server.class);
        var manager = mock(PluginManager.class);
        var failure = new IllegalStateException("configuration failed");
        doReturn(logger).when(plugin).getLogger();
        doReturn(server).when(plugin).getServer();
        when(server.getPluginManager()).thenReturn(manager);
        doAnswer(invocation -> {
            plugin.getWrappedLogger().error("load failed", failure);
            return false;
        }).when(plugin).load();

        plugin.onEnable();
        assertDoesNotThrow(plugin::onDisable);

        verify(logger).log(Level.SEVERE, "load failed", failure);
        verify(manager).disablePlugin(plugin);
        verify(plugin, never()).enable();
        verify(plugin, never()).getBot();
        assertFalse(plugin.enabled());
    }

    @Test
    void testStartupFailureKeepsLoggerAvailableAndDisableIsSafe() {
        var plugin = mock(DiscordChatBridgeBukkit.class, CALLS_REAL_METHODS);
        doReturn(new BridgeLifecycle(plugin)).when(plugin).getLifecycle();
        var logger = mock(Logger.class);
        var server = mock(Server.class);
        var manager = mock(PluginManager.class);
        doReturn(logger).when(plugin).getLogger();
        doReturn(server).when(plugin).getServer();
        when(server.getPluginManager()).thenReturn(manager);
        doReturn(true).when(plugin).load();
        doReturn(false).when(plugin).enable();

        plugin.onEnable();
        plugin.getWrappedLogger().warning("startup failed");
        assertDoesNotThrow(plugin::onDisable);

        verify(logger).warning("startup failed");
        assertFalse(plugin.enabled());
        verify(plugin, never()).getBot();
    }

    @Test
    void testDisableBeforeEnableDoesNotRequireInitializedLogger() {
        var plugin = mock(DiscordChatBridgeBukkit.class, CALLS_REAL_METHODS);
        doReturn(new BridgeLifecycle(plugin)).when(plugin).getLifecycle();

        assertDoesNotThrow(plugin::onDisable);

        verify(plugin, never()).getLogger();
        verify(plugin, never()).getWrappedLogger();
        verify(plugin, never()).getBot();
    }
}
