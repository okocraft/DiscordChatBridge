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
import net.okocraft.discordchatbridge.platform.PlatformInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

class BungeePlatform implements PlatformInfo {

    @Override
    public int getNumberOfPlayers() {
        return ProxyServer.getInstance().getOnlineCount();
    }

    @Override
    public @NotNull Collection<String> getPlayerListsPerServer() {
        return ProxyServer.getInstance()
                .getPlayers()
                .stream()
                .map(CommandSender::getName)
                .sorted()
                .collect(Collectors.toUnmodifiableList());
    }
}
