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

    public static final ConfigValue<String> COMMAND_NO_PERMISSION =
            config -> config.getString("command.no-permission", "* You have no permission: %permission%");

    public static final ConfigValue<String> NOT_ENOUGH_ARGUMENTS =
            config -> config.getString("command.not-enough-arguments", "&c Not enough arguments.");

    public static final ConfigValue<String> PLAYER_ONLY =
            config -> config.getString("command.player-only", "&c* This command is player only.");

    public static final ConfigValue<String> LINK_SUCCESS =
            config -> config.getString("command.link.success", "&7* Linked discord and minecraft account.");

    public static final ConfigValue<String> LINK_INVALID_PASSCODE =
            config -> config.getString("command.link.invalid-passcode", "&c* Invalid passcode.");

    public static final ConfigValue<String> COMMAND_RELOAD_START =
            config -> config.getString("command.reload.start", "* Reloading DiscordChatBridge...");

    public static final ConfigValue<String> COMMAND_RELOAD_SUCCESS =
            config -> config.getString("command.reload.success", "* The reload was successful.");

    public static final ConfigValue<String> COMMAND_RELOAD_FAILURE =
            config -> config.getString("command.reload.failure", "* Failed to reload. Please check the console.");

    public static final ConfigValue<String> CHANNEL_NOT_FOUND =
            config -> config.getString("chat.channel-not-found", "This channel is configured incorrectly. Please report to administrator.");

    public static final ConfigValue<String> NOT_CHANNEL_MEMBER =
            config -> config.getString("chat.not-channel-member", "You are not a channel member.");

    public static final ConfigValue<String> YOU_ARE_BANNED =
            config -> config.getString("chat.you-are-banned-in-this-channel", "You are banned in this channel.");

    public static final ConfigValue<String> YOU_ARE_MUTED =
            config -> config.getString("chat.you-are-muted", "You are muted.");

    public static final ConfigValue<String> NO_SPEAK_PERMISSION =
            config -> config.getString("chat.no-speak-permission", "You do not have in-game permission to speak here.");

    public static final ConfigValue<String> VERIFY_PLEASE =
            config -> config.getString("chat.verify-please", "Please verify your discord account. Use command `/dcb link` in game.");

    private FormatSettings() {
        throw new UnsupportedOperationException();
    }
}
