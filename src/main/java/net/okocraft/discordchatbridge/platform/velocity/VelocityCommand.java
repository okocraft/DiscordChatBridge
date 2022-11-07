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

import com.velocitypowered.api.command.SimpleCommand;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.command.DiscordChatBridgeCommand;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VelocityCommand implements SimpleCommand {

    private final DiscordChatBridgePlugin plugin;

    public VelocityCommand(@NotNull DiscordChatBridgePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        DiscordChatBridgeCommand.onCommand(plugin, new VelocityCommandSender(invocation.source()), invocation.arguments());
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return DiscordChatBridgeCommand.onTabComplete(new VelocityCommandSender(invocation.source()), invocation.arguments());
    }
}
