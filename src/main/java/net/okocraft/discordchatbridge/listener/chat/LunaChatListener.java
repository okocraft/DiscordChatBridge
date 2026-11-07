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

package net.okocraft.discordchatbridge.listener.chat;

import com.github.ucchyocean.lc3.member.ChannelMember;
import com.github.ucchyocean.lc3.member.ChannelMemberOther;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import org.jetbrains.annotations.NotNull;

public abstract class LunaChatListener extends AbstractChatListener {

    private final DiscordChatBridgePlugin plugin;

    public LunaChatListener(@NotNull DiscordChatBridgePlugin plugin) {
        super(plugin);

        this.plugin = plugin;
    }

    protected void processChat(@NotNull String channelName, @NotNull ChannelMember member, @NotNull String message) {
        var config = this.plugin.getGeneralConfig();
        var sourceName = config.discordSourceName;

        // Discord messages are relayed via Channel#chatFromOtherSource, which names the member "<sender>@<source>".
        if (member instanceof ChannelMemberOther && (sourceName.isEmpty() || member.getName().endsWith("@" + sourceName))) {
            return;
        }

        var id = config.channels.get(channelName);

        if (id == null || id == 0) {
            return;
        }

        this.sendChatToDiscord(id, message, member.getName(), member.getDisplayName());
    }
}
