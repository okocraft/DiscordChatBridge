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

package net.okocraft.discordchatbridge.command;

import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.config.FormatSettings;
import net.okocraft.discordchatbridge.constant.Placeholders;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ReloadCommand {

    private final static String PERMISSION = "discordchatbridge.reload";

    private final DiscordChatBridgePlugin plugin;

    public ReloadCommand(@NotNull DiscordChatBridgePlugin plugin) {
        this.plugin = plugin;
    }

    public void processCommand(@NotNull Predicate<String> permissionChecker,
                                  @NotNull Consumer<String> messageSender) {
        if (!permissionChecker.test(PERMISSION)) {
            messageSender.accept(
                    this.plugin.getFormatConfig()
                            .get(FormatSettings.COMMAND_NO_PERMISSION)
                            .replace(Placeholders.PERMISSION, PERMISSION)
            );
        }

        messageSender.accept(this.plugin.getFormatConfig().get(FormatSettings.COMMAND_RELOAD_START));

        this.plugin.disable();

        if (this.plugin.load() && this.plugin.enable()) {
            messageSender.accept(this.plugin.getFormatConfig().get(FormatSettings.COMMAND_RELOAD_SUCCESS));
        } else {
            messageSender.accept(this.plugin.getFormatConfig().get(FormatSettings.COMMAND_RELOAD_FAILURE));
        }
    }
}
