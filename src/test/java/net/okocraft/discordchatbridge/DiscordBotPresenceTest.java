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
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.managers.Presence;
import net.okocraft.discordchatbridge.config.GeneralSettings;
import net.okocraft.discordchatbridge.platform.PlatformInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import static org.mockito.Mockito.*;

class DiscordBotPresenceTest {

    private final DiscordChatBridgePlugin plugin = mock(DiscordChatBridgePlugin.class);
    private final PlatformInfo platform = mock(PlatformInfo.class);
    private final JDA jda = mock(JDA.class);
    private final Presence presence = mock(Presence.class);
    private DiscordBot bot;

    @BeforeEach
    void setUp() {
        when(plugin.getGeneralConfig()).thenReturn(new GeneralSettings());
        when(plugin.getPlatformInfo()).thenReturn(platform);
        when(jda.getPresence()).thenReturn(presence);
    }

    @AfterEach
    void tearDown() {
        bot.shutdown();
    }

    @Test
    void testUpdatesWhileOneIsPendingAreSentOnceWithLatestCount() throws InterruptedException {
        var ready = new CountDownLatch(1);
        when(jda.awaitReady()).thenAnswer(invocation -> {
            ready.await();
            return jda;
        });
        when(platform.getNumberOfPlayers()).thenReturn(3);
        bot = new DiscordBot(plugin, () -> jda, Duration.ZERO);

        bot.updateGame();
        bot.updateGame();
        bot.updateGame();
        ready.countDown();

        verify(presence, timeout(5_000)).setPresence(OnlineStatus.ONLINE, Activity.playing("3 players in server"));
        verify(presence, times(1)).setPresence(any(OnlineStatus.class), any(Activity.class));
    }

    @Test
    void testUpdateAfterPreviousOneIsSentIsScheduledAgain() {
        when(platform.getNumberOfPlayers()).thenReturn(3, 4);
        bot = new DiscordBot(plugin, () -> jda, Duration.ZERO);

        bot.updateGame();
        verify(presence, timeout(5_000)).setPresence(OnlineStatus.ONLINE, Activity.playing("3 players in server"));
        bot.updateGame();

        verify(presence, timeout(5_000)).setPresence(OnlineStatus.ONLINE, Activity.playing("4 players in server"));
    }
}
