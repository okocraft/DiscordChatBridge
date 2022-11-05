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

package net.okocraft.discordchatbridge.chat;

import com.github.siroshun09.configapi.api.value.ConfigValue;
import net.okocraft.discordchatbridge.database.LinkedUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ChatSystem {

    @NotNull Result sendChat(@NotNull String channelName, @NotNull String sender,
                             @NotNull String source, @NotNull String message, @Nullable LinkedUser linkedUser);

    final class Result {

        private static final Result SUCCESS = new Result(null, false);

        public static @NotNull Result success() {
            return SUCCESS;
        }

        public static @NotNull Result failure(@NotNull ConfigValue<String> reasonMessageKey) {
            return new Result(reasonMessageKey, false);
        }

        public static @NotNull Result failureAndDeleteMessage(@NotNull ConfigValue<String> reasonMessageKey) {
            return new Result(reasonMessageKey, true);
        }

        private final ConfigValue<String> reasonMessageKey;
        private final boolean shouldDeleteMessage;

        private Result(@Nullable ConfigValue<String> reasonMessageKey,  boolean shouldDeleteMessage) {
            this.reasonMessageKey = reasonMessageKey;
            this.shouldDeleteMessage = shouldDeleteMessage;
        }

        public boolean succeed() {
            return reasonMessageKey == null;
        }

        public @NotNull ConfigValue<String> reasonMessageKey() {
            if (reasonMessageKey == null) {
                throw new IllegalArgumentException("There is no message when succeed.");
            }

            return reasonMessageKey;
        }

        public boolean shouldDeleteMessage() {
            return shouldDeleteMessage;
        }
    }
}
