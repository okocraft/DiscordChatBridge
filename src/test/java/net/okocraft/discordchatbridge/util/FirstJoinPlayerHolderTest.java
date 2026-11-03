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

package net.okocraft.discordchatbridge.util;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class FirstJoinPlayerHolderTest {

    private static final UUID PLAYER = new UUID(0, 1);

    @Test
    void testDuplicateRegistrationIsConsumedOnlyOnce() {
        var players = new FirstJoinPlayerHolder();
        players.add(PLAYER);
        players.add(PLAYER);

        assertTrue(players.remove(PLAYER));
        assertFalse(players.remove(PLAYER));
    }

    @Test
    void testUnknownPlayerIsNotFirstJoin() {
        assertFalse(new FirstJoinPlayerHolder().remove(PLAYER));
    }

    @Test
    void testCloseDiscardsPendingPlayersAndRejectsLateRegistration() {
        var players = new FirstJoinPlayerHolder();
        players.add(PLAYER);

        players.close();
        players.close();
        assertFalse(players.remove(PLAYER));
        players.add(PLAYER);
        assertFalse(players.remove(PLAYER));
    }

    @Test
    void testRuntimeInstancesDoNotSharePlayers() {
        var previous = new FirstJoinPlayerHolder();
        var current = new FirstJoinPlayerHolder();
        previous.add(PLAYER);
        current.add(PLAYER);

        previous.close();

        assertTrue(current.remove(PLAYER));
        assertFalse(previous.remove(PLAYER));
    }

    @Test
    void testConcurrentRegistrationsAndConsumersOnlyConsumeOnce() throws Exception {
        var players = new FirstJoinPlayerHolder();
        try (var executor = Executors.newFixedThreadPool(8)) {
            var registrations = Collections.nCopies(8, (Callable<Void>) () -> {
                players.add(PLAYER);
                return null;
            });
            for (var registration : executor.invokeAll(registrations)) {
                registration.get();
            }
            var removals = executor.invokeAll(Collections.nCopies(8, (Callable<Boolean>) () -> players.remove(PLAYER)));
            int consumed = 0;
            for (var removal : removals) {
                if (removal.get()) {
                    consumed++;
                }
            }
            assertEquals(1, consumed);
        }
        assertFalse(players.remove(PLAYER));
    }

    @Test
    void testConcurrentAddAndRemoveOfDifferentPlayers() throws Exception {
        var players = new FirstJoinPlayerHolder();
        var operations = IntStream.range(0, 8).mapToObj(worker -> (Callable<Void>) () -> {
            for (int i = 0; i < 1000; i++) {
                var uuid = new UUID(worker, i);
                players.add(uuid);
                assertTrue(players.remove(uuid));
                assertFalse(players.remove(uuid));
            }
            return null;
        }).toList();
        try (var executor = Executors.newFixedThreadPool(8)) {
            for (var operation : executor.invokeAll(operations)) {
                operation.get();
            }
        }
    }
}
