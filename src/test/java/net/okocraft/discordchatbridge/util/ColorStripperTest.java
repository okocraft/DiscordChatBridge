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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColorStripperTest {

    @ParameterizedTest
    @NullAndEmptySource
    void testStripReturnsEmptyForAbsentText(String text) {
        assertEquals("", ColorStripper.strip(text));
    }

    @Test
    void testStripRemovesMixedCaseColorsAndFormatting() {
        assertEquals("Hello world!", ColorStripper.strip("&aHello §Bworld&r!"));
        assertEquals("text", ColorStripper.strip("§k&o&L§n&mtext§r"));
    }

    @Test
    void testStripPreservesUnicodeWhitespaceAndOrdinaryAmpersands() {
        var text = "  日本語 😀 & friends\nnext line &z §? &";
        assertEquals(text, ColorStripper.strip(text));
    }
}
