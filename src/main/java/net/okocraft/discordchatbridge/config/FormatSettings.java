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
            config -> config.getString("command.no-permission", "&c* You have no permission: %permission%");

    public static final ConfigValue<String> COMMAND_RELOAD_START =
            config -> config.getString("command.reload.start", "&7* Reloading DiscordChatBridge...");

    public static final ConfigValue<String> COMMAND_RELOAD_SUCCESS =
            config -> config.getString("command.reload.success", "&7* The reload was successful.");

    public static final ConfigValue<String> COMMAND_RELOAD_FAILURE =
            config -> config.getString("command.reload.failure", "&c* Failed to reload. Please check the console.");

    private FormatSettings() {
        throw new UnsupportedOperationException();
    }
}
