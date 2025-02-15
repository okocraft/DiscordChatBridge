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

package net.okocraft.discordchatbridge.platform.bungee;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.command.ReloadCommand;
import org.jetbrains.annotations.NotNull;

public class BungeeReloadCommand extends Command {

    private final ReloadCommand reloadCommand;

    public BungeeReloadCommand(@NotNull DiscordChatBridgePlugin plugin) {
        super("dcbreload");
        this.reloadCommand = new ReloadCommand(plugin);
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        reloadCommand.processCommand(
                commandSender::hasPermission,
                str -> commandSender.sendMessage(TextComponent.fromLegacyText(str))
        );
    }
}
