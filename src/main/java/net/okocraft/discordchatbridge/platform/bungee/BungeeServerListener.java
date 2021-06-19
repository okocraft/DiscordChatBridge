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

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.listener.ServerListener;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public class BungeeServerListener extends ServerListener implements Listener {

    public BungeeServerListener(@NotNull DiscordChatBridgePlugin plugin) {
        super(plugin);

        var players = ProxyServer.getInstance().getPlayers();

        if (!players.isEmpty()) {
            addJoinedPlayers(
                    players.stream()
                            .map(ProxiedPlayer::getUniqueId)
                            .collect(Collectors.toUnmodifiableList())
            );
        }
    }

    @EventHandler
    public void onJoinOrSwitch(@NotNull ServerSwitchEvent e) {
        var player = e.getPlayer();
        if (e.getFrom() == null) {
            processJoin(player.getUniqueId(), player.getName(), player.getDisplayName());
        } else {
            processServerSwitch(player.getName(), player.getDisplayName(), e.getPlayer().getServer().getInfo().getName());
        }
    }

    @EventHandler
    public void onDisconnect(@NotNull PlayerDisconnectEvent e) {
        var player = e.getPlayer();
        processDisconnection(player.getUniqueId(), player.getName(), player.getDisplayName());
    }
}
