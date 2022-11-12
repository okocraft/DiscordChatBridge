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

package net.okocraft.discordchatbridge.config;

import com.github.siroshun09.configapi.api.value.ConfigValue;

public final class FormatSettings {

    public static final ConfigValue<String> DISCORD_CHAT =
            config -> config.getString("server.chat", "%player_name%: %message%");

    public static final ConfigValue<String> SERVER_JOIN =
            config -> config.getString("server.join", ":heavy_plus_sign: **%player_name%** joined the server.");

    public static final ConfigValue<String> SERVER_FIRST_JOIN =
            config -> config.getString("server.first-join", ":heavy_plus_sign: **%player_name%** joined the server for the first time! :tada:");

    public static final ConfigValue<String> SERVER_LEAVE =
            config -> config.getString("server.leave", ":heavy_plus_sign: **%player_name%** left the server.");

    public static final ConfigValue<String> SERVER_SWITCH =
            config -> config.getString(
                    "server.switch",
                    ":heavy_plus_sign: **%player_name%** moved to **%server_name%**"
            );

    public static final ConfigValue<String> PLAYER_LIST_TOP =
            config -> config.getString("server.player-list.top", "**===== Player List (%player_count%) =====**");

    public static final ConfigValue<String> PLAYER_LIST_FORMAT =
            config -> config.getString("server.player-list.list", "%server%: %player_list%");

    public static final ConfigValue<String> CHANNEL_NOT_FOUND =
            config -> config.getString("server.channel-not-found", "This channel is configured incorrectly. Please report to administrator.");

    public static final ConfigValue<String> LUNACHAT_YOU_ARE_BANNED =
            config -> config.getString("server.you-are-banned-in-lunachat", "You are banned in this lunachat channel.");

    public static final ConfigValue<String> YOU_ARE_BANNED =
            config -> config.getString("server.you-are-banned", "You are banned in this server.");

    public static final ConfigValue<String> YOU_ARE_MUTED =
            config -> config.getString("server.you-are-muted", "You are muted.");

    public static final ConfigValue<String> NO_LUNACHAT_SPEAK_PERMISSION =
            config -> config.getString("server.no-lunachat-speak-permission", "You do not have in-game permission to speak here.");

    public static final ConfigValue<String> PLEASE_VERIFY =
            config -> config.getString("server.please-verify", "Please verify your discord account. Use command \"/dcb link\" in game.");

    public static final ConfigValue<String> SERVER_NOT_ENOUGH_ARGUMENTS =
            config -> config.getString("server.not-enough-arguments", "Not enough arguments.");

    public static final ConfigValue<String> LINKED =
            config -> config.getString("server.linked", "%player_name% linked discord and minecraft account.");

    public static final ConfigValue<String> INVALID_PASSCODE =
            config -> config.getString("server.invalid-passcode", "Invalid passcode.");

    public static final ConfigValue<String> COMMAND_NO_PERMISSION =
            config -> config.getString("command.no-permission", "* You have no permission: %permission%");

    public static final ConfigValue<String> COMMAND_INVALID_ARGUMENT = config -> config.getString("command.invalid-argument", "* Invalid argument.");

    public static final ConfigValue<String> COMMAND_NOT_ENOUGH_ARGUMENTS = config -> config.getString("command.not-enough-arguments", "Not enough arguments.");

    public static final ConfigValue<String> COMMAND_PLAYER_ONLY =
            config -> config.getString("command.player-only", "* This command is player only.");

    public static final ConfigValue<String> COMMAND_THEN_USE_COMMAND_IN_DISCORD =
            config -> config.getString("command.then-use-command-in-discord", "Accepted link request. Then use command `!link %passcode%` in discord.");

    public static final ConfigValue<String> COMMAND_RELOAD_START =
            config -> config.getString("command.reload.start", "* Reloading DiscordChatBridge...");

    public static final ConfigValue<String> COMMAND_RELOAD_SUCCESS =
            config -> config.getString("command.reload.success", "* The reload was successful.");

    public static final ConfigValue<String> COMMAND_RELOAD_FAILURE =
            config -> config.getString("command.reload.failure", "* Failed to reload. Please check the console.");

    public static final ConfigValue<String> COMMAND_RECORD_HELP =
            config -> config.getString("command.reload.help", "/dcb reload - Reloads config.yml and format.yml");

    public static final ConfigValue<String> COMMAND_LINK_HELP =
            config -> config.getString("command.link.help", "/dcb link - Requests Discord-Minecraft account link.");

    private FormatSettings() {
        throw new UnsupportedOperationException();
    }
}
