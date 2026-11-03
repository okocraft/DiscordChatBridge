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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BridgeLifecycleTest {

    private final DiscordChatBridgePlugin plugin = mock(DiscordChatBridgePlugin.class, CALLS_REAL_METHODS);
    private final BridgeLifecycle lifecycle = new BridgeLifecycle(plugin);
    private final DiscordBot bot = mock(DiscordBot.class);

    @BeforeEach
    void setUp() {
        doReturn(lifecycle).when(plugin).getLifecycle();
        doReturn(new GeneralSettings()).when(plugin).getGeneralConfig();
        doReturn(mock(LoggerWrapper.class)).when(plugin).getWrappedLogger();
        when(bot.connected()).thenReturn(true);
    }

    @Test
    void testStartAndStopAreIdempotent() {
        try (var login = mockStatic(DiscordBot.class)) {
            login.when(() -> DiscordBot.login(plugin)).thenReturn(bot);

            assertTrue(plugin.enable());
            assertTrue(plugin.enable());
            assertTrue(plugin.enabled());
            assertSame(bot, plugin.getBot());
            plugin.disable();
            plugin.disable();

            assertEquals(BridgeLifecycle.State.STOPPED, lifecycle.state());
            assertFalse(plugin.enabled());
            verify(plugin).registerCommands();
            verify(plugin).registerListeners();
            verify(plugin).registerLuckPermsFirstJoinListener();
            verify(plugin).unregisterCommands();
            verify(plugin).unregisterListeners();
            verify(plugin).unregisterLuckPermsFirstJoinListener();
            verify(bot).shutdown();
            login.verify(() -> DiscordBot.login(plugin));
        }
    }

    @Test
    void testInitialLoginFailureRetainsManagementCommand() {
        try (var login = mockStatic(DiscordBot.class)) {
            login.when(() -> DiscordBot.login(plugin)).thenThrow(new IllegalStateException("login failed"));

            assertFalse(plugin.enable());

            assertEquals(BridgeLifecycle.State.FAILED, lifecycle.state());
            assertFalse(plugin.enabled());
            verify(plugin).registerCommands();
            verify(plugin, never()).unregisterCommands();
            verify(plugin, never()).registerListeners();
            plugin.disable();
            verify(plugin).unregisterCommands();
        }
    }

    @Test
    void testManualRetryAfterFailedLoginUsesExistingManagementCommand() {
        try (var login = mockStatic(DiscordBot.class)) {
            login.when(() -> DiscordBot.login(plugin))
                    .thenThrow(new IllegalStateException("login failed"))
                    .thenReturn(bot);

            assertFalse(plugin.enable());
            assertTrue(plugin.enable());

            assertTrue(plugin.enabled());
            verify(plugin).registerCommands();
            verify(plugin).registerListeners();
            login.verify(() -> DiscordBot.login(plugin), times(2));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"listeners", "luckperms", "presence", "terminated"})
    void testPartialStartupFailureRetiresBotAndRegistrations(String stage) {
        try (var login = mockStatic(DiscordBot.class)) {
            login.when(() -> DiscordBot.login(plugin)).thenReturn(bot);
            switch (stage) {
                case "listeners" -> doThrow(new IllegalStateException(stage)).when(plugin).registerListeners();
                case "luckperms" -> doThrow(new IllegalStateException(stage)).when(plugin).registerLuckPermsFirstJoinListener();
                case "presence" -> doThrow(new IllegalStateException(stage)).when(bot).updateGame();
                case "terminated" -> when(bot.terminated()).thenReturn(true);
            }

            assertFalse(plugin.enable());

            assertEquals(BridgeLifecycle.State.FAILED, lifecycle.state());
            verify(bot).shutdown();
            verify(plugin).unregisterListeners();
            verify(plugin).unregisterLuckPermsFirstJoinListener();
            verify(plugin, never()).unregisterCommands();
        }
    }

    @Test
    void testFailedCommandRegistrationIsUndoneBeforeLogin() {
        try (var login = mockStatic(DiscordBot.class)) {
            doThrow(new IllegalStateException("command failed")).when(plugin).registerCommands();

            assertFalse(plugin.enable());

            verify(plugin).unregisterCommands();
            login.verifyNoInteractions();
            assertEquals(BridgeLifecycle.State.FAILED, lifecycle.state());
        }
    }

    @Test
    void testDisconnectAndReconnectDoNotReregisterRuntime() {
        try (var login = mockStatic(DiscordBot.class)) {
            login.when(() -> DiscordBot.login(plugin)).thenReturn(bot);
            assertTrue(plugin.enable());

            when(bot.connected()).thenReturn(false);
            assertEquals(BridgeLifecycle.State.RECONNECTING, lifecycle.state());
            assertFalse(plugin.enabled());
            assertTrue(plugin.enable());
            when(bot.connected()).thenReturn(true);

            assertEquals(BridgeLifecycle.State.RUNNING, lifecycle.state());
            assertTrue(plugin.enabled());
            verify(plugin).registerListeners();
            verify(plugin, never()).unregisterListeners();
            login.verify(() -> DiscordBot.login(plugin));
        }
    }

    @Test
    void testRestartReplacesBotButKeepsManagementCommand() {
        var next = mock(DiscordBot.class);
        when(next.connected()).thenReturn(true);
        var settings = new ConfigLoader.ConfigPair(new GeneralSettings(), new FormatSettings());
        try (var login = mockStatic(DiscordBot.class)) {
            login.when(() -> DiscordBot.login(plugin)).thenReturn(bot, next);
            assertTrue(plugin.enable());

            assertTrue(lifecycle.restart(settings));
            lifecycle.botStopped(bot);

            assertSame(next, plugin.getBot());
            assertEquals(BridgeLifecycle.State.RUNNING, lifecycle.state());
            verify(plugin).setSettings(settings);
            verify(bot).shutdown();
            verify(next, never()).shutdown();
            verify(plugin).registerCommands();
            verify(plugin, never()).unregisterCommands();
            verify(plugin, times(2)).registerListeners();
        }
    }

    @Test
    void testCleanupFailureStillRetiresOtherResourcesAndCanBeRetried() {
        try (var login = mockStatic(DiscordBot.class)) {
            login.when(() -> DiscordBot.login(plugin)).thenReturn(bot);
            assertTrue(plugin.enable());
            doThrow(new IllegalStateException("listener cleanup failed")).when(plugin).unregisterListeners();

            plugin.disable();

            assertEquals(BridgeLifecycle.State.FAILED, lifecycle.state());
            verify(bot).shutdown();
            verify(plugin).unregisterLuckPermsFirstJoinListener();
            verify(plugin).unregisterCommands();
            doNothing().when(plugin).unregisterListeners();
            plugin.disable();
            assertEquals(BridgeLifecycle.State.STOPPED, lifecycle.state());
        }
    }

    @Test
    void testTerminalBotShutdownRetiresRuntimeButKeepsCommand() {
        try (var login = mockStatic(DiscordBot.class)) {
            login.when(() -> DiscordBot.login(plugin)).thenReturn(bot);
            assertTrue(plugin.enable());

            lifecycle.botStopped(bot);

            assertEquals(BridgeLifecycle.State.FAILED, lifecycle.state());
            verify(bot).shutdown();
            verify(plugin).unregisterListeners();
            verify(plugin, never()).unregisterCommands();
        }
    }
}
