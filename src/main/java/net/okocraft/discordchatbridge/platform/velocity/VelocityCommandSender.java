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

package net.okocraft.discordchatbridge.platform.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.okocraft.discordchatbridge.command.model.Sender;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

class VelocityCommandSender implements Sender {

    private final CommandSource original;

    VelocityCommandSender(@NotNull CommandSource original) {
        this.original = original;
    }

    @Override
    public boolean hasPermission(@NotNull String permissionNode) {
        return original.hasPermission(permissionNode);
    }

    @Override
    public void sendMessage(@NotNull String message) {
        original.sendMessage(LegacyComponentSerializer.legacySection().deserialize(message));
    }

    @Override
    public boolean isPlayer() {
        return original instanceof Player;
    }

    @Override
    public @NotNull UUID uuid() {
        if (isPlayer()) {
            return ((Player) original).getUniqueId();
        } else {
            throw new IllegalStateException(original + " is not a player.");
        }
    }

    @Override
    public @NotNull String name() {
        if (isPlayer()) {
            return ((Player) original).getUsername();
        } else {
            throw new IllegalStateException(original + " is not a player.");
        }
    }
}
