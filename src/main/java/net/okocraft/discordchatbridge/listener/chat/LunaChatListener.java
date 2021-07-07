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

import com.github.ucchyocean.lc3.member.ChannelMember;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.config.GeneralSettings;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract class LunaChatListener extends AbstractChatListener {

    private final DiscordChatBridgePlugin plugin;
    private final Map<String, Long> linkedChannels = new HashMap<>();

    public LunaChatListener(@NotNull DiscordChatBridgePlugin plugin) {
        super(plugin);

        this.plugin = plugin;

        var channelSection = plugin.getGeneralConfig().get(GeneralSettings.LINKED_CHANNELS);

        for (var key : channelSection.getPaths()) {
            var id = channelSection.getLong(key);
            if (id != 0) {
                linkedChannels.put(key, id);
            }
        }
    }

    protected void processChat(@NotNull String channelName, @NotNull String displayName,
                               @NotNull ChannelMember member, @NotNull String message) {
        var sourceName = plugin.getGeneralConfig().get(GeneralSettings.DISCORD_SOURCE_NAME);

        if (displayName.endsWith(sourceName)) {
            return;
        }

        var id = linkedChannels.get(channelName);

        if (id == null) {
            return;
        }

        sendChatToDiscord(id, message, member.getName(), member.getDisplayName());
    }
}
