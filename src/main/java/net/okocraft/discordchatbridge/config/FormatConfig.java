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

package net.okocraft.discordchatbridge.config;

import com.github.siroshun09.configapi.bungee.BungeeYamlFactory;
import com.github.siroshun09.configapi.common.configurable.Configurable;
import com.github.siroshun09.configapi.common.configurable.StringValue;
import com.github.siroshun09.configapi.common.yaml.Yaml;
import net.okocraft.discordchatbridge.DiscordChatBridge;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class FormatConfig {

    private static final StringValue SERVER_CHAT =
            Configurable.create("server.chat", "%player%: %message%");

    private static final StringValue SERVER_JOIN =
            Configurable.create("server.join", ":heavy_plus_sign: **%player%** joined the server.");

    private static final StringValue SERVER_LEAVE =
            Configurable.create("server.leave", ":heavy_minus_sign: **%player%** left the server.");

    private static final StringValue SERVER_SWITCH =
            Configurable.create("server.switch", ":heavy_plus_sign: **%player%** moved to **%server%**");

    private static final StringValue PLAYER_LIST_TOP =
            Configurable.create("server.player-list.top", "**===== Player List (%count%) =====**");

    private static final StringValue PLAYER_LIST_FORMAT =
            Configurable.create("server.player-list.list", "%server%: %players%");

    private static final StringValue COMMAND_NO_PERMISSION =
            Configurable.create("command.no-permission", "&c* You have no permission: %perm%");

    private static final StringValue COMMAND_RELOAD_START =
            Configurable.create("command.reload.start", "&7* Reloading DiscordChatBridge...");

    private static final StringValue COMMAND_RELOAD_SUCCESS =
            Configurable.create("command.reload.success", "&7* The reload was successful.");

    private static final StringValue COMMAND_RELOAD_FAILURE =
            Configurable.create("command.reload.failure", "&c* Failed to reload. Please check the console.");


    private final Yaml yaml;

    public FormatConfig(@NotNull DiscordChatBridge plugin) throws IOException {
        this.yaml = BungeeYamlFactory.load(plugin, "format.yml");
    }

    public void reload() throws IOException {
        yaml.reload();
    }

    public @NotNull String getDiscordChatFormat() {
        return yaml.get(SERVER_CHAT);
    }

    public @NotNull String getServerJoinFormat() {
        return yaml.get(SERVER_JOIN);
    }

    public @NotNull String getServerLeftFormat() {
        return yaml.get(SERVER_LEAVE);
    }

    public @NotNull String getServerSwitchFormat() {
        return yaml.get(SERVER_SWITCH);
    }

    public @NotNull String getPlayerListTop() {
        return yaml.get(PLAYER_LIST_TOP);
    }

    public @NotNull String getPlayerListFormat() {
        return yaml.get(PLAYER_LIST_FORMAT);
    }

    public @NotNull String getNoPermissionMessage() {
        return yaml.get(COMMAND_NO_PERMISSION);
    }

    public @NotNull String getReloadingMessage() {
        return yaml.get(COMMAND_RELOAD_START);
    }

    public @NotNull String getReloadSuccessMessage() {
        return yaml.get(COMMAND_RELOAD_SUCCESS);
    }

    public @NotNull String getReloadFailureMessage() {
        return yaml.get(COMMAND_RELOAD_FAILURE);
    }
}
