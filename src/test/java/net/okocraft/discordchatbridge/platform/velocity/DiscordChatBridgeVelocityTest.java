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

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.proxy.ProxyServer;
import net.okocraft.discordchatbridge.DiscordBot;
import net.okocraft.discordchatbridge.config.ConfigLoader;
import net.okocraft.discordchatbridge.config.FormatSettings;
import net.okocraft.discordchatbridge.config.GeneralSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.Logger;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscordChatBridgeVelocityTest {

    private final ProxyServer server = mock(ProxyServer.class);
    private final CommandManager commandManager = mock(CommandManager.class);
    private final EventManager eventManager = mock(EventManager.class);
    private final DiscordBot bot = mock(DiscordBot.class);
    private DiscordChatBridgeVelocity plugin;
    private MockedStatic<DiscordBot> login;

    @BeforeEach
    void setUp() {
        when(server.getCommandManager()).thenReturn(commandManager);
        when(server.getEventManager()).thenReturn(eventManager);
        when(server.getPluginManager()).thenReturn(mock(PluginManager.class));
        var metaBuilder = mock(CommandMeta.Builder.class, RETURNS_SELF);
        when(metaBuilder.build()).thenReturn(mock(CommandMeta.class));
        when(commandManager.metaBuilder("dcbreload")).thenReturn(metaBuilder);

        plugin = spy(new DiscordChatBridgeVelocity(server, mock(Logger.class), Path.of("unused")));
        doReturn(true).when(plugin).load();
        doReturn(new ConfigLoader.ConfigPair(new GeneralSettings(), new FormatSettings())).when(plugin).getSettings();
        login = mockStatic(DiscordBot.class);
        login.when(() -> DiscordBot.login(plugin)).thenReturn(bot);
    }

    @AfterEach
    void tearDown() {
        login.close();
    }

    @Test
    void testRestartKeepsReloadCommandAndOwnShutdownSubscription() {
        plugin.onEnable(null);
        plugin.disable();
        plugin.enable();

        verify(commandManager, times(1)).register(any(CommandMeta.class), any(VelocityReloadCommand.class));
        verify(commandManager, never()).unregister(anyString());
        verify(eventManager, never()).unregisterListeners(any());
        verify(eventManager).unregisterListener(eq(plugin), any(VelocityServerListener.class));
        verify(eventManager, times(2)).register(eq(plugin), any(VelocityServerListener.class));
    }

    @Test
    void testLoginFailureKeepsReloadCommandWithoutListeners() {
        login.when(() -> DiscordBot.login(plugin)).thenThrow(new IllegalStateException("login failed"));

        plugin.onEnable(null);

        verify(commandManager).register(any(CommandMeta.class), any(VelocityReloadCommand.class));
        verify(eventManager, never()).register(any(), any());
        assertNull(plugin.getBot());
    }

    @Test
    void testRepeatedShutdownShutsDownBotOnce() {
        plugin.onEnable(null);
        plugin.onDisable(null);
        plugin.onDisable(null);

        verify(bot).shutdown();
        verify(eventManager).unregisterListener(eq(plugin), any(VelocityServerListener.class));
    }
}
