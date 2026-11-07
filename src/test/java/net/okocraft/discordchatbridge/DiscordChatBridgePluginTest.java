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

import net.okocraft.discordchatbridge.config.ConfigLoader;
import net.okocraft.discordchatbridge.config.FormatSettings;
import net.okocraft.discordchatbridge.config.GeneralSettings;
import net.okocraft.discordchatbridge.logger.LoggerWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscordChatBridgePluginTest {

    @TempDir
    Path dataDirectory;

    private final DiscordChatBridgePlugin plugin = mock(DiscordChatBridgePlugin.class, CALLS_REAL_METHODS);
    private final LoggerWrapper logger = mock(LoggerWrapper.class);
    private final GeneralSettings general = new GeneralSettings();

    @Test
    void testEnableStartsRuntimeResources() {
        var bot = mock(DiscordBot.class);
        general.messageSendingSetting.firstJoin = true;
        doReturn(general).when(plugin).getGeneralConfig();
        doReturn(bot).when(plugin).getBot();

        assertTrue(plugin.enable());

        verify(plugin).loginToDiscord();
        verify(plugin).registerListeners();
        verify(plugin).registerLuckPermsFirstJoinListener();
        verify(bot).updateGame();
        verify(plugin, never()).shutdownBot();
    }

    @Test
    void testEnableWithLoginFailureRegistersNothing() {
        var failure = new IllegalStateException("login failed");
        doReturn(logger).when(plugin).getWrappedLogger();
        doThrow(failure).when(plugin).loginToDiscord();

        assertFalse(plugin.enable());

        verify(logger).error("Could not enable DiscordChatBridge", failure);
        verify(plugin, never()).registerListeners();
        verify(plugin, never()).registerLuckPermsFirstJoinListener();
    }

    @Test
    void testEnableWithFailureAfterLoginReleasesAcquiredResources() {
        var failure = new IllegalStateException("LuckPerms unavailable");
        general.messageSendingSetting.firstJoin = true;
        doReturn(logger).when(plugin).getWrappedLogger();
        doReturn(general).when(plugin).getGeneralConfig();
        doThrow(failure).when(plugin).registerLuckPermsFirstJoinListener();

        assertFalse(plugin.enable());

        verify(logger).error("Could not enable DiscordChatBridge", failure);
        verify(plugin).unregisterListeners();
        verify(plugin).unregisterLuckPermsFirstJoinListener();
        verify(plugin).shutdownBot();
    }

    @Test
    void testReloadWithInvalidConfigurationKeepsCurrentState() throws IOException {
        Files.writeString(dataDirectory.resolve("config.yml"), "discord:\n  token: ''\n");
        doReturn(dataDirectory).when(plugin).getDataDirectory();
        doReturn(logger).when(plugin).getWrappedLogger();

        assertFalse(plugin.reload());

        verify(plugin, never()).setSettings(any());
        verify(plugin, never()).shutdownBot();
    }

    @Test
    void testReloadWithSameTokenKeepsConnection() throws IOException {
        var bot = mock(DiscordBot.class);
        Files.writeString(dataDirectory.resolve("config.yml"), "discord:\n  token: test-token\n");
        general.discord.token = "test-token";
        doReturn(dataDirectory).when(plugin).getDataDirectory();
        doReturn(new ConfigLoader.ConfigPair(general, new FormatSettings())).when(plugin).getSettings();
        doReturn(bot).when(plugin).getBot();

        assertTrue(plugin.reload());

        verify(plugin).setSettings(any());
        verify(plugin, never()).shutdownBot();
        verify(plugin, never()).loginToDiscord();
        verify(bot).updateGame();
    }

    @Test
    void testReloadWithChangedTokenReconnects() throws IOException {
        Files.writeString(dataDirectory.resolve("config.yml"), "discord:\n  token: new-token\n");
        general.discord.token = "old-token";
        doReturn(dataDirectory).when(plugin).getDataDirectory();
        doReturn(new ConfigLoader.ConfigPair(general, new FormatSettings())).when(plugin).getSettings();
        doReturn(mock(DiscordBot.class)).when(plugin).getBot();

        assertTrue(plugin.reload());

        verify(plugin).shutdownBot();
        verify(plugin).loginToDiscord();
    }

    @Test
    void testReloadReconnectsStoppedBot() throws IOException {
        var bot = mock(DiscordBot.class);
        when(bot.isStopped()).thenReturn(true);
        Files.writeString(dataDirectory.resolve("config.yml"), "discord:\n  token: test-token\n");
        general.discord.token = "test-token";
        doReturn(dataDirectory).when(plugin).getDataDirectory();
        doReturn(new ConfigLoader.ConfigPair(general, new FormatSettings())).when(plugin).getSettings();
        doReturn(bot).when(plugin).getBot();

        assertTrue(plugin.reload());

        verify(plugin).shutdownBot();
        verify(plugin).loginToDiscord();
    }

    @Test
    void testReloadDisablingFirstJoinUnsubscribesFromLuckPerms() throws IOException {
        Files.writeString(dataDirectory.resolve("config.yml"),
                "discord:\n  token: test-token\nmessage-sending-setting:\n  first-join: false\n");
        general.discord.token = "test-token";
        doReturn(dataDirectory).when(plugin).getDataDirectory();
        doReturn(new ConfigLoader.ConfigPair(general, new FormatSettings())).when(plugin).getSettings();
        doReturn(mock(DiscordBot.class)).when(plugin).getBot();

        assertTrue(plugin.reload());

        verify(plugin).unregisterLuckPermsFirstJoinListener();
        verify(plugin, never()).registerLuckPermsFirstJoinListener();
    }
}
