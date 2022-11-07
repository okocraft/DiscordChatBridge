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

import com.github.siroshun09.configapi.api.Configuration;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.command.model.Sender;
import net.okocraft.discordchatbridge.config.FormatSettings;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DiscordChatBridgeCommand {

    public static void onCommand(@NotNull DiscordChatBridgePlugin plugin, @NotNull Sender sender, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.getFormatConfig().get(FormatSettings.COMMAND_NOT_ENOUGH_ARGUMENTS));
            sendHelp(sender, plugin.getFormatConfig());
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            ReloadCommand.processCommand(plugin, sender);
            return;
        }

        if (args[0].equalsIgnoreCase("link")) {
            LinkCommand.processCommand(plugin, sender);
            return;
        }

        sender.sendMessage(plugin.getFormatConfig().get(FormatSettings.COMMAND_INVALID_ARGUMENT));
        sendHelp(sender, plugin.getFormatConfig());
    }

    public static @NotNull List<String> onTabComplete(@NotNull Sender sender, @NotNull String[] args) {
        if (args.length == 0) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            var result = new ArrayList<String>(2);

            if ("reload".startsWith(args[0]) && sender.hasPermission(ReloadCommand.PERMISSION)) {
                result.add("reload");
            }

            if ("link".startsWith(args[0]) && sender.hasPermission(LinkCommand.PERMISSION)) {
                result.add("link");
            }

            return result;
        }

        return Collections.emptyList();
    }

    private static void sendHelp(@NotNull Sender sender, @NotNull Configuration formatConfig) {
        if (sender.hasPermission(LinkCommand.PERMISSION)) {
            sender.sendMessage(formatConfig.get(FormatSettings.COMMAND_LINK_HELP));
        }

        if (sender.hasPermission(ReloadCommand.PERMISSION)) {
            sender.sendMessage(formatConfig.get(FormatSettings.COMMAND_RECORD_HELP));
        }
    }
}
