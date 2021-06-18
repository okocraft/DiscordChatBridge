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

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.config.FormatSettings;
import net.okocraft.discordchatbridge.platform.PlatformInfo;
import net.okocraft.discordchatbridge.util.PlayerListFormatter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

class BungeePlatform implements PlatformInfo {

    private final DiscordChatBridgePlugin plugin;

    BungeePlatform(@NotNull DiscordChatBridgePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public int getNumberOfPlayers() {
        return ProxyServer.getInstance().getOnlineCount();
    }

    @Override
    public @NotNull Collection<String> getPlayerListsPerServer() {
        var format = plugin.getFormatConfig().get(FormatSettings.PLAYER_LIST_FORMAT);
        var list = new ArrayList<String>();

        for (var server : ProxyServer.getInstance().getServers().values()) {
            var players =
                    server.getPlayers()
                            .stream()
                            .map(CommandSender::getName)
                            .sorted()
                            .collect(Collectors.toUnmodifiableList());

            list.add(PlayerListFormatter.format(format, server.getName(), players));
        }

        return list;
    }
}
