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

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.command.LinkCommand;
import net.okocraft.discordchatbridge.command.ReloadCommand;
import net.okocraft.discordchatbridge.config.FormatSettings;
import org.jetbrains.annotations.NotNull;

public class BungeeCommand extends Command implements TabExecutor {

    DiscordChatBridgePlugin plugin;

    private final ReloadCommand reloadCommand;
    private final LinkCommand linkCommand;

    public BungeeCommand(@NotNull DiscordChatBridgePlugin plugin) {
        super("discordchatbridge", null, "dcb");
        this.reloadCommand = new ReloadCommand(plugin);
        this.linkCommand = new LinkCommand(plugin);

        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.getFormatConfig().get(FormatSettings.COMMAND_NOT_ENOUGH_ARGUMENTS));
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            reloadCommand.processCommand(
                    sender::hasPermission,
                    str -> sender.sendMessage(TextComponent.fromLegacyText(str))
            );
        } else if (args[0].equalsIgnoreCase("link")) {
            if (!(sender instanceof ProxiedPlayer)) {
                sender.sendMessage(TextComponent.fromLegacyText(plugin.getFormatConfig().get(FormatSettings.COMMAND_PLAYER_ONLY)));
                return;
            }
            linkCommand.processCommand(
                    sender::hasPermission,
                    str -> sender.sendMessage(TextComponent.fromLegacyText(str)),
                    args,
                    ((ProxiedPlayer) sender).getUniqueId(),
                    sender.getName()
            );
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Stream.of("reload", "link")
                    .filter(sub -> sub.startsWith(args[0]))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
