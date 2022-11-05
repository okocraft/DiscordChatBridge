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

package net.okocraft.discordchatbridge.platform.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.okocraft.discordchatbridge.chat.ChatSystem;
import net.okocraft.discordchatbridge.constant.Constants;
import net.okocraft.discordchatbridge.database.LinkedUser;
import net.okocraft.discordchatbridge.util.VanillaChatFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class VelocityChatSystem implements ChatSystem {

    private final ProxyServer server;

    VelocityChatSystem(@NotNull ProxyServer server) {
        this.server = server;
    }

    @Override
    public @NotNull Result sendChat(@NotNull String channelName, @NotNull String sender, @NotNull String source,
                                    @NotNull String message, @Nullable LinkedUser linkedUser) {
        if (channelName.equals(Constants.GLOBAL_CHANNEL_NAME)) {
            var chat = VanillaChatFormatter.format(sender, source, message);
            var component = LegacyComponentSerializer.legacyAmpersand().deserialize(chat);
            server.getAllPlayers().forEach(player -> player.sendMessage(component));
        }

        return Result.success();
    }
}
