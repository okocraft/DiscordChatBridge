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

package net.okocraft.discordchatbridge.platform.velocity;

import com.github.ucchyocean.lc3.LunaChatBungee;
import com.github.ucchyocean.lc3.channel.Channel;
import net.okocraft.discordchatbridge.chat.LunaChatSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SnapLunaChatSystemIntegrator extends LunaChatSystem {

    @Override
    protected @Nullable Channel getChannel(@NotNull String channelName) {
        return LunaChatBungee.getInstance().getLunaChatAPI().getChannel(channelName);
    }
}
