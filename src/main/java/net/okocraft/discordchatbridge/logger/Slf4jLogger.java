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

package net.okocraft.discordchatbridge.logger;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class Slf4jLogger implements LoggerWrapper {

    private final Logger logger;

    public Slf4jLogger(@NotNull Logger logger) {
        this.logger = logger;
    }

    @Override
    public void warning(@NotNull String log) {
        logger.warn(log);
    }

    @Override
    public void error(@NotNull String message, @NotNull Throwable exception) {
        logger.error(message, exception);
    }
}
