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

import com.github.siroshun09.configapi.api.Configuration;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.okocraft.discordchatbridge.config.FormatSettings;
import net.okocraft.discordchatbridge.platform.PlatformInfo;
import net.okocraft.discordchatbridge.util.PlayerListFormatter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

class VelocityPlatform implements PlatformInfo {

    private final ProxyServer server;
    private final Configuration formatConfig;

    VelocityPlatform(@NotNull ProxyServer server, @NotNull Configuration formatConfig) {
        this.server = server;
        this.formatConfig = formatConfig;
    }

    @Override
    public int getNumberOfPlayers() {
        return this.server.getPlayerCount();
    }

    @Override
    public @NotNull Collection<String> getPlayerListsPerServer() {
        var format = this.formatConfig.get(FormatSettings.PLAYER_LIST_FORMAT);
        var list = new ArrayList<String>();

        for (var server : this.server.getAllServers()) {
            if (server.getPlayersConnected().isEmpty()) {
                continue;
            }

            var players =
                    server.getPlayersConnected()
                            .stream()
                            .map(Player::getUsername)
                            .sorted()
                            .toList();

            list.add(PlayerListFormatter.format(format, server.getServerInfo().getName(), players));
        }

        return list;
    }
}
