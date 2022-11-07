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
import net.okocraft.discordchatbridge.session.LinkRequestContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

final class LinkCommand {

    final static String PERMISSION = "discordchatbridge.link";

    static void processCommand(@NotNull DiscordChatBridgePlugin plugin, @NotNull Sender sender) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(
                    plugin.getFormatConfig()
                            .get(FormatSettings.COMMAND_NO_PERMISSION)
                            .replace(Placeholders.PERMISSION, PERMISSION)
            );
            return;
        }

        if (!sender.isPlayer()) {
            sender.sendMessage(plugin.getFormatConfig().get(FormatSettings.COMMAND_PLAYER_ONLY));
            return;
        }

        String passcode = String.valueOf(new Random().nextInt(10000));
        LinkRequestContainer.add(passcode, sender.uuid(), sender.name());

        sender.sendMessage(
                plugin.getFormatConfig()
                        .get(FormatSettings.COMMAND_THEN_USE_COMMAND_IN_DISCORD)
                        .replace(Placeholders.PASSCODE, passcode)
        );
    }
}
