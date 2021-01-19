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

import com.github.siroshun09.configapi.bungee.BungeeYaml;
import com.github.siroshun09.configapi.bungee.BungeeYamlFactory;
import com.github.siroshun09.configapi.common.Configuration;
import com.github.siroshun09.configapi.common.configurable.AbstractConfigurableValue;
import com.github.siroshun09.configapi.common.configurable.Configurable;
import com.github.siroshun09.configapi.common.configurable.IntegerValue;
import com.github.siroshun09.configapi.common.configurable.StringValue;
import com.github.siroshun09.configapi.common.yaml.Yaml;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.okocraft.discordchatbridge.DiscordChatBridge;
import net.okocraft.discordchatbridge.data.LinkedChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class GeneralConfig {

    private static final StringValue DISCORD_TOKEN = Configurable.create("discord.token", "");

    private static final Configurable<OnlineStatus> DISCORD_STATUS =
            new AbstractConfigurableValue<>("discord.status", OnlineStatus.ONLINE) {
                @Override
                public @Nullable OnlineStatus getValueOrNull(@NotNull Configuration configuration) {
                    try {
                        var value = configuration.getString(getKey());
                        return OnlineStatus.valueOf(value);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                }
            };

    private static final Configurable<Activity.ActivityType> DISCORD_ACTIVITY_TYPE =
            new AbstractConfigurableValue<>("discord.activity.type", Activity.ActivityType.DEFAULT) {
                @Override
                public @Nullable Activity.ActivityType getValueOrNull(@NotNull Configuration configuration) {
                    try {
                        var value = configuration.getString(getKey());
                        return Activity.ActivityType.valueOf(value);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                }
            };

    private static final StringValue DISCORD_ACTIVITY_GAME =
            Configurable.create("discord.activity.game", "%count% players in server");

    private static final StringValue DISCORD_ACTIVITY_URL = Configurable.create("discord.activity.", "");

    private static final IntegerValue CHAT_MAX_LENGTH = Configurable.create("chat-max-length", 150);

    private static final StringValue DISCORD_SOURCE_NAME = Configurable.create("discord-source-name", "Dis");

    private static final StringValue DEFAULT_PREFIX = Configurable.create("role-prefix.default", "&f* ");

    private final DiscordChatBridge plugin;
    private final Yaml yaml;
    private List<LinkedChannel> linkedChannels = Collections.emptyList();

    public GeneralConfig(@NotNull DiscordChatBridge plugin) throws IOException {
        this.plugin = plugin;
        this.yaml = BungeeYamlFactory.load(plugin, "config.yml");

        loadChannels();
    }

    public void reload() throws IOException {
        yaml.reload();
        loadChannels();
    }

    public @NotNull String getToken() {
        return yaml.get(DISCORD_TOKEN);
    }

    public @NotNull OnlineStatus getStatus() {
        return yaml.get(DISCORD_STATUS);
    }

    public @NotNull Activity createActivity() {
        var type = yaml.get(DISCORD_ACTIVITY_TYPE);
        var game = yaml.get(DISCORD_ACTIVITY_GAME);
        var url = yaml.get(DISCORD_ACTIVITY_URL);

        var playerCount = plugin.getProxy().getOnlineCount();

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
        return yaml.get(CHAT_MAX_LENGTH);
    }

    public @NotNull String getSourceName() {
        return yaml.get(DISCORD_SOURCE_NAME);
    }

    public @NotNull String getRolePrefix(long roleId) {
        return yaml.getString("role-prefix." + roleId);
    }

    public @NotNull String getDefaultPrefix() {
        return yaml.get(DEFAULT_PREFIX);
    }

    private void loadChannels() {
        var bungeeYaml = (BungeeYaml) yaml;
        var section = bungeeYaml.getConfig().getSection("channels");

        if (section != null) {
            linkedChannels = new LinkedList<>();

            for (String key : section.getKeys()) {
                linkedChannels.add(new LinkedChannel(key, section.getLong(key, 0)));
            }
        }
    }
}
