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
import net.okocraft.discordchatbridge.config.ConfigLoader;
import net.okocraft.discordchatbridge.config.FormatSettings;
import net.okocraft.discordchatbridge.config.GeneralSettings;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscordChatBridgeBukkitTest {

    private final DiscordChatBridgeBukkit plugin = mock(DiscordChatBridgeBukkit.class, CALLS_REAL_METHODS);
    private final Logger logger = mock(Logger.class);
    private final PluginManager manager = mock(PluginManager.class);
    private final DiscordBot bot = mock(DiscordBot.class);
    private MockedStatic<DiscordBot> login;

    @BeforeEach
    void setUp() {
        var server = mock(Server.class);
        doReturn(logger).when(plugin).getLogger();
        doReturn(server).when(plugin).getServer();
        when(server.getPluginManager()).thenReturn(manager);
        doReturn(new ConfigLoader.ConfigPair(new GeneralSettings(), new FormatSettings())).when(plugin).getSettings();
        doNothing().when(plugin).registerListeners();
        doNothing().when(plugin).unregisterListeners();
        login = mockStatic(DiscordBot.class);
        login.when(() -> DiscordBot.login(plugin)).thenReturn(bot);
    }

    @AfterEach
    void tearDown() {
        login.close();
    }

    @Test
    void testEnableInitializesLoggerBeforeConfigurationAndCanDisable() {
        doAnswer(invocation -> {
            plugin.getWrappedLogger().warning("loading configuration");
            return true;
        }).when(plugin).load();

        plugin.onEnable();

        verify(logger).warning("loading configuration");
        assertSame(bot, plugin.getBot());
        assertInstanceOf(PaperChatSystem.class, plugin.getChatSystem());
        verify(manager, never()).disablePlugin(plugin);
        plugin.onDisable();
        verify(bot).shutdown();
        verify(plugin).unregisterListeners();
    }

    @Test
    void testRepeatedDisableShutsDownBotOnce() {
        doReturn(true).when(plugin).load();

        plugin.onEnable();
        plugin.onDisable();
        plugin.onDisable();

        verify(bot).shutdown();
    }

    @Test
    void testConfigurationFailureCanLogAndDisableWithoutStartup() {
        var failure = new IllegalStateException("configuration failed");
        doAnswer(invocation -> {
            plugin.getWrappedLogger().error("load failed", failure);
            return false;
        }).when(plugin).load();

        plugin.onEnable();
        assertDoesNotThrow(plugin::onDisable);

        verify(logger).log(Level.SEVERE, "load failed", failure);
        verify(manager).disablePlugin(plugin);
        verify(plugin, never()).enable();
    }

    @Test
    void testLoginFailureKeepsPluginEnabledWithoutRuntimeResources() {
        var failure = new IllegalStateException("login failed");
        doReturn(true).when(plugin).load();
        login.when(() -> DiscordBot.login(plugin)).thenThrow(failure);

        plugin.onEnable();

        verify(logger).log(Level.SEVERE, "Could not enable DiscordChatBridge", failure);
        verify(manager, never()).disablePlugin(plugin);
        verify(plugin, never()).registerListeners();
        assertNull(plugin.getBot());
        assertDoesNotThrow(plugin::onDisable);
    }

    @Test
    void testDisableBeforeEnableDoesNotRequireInitializedLogger() {
        assertDoesNotThrow(plugin::onDisable);

        verify(plugin, never()).getLogger();
        verify(plugin, never()).getWrappedLogger();
    }
}
