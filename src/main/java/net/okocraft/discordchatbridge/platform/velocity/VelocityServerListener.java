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

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.listener.ServerListener;
import org.jetbrains.annotations.NotNull;

public class VelocityServerListener extends ServerListener {

    public VelocityServerListener(@NotNull DiscordChatBridgePlugin plugin) {
        super(plugin);
    }

    @Subscribe
    public void onJoinOrSwitch(@NotNull ServerConnectedEvent event) {
        var player = event.getPlayer();

        if (event.getPreviousServer().isEmpty()) {
            this.processJoin(player.getUniqueId(), player.getUsername(), player.getUsername());
        } else {
            this.processServerSwitch(player.getUsername(), player.getUsername(), event.getServer().getServerInfo().getName());
        }
    }

    @Subscribe
    public void onDisconnect(@NotNull DisconnectEvent event) {
        var player = event.getPlayer();
        this.processDisconnection(player.getUniqueId(), player.getUsername(), player.getUsername());
    }
}
