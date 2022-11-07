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

package net.okocraft.discordchatbridge.command;

import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.command.model.Sender;
import net.okocraft.discordchatbridge.config.FormatSettings;
import net.okocraft.discordchatbridge.constant.Placeholders;
import org.jetbrains.annotations.NotNull;

final class ReloadCommand {

    final static String PERMISSION = "discordchatbridge.reload";

    static void processCommand(@NotNull DiscordChatBridgePlugin plugin, @NotNull Sender sender) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(
                    plugin.getFormatConfig()
                            .get(FormatSettings.COMMAND_NO_PERMISSION)
                            .replace(Placeholders.PERMISSION, PERMISSION)
            );
        }

        sender.sendMessage(plugin.getFormatConfig().get(FormatSettings.COMMAND_RELOAD_START));

        plugin.disable();

        if (plugin.load() && plugin.enable()) {
            sender.sendMessage(plugin.getFormatConfig().get(FormatSettings.COMMAND_RELOAD_SUCCESS));
        } else {
            sender.sendMessage(plugin.getFormatConfig().get(FormatSettings.COMMAND_RELOAD_FAILURE));
        }
    }
}
