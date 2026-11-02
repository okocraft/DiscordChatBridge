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

    public Server server = new Server();
    public Command command = new Command();

    void validate() {
        if (this.server == null || this.server.playerList == null ||
                this.command == null || this.command.reload == null) {
            throw new IllegalArgumentException("server, server.player-list, command and command.reload must be mappings");
        }
    }

    @ConfigSerializable
    public static final class Server {
        public String chat = "%player_name%: %message%";
        public String join = ":heavy_plus_sign: **%player_name%** joined the server.";
        public String firstJoin = ":heavy_plus_sign: **%player_name%** joined the server for the first time! :tada:";
        public String leave = ":heavy_plus_sign: **%player_name%** left the server.";

        @Setting("switch")
        public String switchMessage = ":heavy_plus_sign: **%player_name%** moved to **%server_name%**";

        public PlayerList playerList = new PlayerList();
    }

    @ConfigSerializable
    public static final class PlayerList {
        public String top = "**===== Player List (%player_count%) =====**";
        public String list = "%server%: %player_list%";
    }

    @ConfigSerializable
    public static final class Command {
        public String noPermission = "* You have no permission: %permission%";
        public Reload reload = new Reload();
    }

    @ConfigSerializable
    public static final class Reload {
        public String start = "* Reloading DiscordChatBridge...";
        public String success = "* The reload was successful.";
        public String failure = "* Failed to reload. Please check the console.";
    }
}
