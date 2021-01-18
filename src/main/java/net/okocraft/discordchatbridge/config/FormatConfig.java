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
import com.github.siroshun09.configapi.common.yaml.Yaml;
import net.okocraft.discordchatbridge.DiscordChatBridge;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class FormatConfig {

    private final Yaml yaml;

    public FormatConfig(@NotNull DiscordChatBridge plugin) throws IOException {
        this.yaml = BungeeYamlFactory.load(plugin, "format.yml");
    }

    public void reload() throws IOException {
        yaml.reload();
    }

    @NotNull
    public String getDiscordChatFormat() {
        return yaml.getString("server.chat", "%player%: %message%");
    }

    @NotNull
    public String getServerJoinFormat() {
        return yaml.getString("server.join", ":heavy_plus_sign: **%player%** joined the server.");
    }

    @NotNull
    public String getServerLeftFormat() {
        return yaml.getString("server.leave", ":heavy_minus_sign: **%player%** left the server.");
    }

    @NotNull
    public String getServerSwitchFormat() {
        return yaml.getString("server.switch", ":heavy_plus_sign: **%player%** moved to **%server%**");
    }

    @NotNull
    public String getPlayerListTop() {
        return yaml.getString("server.player-list.top", "**===== Player List (%count%) =====**");
    }

    @NotNull
    public String getPlayerListFormat() {
        return yaml.getString("server.player-list.list", "%server%: %players%");
    }

    @NotNull
    public String getNoPermissionMessage() {
        return yaml.getString("command.no-permission", "&c* You have no permission: %perm%");
    }

    @NotNull
    public String getReloadingMessage() {
        return yaml.getString("command.reload.start", "&7* Reloading DiscordChatBridge...");
    }

    @NotNull
    public String getReloadSuccessMessage() {
        return yaml.getString("command.reload.success", "&7* The reload was successful.");
    }

    @NotNull
    public String getReloadFailureMessage() {
        return yaml.getString("command.reload.failure", "&c* Failed to reload. Please check the console.");
    }
}
