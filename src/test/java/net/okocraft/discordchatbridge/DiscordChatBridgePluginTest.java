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

import net.okocraft.discordchatbridge.config.GeneralSettings;
import net.okocraft.discordchatbridge.logger.LoggerWrapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscordChatBridgePluginTest {

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
}
