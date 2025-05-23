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

import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.command.ReloadCommand;
import org.jetbrains.annotations.NotNull;

public class VelocityReloadCommand implements SimpleCommand {

    private final ReloadCommand reloadCommand;

    public VelocityReloadCommand(@NotNull DiscordChatBridgePlugin plugin) {
        reloadCommand = new ReloadCommand(plugin);
    }

    @Override
    public void execute(Invocation invocation) {
        reloadCommand.processCommand(
                permission -> invocation.source().hasPermission(permission),
                str -> invocation.source().sendMessage(LegacyComponentSerializer.legacySection().deserialize(str))
        );
    }
}
