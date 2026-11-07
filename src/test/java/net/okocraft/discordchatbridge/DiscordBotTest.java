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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.okocraft.discordchatbridge.config.GeneralSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscordBotTest {

    private final DiscordChatBridgePlugin plugin = mock(DiscordChatBridgePlugin.class);
    private final JDA jda = mock(JDA.class);
    private final JDABuilder builder = mock(JDABuilder.class, RETURNS_SELF);

    @AfterEach
    void clearInterruptFlag() {
        Thread.interrupted();
    }

    @Test
    void testInterruptedLoginShutsDownJdaAndKeepsInterruptFlag() throws InterruptedException {
        when(plugin.getGeneralConfig()).thenReturn(new GeneralSettings());
        when(builder.build()).thenReturn(jda);
        when(jda.awaitReady()).thenThrow(new InterruptedException());

        try (var factory = mockStatic(JDABuilder.class)) {
            factory.when(() -> JDABuilder.createDefault(anyString())).thenReturn(builder);

            assertThrows(IllegalStateException.class, () -> DiscordBot.login(plugin));
        }

        verify(jda).shutdownNow();
        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void testLoginShutsDownJdaWhenConnectionFails() throws InterruptedException {
        when(plugin.getGeneralConfig()).thenReturn(new GeneralSettings());
        when(builder.build()).thenReturn(jda);
        when(jda.awaitReady()).thenThrow(new IllegalStateException("shut down while connecting"));

        try (var factory = mockStatic(JDABuilder.class)) {
            factory.when(() -> JDABuilder.createDefault(anyString())).thenReturn(builder);

            assertThrows(IllegalStateException.class, () -> DiscordBot.login(plugin));
        }

        verify(jda).shutdownNow();
    }
}
