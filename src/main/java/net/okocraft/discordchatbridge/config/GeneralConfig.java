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
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.md_5.bungee.api.ProxyServer;
import net.okocraft.discordchatbridge.DiscordChatBridge;
import net.okocraft.discordchatbridge.data.LinkedChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class GeneralConfig {

    private final FileConfiguration file;
    private List<LinkedChannel> linkedChannels = Collections.emptyList();

    public GeneralConfig(@NotNull DiscordChatBridge plugin) throws IOException {
        var path = plugin.getDataFolder().toPath().resolve("config.yml");

        if (!Files.exists(path)) {
            saveDefault(plugin, path);
        }

        this.file = YamlConfiguration.create(path);
        file.load();
        loadChannels();
    }

    public void reload() throws IOException {
        file.reload();
        loadChannels();
    }

    public @NotNull String getToken() {
        return file.getString("discord.token", "");
    }

    public @NotNull OnlineStatus getStatus() {
        try {
            var value = file.getString("discord.status");
            return OnlineStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            return OnlineStatus.ONLINE;
        }
    }

    public @NotNull Activity createActivity() {
        Activity.ActivityType type;

        try {
            var value = file.getString("discord.activity.type");
            type = Activity.ActivityType.valueOf(value);
        } catch (IllegalArgumentException e) {
            type = Activity.ActivityType.DEFAULT;
        }

        var game = file.getString("discord.activity.game", "%count% players in server");
        var url = file.getString("discord.activity.url", "");

        var playerCount = ProxyServer.getInstance().getOnlineCount();

        return Activity.of(type, game.replace("%count%", String.valueOf(playerCount)), url);
    }

    public @NotNull Optional<Long> getDiscordChannel(@NotNull String channelName) {
        return linkedChannels.stream()
                .filter(c -> c.getChannelName().equals(channelName))
                .map(LinkedChannel::getId)
                .findFirst();
    }

    public @NotNull Optional<String> getLunaChatChannel(long id) {
        return linkedChannels.stream()
                .filter(c -> c.getId() == id)
                .map(LinkedChannel::getChannelName)
                .findFirst();
    }

    public @Nullable LinkedChannel getSystemChannel() {
        if (linkedChannels.isEmpty()) {
            return null;
        } else {
            return linkedChannels.get(0);
        }
    }

    public int getChatMaxLength() {
        return file.getInteger("chat-max-length", 150);
    }

    public @NotNull String getSourceName() {
        return file.getString("discord-source-name", "Dis");
    }

    public @NotNull String getRolePrefix(long roleId) {
        return file.getString("role-prefix." + roleId);
    }

    public @NotNull String getDefaultPrefix() {
        return file.getString("role-prefix.default", "&f* ");
    }

    private void loadChannels() {
        var section = file.getSection("channels");

        if (section != null) {
            linkedChannels = new LinkedList<>();

            for (String key : section.getKeys()) {
                linkedChannels.add(new LinkedChannel(key, section.getLong(key, 0)));
            }
        }
    }

    private void saveDefault(@NotNull DiscordChatBridge plugin, @NotNull Path path) throws IOException {
        try (var def = plugin.getResourceAsStream("config.yml")) {
            Files.copy(def, path);
        }
    }
}
