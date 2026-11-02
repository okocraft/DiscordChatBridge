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

package net.okocraft.discordchatbridge.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public final class FormatSettings {

    private ServerSettings server = new ServerSettings();
    private CommandSettings command = new CommandSettings();

    public String discordChat() {
        return this.server.chat;
    }

    public String serverJoin() {
        return this.server.join;
    }

    public String serverFirstJoin() {
        return this.server.firstJoin;
    }

    public String serverLeave() {
        return this.server.leave;
    }

    public String serverSwitch() {
        return this.server.switchMessage;
    }

    public String playerListTop() {
        return this.server.playerList.top;
    }

    public String playerListFormat() {
        return this.server.playerList.list;
    }

    public String commandNoPermission() {
        return this.command.noPermission;
    }

    public String commandReloadStart() {
        return this.command.reload.start;
    }

    public String commandReloadSuccess() {
        return this.command.reload.success;
    }

    public String commandReloadFailure() {
        return this.command.reload.failure;
    }

    void validate() {
        if (this.server == null || this.server.playerList == null ||
                this.command == null || this.command.reload == null) {
            throw new IllegalArgumentException("server, server.player-list, command and command.reload must be mappings");
        }
        if (this.server.chat == null || this.server.join == null || this.server.firstJoin == null ||
                this.server.leave == null || this.server.switchMessage == null ||
                this.server.playerList.top == null || this.server.playerList.list == null ||
                this.command.noPermission == null || this.command.reload.start == null ||
                this.command.reload.success == null || this.command.reload.failure == null) {
            throw new IllegalArgumentException("format.yml message values must be strings");
        }
    }

    @ConfigSerializable
    public static final class ServerSettings {
        private String chat = "%player_name%: %message%";
        private String join = ":heavy_plus_sign: **%player_name%** joined the server.";

        @Setting("first-join")
        private String firstJoin = ":heavy_plus_sign: **%player_name%** joined the server for the first time! :tada:";

        private String leave = ":heavy_plus_sign: **%player_name%** left the server.";

        @Setting("switch")
        private String switchMessage = ":heavy_plus_sign: **%player_name%** moved to **%server_name%**";

        @Setting("player-list")
        private PlayerListSettings playerList = new PlayerListSettings();
    }

    @ConfigSerializable
    public static final class PlayerListSettings {
        private String top = "**===== Player List (%player_count%) =====**";
        private String list = "%server%: %player_list%";
    }

    @ConfigSerializable
    public static final class CommandSettings {
        @Setting("no-permission")
        private String noPermission = "* You have no permission: %permission%";

        private ReloadSettings reload = new ReloadSettings();
    }

    @ConfigSerializable
    public static final class ReloadSettings {
        private String start = "* Reloading DiscordChatBridge...";
        private String success = "* The reload was successful.";
        private String failure = "* Failed to reload. Please check the console.";
    }
}
