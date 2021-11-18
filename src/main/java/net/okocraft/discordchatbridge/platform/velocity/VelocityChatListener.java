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

package net.okocraft.discordchatbridge.platform.velocity;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.constant.Constants;
import net.okocraft.discordchatbridge.listener.chat.VanillaChatListener;
import net.okocraft.discordchatbridge.util.AdventureVanillaChatFormatter;
import org.jetbrains.annotations.NotNull;

class VelocityChatListener extends VanillaChatListener {

    private final ProxyServer server;

    VelocityChatListener(@NotNull DiscordChatBridgePlugin plugin, @NotNull ProxyServer server) {
        super(plugin);
        this.server = server;
    }

    @Subscribe(order = PostOrder.LAST)
    public void onChat(@NotNull PlayerChatEvent event) {
        if (!event.getResult().isAllowed()) {
            return;
        }

        var sender = event.getPlayer();

        processChat(Constants.GLOBAL_CHANNEL_NAME, sender.getUsername(), sender.getUsername(), event.getMessage());
        server.sendMessage(AdventureVanillaChatFormatter.format(sender.getUsername(), Component.text(event.getMessage())));
        event.setResult(PlayerChatEvent.ChatResult.denied());
    }
}
