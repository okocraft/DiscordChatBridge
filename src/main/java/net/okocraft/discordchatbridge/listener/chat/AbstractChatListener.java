/*
 *     Copyright (c) 2021 Okocraft
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

package net.okocraft.discordchatbridge.listener.chat;

import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.util.ColorStripper;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractChatListener {

    private final DiscordChatBridgePlugin plugin;

    protected AbstractChatListener(@NotNull DiscordChatBridgePlugin plugin) {
        this.plugin = plugin;
    }

    protected void sendChatToDiscord(long id, @NotNull String message,
                                     @NotNull String name, @NotNull String displayName) {
        plugin.getBot().sendChat(
                id,
                ColorStripper.strip(message),
                name,
                ColorStripper.strip(displayName)
        );
    }
}
