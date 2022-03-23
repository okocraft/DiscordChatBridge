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

import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.config.GeneralSettings;
import net.okocraft.discordchatbridge.constant.Constants;
import org.jetbrains.annotations.NotNull;

public abstract class VanillaChatListener extends AbstractChatListener {

    private final DiscordChatBridgePlugin plugin;

    protected VanillaChatListener(@NotNull DiscordChatBridgePlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    public void processChat(@NotNull String channelName, @NotNull String name,
                            @NotNull String displayName, @NotNull String message) {
        if (channelName.equals(Constants.GLOBAL_CHANNEL_NAME)) {
            long systemChannelID = plugin.getGeneralConfig().get(GeneralSettings.SYSTEM_CHANNEL);

            if (systemChannelID != 0) {
                sendChatToDiscord(systemChannelID, message, name, displayName);
            }
        }
    }
}
