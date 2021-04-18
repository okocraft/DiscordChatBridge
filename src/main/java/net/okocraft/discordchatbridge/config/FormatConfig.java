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

import com.github.siroshun09.configapi.common.FileConfiguration;
import com.github.siroshun09.configapi.common.util.ResourceUtils;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.okocraft.discordchatbridge.DiscordChatBridge;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class FormatConfig {

    private final FileConfiguration file;

    public FormatConfig(@NotNull DiscordChatBridge plugin) throws IOException {
        var path = plugin.getDataFolder().toPath().resolve("format.yml");

        ResourceUtils.copyFromClassLoaderIfNotExists(
                plugin.getClass().getClassLoader(),
                "format.yml",
                path
        );

        this.file = YamlConfiguration.create(path);
        file.load();
    }

    public void reload() throws IOException {
        file.reload();
    }

    public @NotNull String getDiscordChatFormat() {
        return file.getString("server.chat", "%player%: %message%");
    }

    public @NotNull String getServerJoinFormat() {
        return file.getString("server.join", ":heavy_plus_sign: **%player%** joined the server.");
    }

    public @NotNull String getServerLeftFormat() {
        return file.getString("server.leave", ":heavy_minus_sign: **%player%** left the server.");
    }

    public @NotNull String getServerSwitchFormat() {
        return file.getString("server.switch", ":heavy_plus_sign: **%player%** moved to **%server%**");
    }

    public @NotNull String getPlayerListTop() {
        return file.getString("server.player-list.top", "**===== Player List (%count%) =====**");
    }

    public @NotNull String getPlayerListFormat() {
        return file.getString("server.player-list.list", "%server%: %players%");
    }

    public @NotNull String getNoPermissionMessage() {
        return file.getString("command.no-permission", "&c* You have no permission: %perm%");
    }

    public @NotNull String getReloadingMessage() {
        return file.getString("command.reload.start", "&7* Reloading DiscordChatBridge...");
    }

    public @NotNull String getReloadSuccessMessage() {
        return file.getString("command.reload.success", "&7* The reload was successful.");
    }

    public @NotNull String getReloadFailureMessage() {
        return file.getString("command.reload.failure", "&c* Failed to reload. Please check the console.");
    }
}
