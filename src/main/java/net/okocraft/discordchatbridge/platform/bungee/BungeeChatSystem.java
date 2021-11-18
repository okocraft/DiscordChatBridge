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

package net.okocraft.discordchatbridge.platform.bungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.okocraft.discordchatbridge.chat.ChatSystem;
import net.okocraft.discordchatbridge.constant.Constants;
import net.okocraft.discordchatbridge.util.VanillaChatFormatter;
import org.jetbrains.annotations.NotNull;

public class BungeeChatSystem implements ChatSystem {

    @Override
    public void sendChat(@NotNull String channelName, @NotNull String sender, @NotNull String source, @NotNull String message) {
        if (channelName.equals(Constants.GLOBAL_CHANNEL_NAME)) {
            var chat = VanillaChatFormatter.format(sender, source, message);
            var component = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', chat));
            ProxyServer.getInstance().broadcast(component);
        }
    }
}
