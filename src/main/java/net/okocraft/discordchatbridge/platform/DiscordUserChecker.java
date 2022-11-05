/*
 *     Copyright (c) 2022 Okocraft
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

package net.okocraft.discordchatbridge.platform;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.okocraft.discordchatbridge.database.LinkedUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DiscordUserChecker {

    @NotNull Result check(@NotNull LinkedUser user);

    final class Result {

        private static final Result ALLOW = new Result(null);

        public static @NotNull Result allow() {
            return ALLOW;
        }

        public static @NotNull Result deny(@NotNull String reasonMessage) {
            return new Result(reasonMessage);
        }

        private final String reasonMessage;

        private Result(@Nullable String reasonMessage) {
            this.reasonMessage = reasonMessage;
        }

        public boolean allowed() {
            return reasonMessage == null;
        }

        public @NotNull String reasonMessage() {
            if (reasonMessage == null) {
                throw new IllegalArgumentException("There is no message when allowed.");
            }

            return reasonMessage;
        }
    }
}
