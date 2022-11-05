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
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.command.LinkCommand;
import net.okocraft.discordchatbridge.command.ReloadCommand;
import net.okocraft.discordchatbridge.config.FormatSettings;
import org.jetbrains.annotations.NotNull;

public class VelocityCommand implements SimpleCommand {

    private final ReloadCommand reloadCommand;
    private final LinkCommand linkCommand;
    private final DiscordChatBridgePlugin plugin;

    public VelocityCommand(@NotNull DiscordChatBridgePlugin plugin) {
        reloadCommand = new ReloadCommand(plugin);
        linkCommand = new LinkCommand(plugin);
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (invocation.arguments().length == 0) {
            sender.sendMessage(Component.text(plugin.getFormatConfig().get(FormatSettings.NOT_ENOUGH_ARGUMENTS)));
            return;
        }

        if (invocation.arguments()[0].equalsIgnoreCase("reload")) {
            reloadCommand.processCommand(
                    sender::hasPermission,
                    str -> sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(str))
            );
        } else if (args[0].equalsIgnoreCase("link")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                        .deserialize(plugin.getFormatConfig().get(FormatSettings.PLAYER_ONLY)));
                return;
            }
            linkCommand.processCommand(
                    sender::hasPermission,
                    str -> sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(str)),
                    args,
                    ((Player) sender).getUniqueId(),
                    ((Player) sender).getUsername()
            );
        }
    }
}
