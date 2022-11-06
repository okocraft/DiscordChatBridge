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

package net.okocraft.discordchatbridge.listener.chat;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.member.ChannelMember;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.chat.lunachat.ChannelMemberDiscord;
import net.okocraft.discordchatbridge.config.GeneralSettings;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class LunaChatListener extends AbstractChatListener {

    private final Map<String, Long> linkedChannels = new HashMap<>();

    public LunaChatListener(@NotNull DiscordChatBridgePlugin plugin) {
        super(plugin);
        var channelSection = plugin.getGeneralConfig().get(GeneralSettings.LINKED_CHANNELS);

        for (var key : channelSection.getKeyList()) {
            var id = channelSection.getLong(key);
            if (id != 0) {
                linkedChannels.put(key, id);
            }
        }
    }

    protected void processChat(@NotNull String channelName,
                               @NotNull ChannelMember member, @NotNull String message) {
        var id = linkedChannels.get(channelName);

        if (id == null) {
            return;
        }

        sendChatToDiscord(id, message, member.getName(), member.getDisplayName());
    }

    protected void processHiding(@NotNull ChannelMemberDiscord sender, @NotNull List<ChannelMember> recipients) {
        var hidelist = LunaChat.getAPI().getHidelist(sender);

        if (hidelist.isEmpty()) {
            return;
        }

        hidelist.forEach(recipients::remove);
    }
}
