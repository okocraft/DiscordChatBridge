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

import com.github.siroshun09.configapi.api.Configuration;
import com.github.siroshun09.configapi.api.MappedConfiguration;
import com.github.siroshun09.configapi.api.value.ConfigValue;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import java.util.Objects;

public final class GeneralSettings {

    public static final ConfigValue<String> DISCORD_TOKEN = config -> config.getString("discord.token");

    public static final ConfigValue<OnlineStatus> DISCORD_STATUS =
            config -> {
                try {
                    var value = config.getString("discord.status");
                    return OnlineStatus.valueOf(value);
                } catch (IllegalArgumentException e) {
                    return OnlineStatus.ONLINE;
                }
            };

    public static final ConfigValue<Activity.ActivityType> DISCORD_ACTIVITY_TYPE =
            config -> {
                try {
                    var value = config.getString("discord.activity.type");
                    return Activity.ActivityType.valueOf(value);
                } catch (IllegalArgumentException e) {
                    return Activity.ActivityType.DEFAULT;
                }
            };

    public static final ConfigValue<String> DISCORD_ACTIVITY_GAME =
            config -> config.getString("discord.activity.game");

    public static final ConfigValue<String> DISCORD_ACTIVITY_URL =
            config -> config.getString("discord.activity.url");

    public static final ConfigValue<String> SERVER_NAME =
            config -> config.getString("server-name", "server");

    public static final ConfigValue<Integer> CHAT_MAX_LENGTH =
            config -> config.getInteger("chat-max-length", 150);

    public static final ConfigValue<String> DISCORD_SOURCE_NAME =
            config -> config.getString("discord-source-name", "Dis");

    public static final ConfigValue<Configuration> ROLE_PREFIX =
            config -> Objects.requireNonNullElseGet(config.getSection("role-prefix"), MappedConfiguration::create);

    public static final ConfigValue<String> DEFAULT_ROLE_PREFIX =
            config -> config.getString("role-prefix.default", "&f*");

    public static final ConfigValue<Long> SYSTEM_CHANNEL =
            config -> config.getLong("system-channel");

    public static final ConfigValue<Configuration> LINKED_CHANNELS =
            config -> Objects.requireNonNullElseGet(config.getSection("channels"), MappedConfiguration::create);

    private GeneralSettings() {
        throw new UnsupportedOperationException();
    }
}
