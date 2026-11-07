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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscordChatBridgePluginTest {

    private final DiscordChatBridgePlugin plugin = mock(DiscordChatBridgePlugin.class, CALLS_REAL_METHODS);
    private final LoggerWrapper logger = mock(LoggerWrapper.class);
    private final DiscordBot bot = mock(DiscordBot.class);
    private final GeneralSettings general = new GeneralSettings();

    @BeforeEach
    void setUp() {
        doReturn(logger).when(plugin).getWrappedLogger();
        doReturn(general).when(plugin).getGeneralConfig();
        doReturn(bot).when(plugin).getBot();
    }

    @Test
    void testEnableStartsAllRuntimeResources() {
        general.messageSendingSetting.firstJoin = true;

        assertTrue(plugin.enable());

        var order = inOrder(plugin, bot);
        order.verify(plugin).loginToDiscord();
        order.verify(plugin).registerListeners();
        order.verify(plugin).registerLuckPermsFirstJoinListener();
        order.verify(bot).updateGame();
        verify(plugin, never()).shutdownBot();
    }

    @Test
    void testLoginFailureLeavesNothingRegistered() {
        var failure = new IllegalStateException("login failed");
        doThrow(failure).when(plugin).loginToDiscord();

        assertFalse(plugin.enable());

        verify(logger).error("Could not enable DiscordChatBridge", failure);
        verify(plugin, never()).registerListeners();
        verify(plugin).shutdownBot();
    }

    @Test
    void testFailureAfterLoginReleasesAcquiredResources() {
        general.messageSendingSetting.firstJoin = true;
        var failure = new IllegalStateException("LuckPerms unavailable");
        doThrow(failure).when(plugin).registerLuckPermsFirstJoinListener();

        assertFalse(plugin.enable());

        verify(logger).error("Could not enable DiscordChatBridge", failure);
        verify(plugin).unregisterListeners();
        verify(plugin).unregisterLuckPermsFirstJoinListener();
        verify(plugin).shutdownBot();
        verify(bot, never()).updateGame();
    }
}
