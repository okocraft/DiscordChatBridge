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

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.constant.Constants;
import net.okocraft.discordchatbridge.listener.chat.VanillaChatListener;
import org.jetbrains.annotations.NotNull;

public class BungeeChatListener extends VanillaChatListener implements Listener {

    protected BungeeChatListener(@NotNull DiscordChatBridgePlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(@NotNull ChatEvent e) {
        if (e.isCancelled() || e.isCommand() || e.isProxyCommand()) {
            return;
        }

        if (!(e.getSender() instanceof ProxiedPlayer)) {
            return;
        }

        var sender = (ProxiedPlayer) e.getSender();

        processChat(Constants.GLOBAL_CHANNEL_NAME, sender.getName(), sender.getDisplayName(), e.getMessage());
    }
}
