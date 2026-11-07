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
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscordChatBridgeBukkitTest {

    @TempDir
    Path dataDirectory;

    private final DiscordChatBridgeBukkit plugin = mock(DiscordChatBridgeBukkit.class, CALLS_REAL_METHODS);
    private final Logger logger = mock(Logger.class);
    private final PluginManager manager = mock(PluginManager.class);
    private final DiscordBot bot = mock(DiscordBot.class);

    private void prepareServer(String config) throws IOException {
        Files.writeString(dataDirectory.resolve("config.yml"), config);
        var server = mock(Server.class);
        when(server.getPluginManager()).thenReturn(manager);
        doReturn(server).when(plugin).getServer();
        doReturn(logger).when(plugin).getLogger();
        doReturn(dataDirectory.toFile()).when(plugin).getDataFolder();
    }

    @Test
    void testInvalidConfigurationIsLoggedAndDisablesPlugin() throws IOException {
        prepareServer("discord:\n  token: ''\n");

        plugin.onEnable();

        verify(logger).log(eq(Level.SEVERE), startsWith("Could not load plugin configuration"), any(IOException.class));
        verify(manager).disablePlugin(plugin);
    }

    @Test
    void testLoginFailureKeepsPluginEnabledWithoutBot() throws IOException {
        var failure = new IllegalStateException("login failed");
        prepareServer("discord:\n  token: test-token\n");

        try (var login = mockStatic(DiscordBot.class)) {
            login.when(() -> DiscordBot.login(plugin)).thenThrow(failure);

            plugin.onEnable();
        }

        verify(logger).log(Level.SEVERE, "Could not enable DiscordChatBridge", failure);
        verify(manager, never()).disablePlugin(plugin);
        assertNull(plugin.getBot());
    }

    @Test
    void testRepeatedDisableShutsDownBotOnce() throws IOException {
        prepareServer("discord:\n  token: test-token\n");
        doNothing().when(plugin).registerListeners();
        try (var login = mockStatic(DiscordBot.class)) {
            login.when(() -> DiscordBot.login(plugin)).thenReturn(bot);
            plugin.onEnable();
        }

        plugin.onDisable();
        plugin.onDisable();

        verify(bot).shutdown();
        assertNull(plugin.getBot());
    }

    @Test
    void testDisableBeforeEnableDoesNotRequireInitializedLogger() {
        assertDoesNotThrow(plugin::onDisable);

        verify(plugin, never()).getLogger();
    }
}
