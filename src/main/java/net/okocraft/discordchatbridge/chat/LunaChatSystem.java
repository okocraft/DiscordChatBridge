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

package net.okocraft.discordchatbridge.chat;

import com.github.ucchyocean.lc3.channel.Channel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class LunaChatSystem implements ChatSystem {

    @Override
    public void sendChat(@NotNull String channelName, @NotNull String sender,
                         @NotNull String source, @NotNull String message) {
        var channel = this.getChannel(channelName);

        if (channel != null) {
            channel.chatFromOtherSource(sender, source, message);
        }
    }

    protected abstract @Nullable Channel getChannel(@NotNull String channelName);
}
