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
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.command.DiscordChatBridgeCommand;
import org.jetbrains.annotations.NotNull;

public class BungeeCommand extends Command implements TabExecutor {

    private final DiscordChatBridgePlugin plugin;

    public BungeeCommand(@NotNull DiscordChatBridgePlugin plugin) {
        super("discordchatbridge", null, "dcb");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        DiscordChatBridgeCommand.onCommand(plugin, new BungeeCommandSender(sender), args);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return DiscordChatBridgeCommand.onTabComplete(new BungeeCommandSender(sender), args);
    }
}
