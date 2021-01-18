/*
 *     Copyright (c) 2021 Okocraft
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

import com.github.siroshun09.mcmessage.util.Colorizer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.okocraft.discordchatbridge.DiscordChatBridge;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand extends Command {

    private final static String PERM = "discordchatbridge.reload";

    private final DiscordChatBridge plugin;

    public ReloadCommand(@NotNull DiscordChatBridge plugin) {
        super("dcbreload");

        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PERM)) {
            sendMessage(sender, plugin.getFormatConfig().getNoPermissionMessage().replace("%perm%", PERM));
            return;
        }

        sendMessage(sender, plugin.getFormatConfig().getReloadingMessage());

        if (plugin.reload()) {
            sendMessage(sender, plugin.getFormatConfig().getReloadSuccessMessage());
        } else {
            sendMessage(sender, plugin.getFormatConfig().getReloadFailureMessage());
        }
    }

    private void sendMessage(@NotNull CommandSender sender, @NotNull String message) {
        sender.sendMessage(TextComponent.fromLegacyText(Colorizer.colorize(message)));
    }
}
