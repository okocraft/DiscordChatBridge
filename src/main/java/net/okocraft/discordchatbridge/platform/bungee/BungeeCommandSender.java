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

package net.okocraft.discordchatbridge.platform.bungee;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.okocraft.discordchatbridge.command.model.Sender;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

class BungeeCommandSender implements Sender {

    private final CommandSender original;

    BungeeCommandSender(@NotNull CommandSender original) {
        this.original = original;
    }

    @Override
    public boolean hasPermission(@NotNull String permissionNode) {
        return original.hasPermission(permissionNode);
    }

    @Override
    public void sendMessage(@NotNull String message) {
        original.sendMessage(TextComponent.fromLegacyText(message));
    }

    @Override
    public boolean isPlayer() {
        return original instanceof ProxiedPlayer;
    }

    @Override
    public @NotNull UUID uuid() {
        if (isPlayer()) {
            return ((ProxiedPlayer) original).getUniqueId();
        } else {
            throw new IllegalStateException(original + " is not a player.");
        }
    }

    @Override
    public @NotNull String name() {
        return original.getName();
    }
}
