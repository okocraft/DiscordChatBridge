/*
 *     Copyright (c) 2025 Okocraft
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

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public final class ColorStripper {

    private static final String EMPTY = "";
    private static final Pattern COLOR_SECTION_PATTERN = Pattern.compile("(?i)[&§][0-9A-FK-ORX]");

    public static @NotNull String strip(String str) {
        if (str == null || str.isEmpty()) {
            return EMPTY;
        }

        return COLOR_SECTION_PATTERN.matcher(str).replaceAll(EMPTY);
    }

    private ColorStripper() {
        throw new UnsupportedOperationException();
    }
}
