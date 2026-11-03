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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaperChatSystemTest {

    @ParameterizedTest
    @ValueSource(strings = {"&cRed &lbold &rreset", "§cRed §lbold", "<red>text</red>",
            "😀 日本語 &x&f&f&0&0&0&0", "https://example.com/?a=1&code=2", ""})
    void testDiscordBodyIsLiteralTextWithoutFormatting(String message) {
        var captured = ArgumentCaptor.forClass(Component.class);
        try (var bukkit = mockStatic(Bukkit.class)) {
            new PaperChatSystem().sendChat("global", "Alice", "Dis", message);

            bukkit.verify(() -> Bukkit.broadcast(captured.capture()));
            bukkit.verifyNoMoreInteractions();
        }

        var result = captured.getValue();
        assertEquals("<Alice@Dis> " + message, PlainTextComponentSerializer.plainText().serialize(result));
        if (!message.isEmpty()) {
            var body = assertInstanceOf(TextComponent.class, result.children().getLast());
            assertEquals(message, body.content());
            assertNull(body.color());
            assertTrue(body.style().decorations().values().stream().noneMatch(state -> state == TextDecoration.State.TRUE));
        }
    }

    @Test
    void testRoleAndNameColorsArePreservedWithoutColoringBody() {
        var captured = ArgumentCaptor.forClass(Component.class);
        try (var bukkit = mockStatic(Bukkit.class)) {
            new PaperChatSystem().sendChat("global", "&a*&bAlice", "&eDis", "&cLiteral body");

            bukkit.verify(() -> Bukkit.broadcast(captured.capture()));
        }

        var result = captured.getValue();
        assertEquals("<*Alice@Dis> &cLiteral body", PlainTextComponentSerializer.plainText().serialize(result));
        var serialized = LegacyComponentSerializer.legacySection().serialize(result);
        assertTrue(serialized.contains("§a*§bAlice"));
        assertTrue(serialized.contains("§eDis"));
        assertEquals("&cLiteral body", ((TextComponent) result.children().getLast()).content());
        assertNull(result.children().getLast().color());
        assertNull(result.color());
    }

    @Test
    void testEmptySourceKeepsSenderOnlyFormat() {
        var captured = ArgumentCaptor.forClass(Component.class);
        try (var bukkit = mockStatic(Bukkit.class)) {
            new PaperChatSystem().sendChat("global", "Alice", "", "hello");

            bukkit.verify(() -> Bukkit.broadcast(captured.capture()));
        }

        assertEquals("<Alice> hello", PlainTextComponentSerializer.plainText().serialize(captured.getValue()));
    }

    @Test
    void testOtherChannelIsNotBroadcast() {
        try (var bukkit = mockStatic(Bukkit.class)) {
            new PaperChatSystem().sendChat("private", "Alice", "Dis", "hello");

            bukkit.verifyNoInteractions();
        }
    }
}
