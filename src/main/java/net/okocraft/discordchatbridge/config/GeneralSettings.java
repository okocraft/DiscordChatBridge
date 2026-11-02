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

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.Map;

@ConfigSerializable
public final class GeneralSettings {

    public Discord discord = new Discord();
    public String serverName = "server";
    public int chatMaxLength = 150;
    public int chatMaxLines;
    public int chatMaxAttachments;
    public String discordSourceName = "Dis";
    public long systemChannel;
    public Map<String, Long> channels = Map.of();
    public Map<String, String> rolePrefix = Map.of();
    public String allowColorsInName = "true";
    public int nicknameLengthLimit = 16;
    public MessageSending messageSendingSetting = new MessageSending();

    public OnlineStatus discordStatus() {
        try {
            return OnlineStatus.valueOf(this.discord.status);
        } catch (IllegalArgumentException e) {
            return OnlineStatus.ONLINE;
        }
    }

    public Activity.ActivityType discordActivityType() {
        var type = this.discord.activity.type;
        if ("DEFAULT".equalsIgnoreCase(type)) {
            return Activity.ActivityType.PLAYING;
        }
        try {
            return Activity.ActivityType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return Activity.ActivityType.PLAYING;
        }
    }

    public boolean enableRolePrefix() {
        return Boolean.parseBoolean(this.rolePrefix.getOrDefault("enable", "true"));
    }

    public String defaultRolePrefix() {
        return this.rolePrefix.getOrDefault("default", "&f*");
    }

    void validate() {
        if (this.discord == null || this.discord.activity == null ||
                this.discord.token == null || this.discord.token.isBlank()) {
            throw new IllegalArgumentException("discord.token must not be blank");
        }
        if (this.discord.status == null || this.discord.activity.type == null ||
                this.discord.activity.game == null || this.discord.activity.url == null) {
            throw new IllegalArgumentException("discord.status and discord.activity fields must be strings");
        }
        if (this.channels == null || this.rolePrefix == null || this.messageSendingSetting == null) {
            throw new IllegalArgumentException("channels, role-prefix and message-sending-setting must be mappings");
        }
        if (this.chatMaxLength < 0 || this.chatMaxLines < 0 || this.chatMaxAttachments < 0 ||
                this.nicknameLengthLimit <= 0) {
            throw new IllegalArgumentException("Chat limits must be non-negative and nickname-length-limit positive");
        }
        this.channels = Map.copyOf(this.channels);
        this.rolePrefix = Map.copyOf(this.rolePrefix);
    }

    @ConfigSerializable
    public static final class Discord {
        public String token = "";
        public String status = "ONLINE";
        public ActivitySettings activity = new ActivitySettings();
    }

    @ConfigSerializable
    public static final class ActivitySettings {
        public String type = "PLAYING";
        public String game = "%player_count% players in server";
        public String url = "";
    }

    @ConfigSerializable
    public static final class MessageSending {
        public boolean join = true;
        public boolean leave = true;

        @Setting("switch")
        public boolean switchMessage = true;

        public boolean firstJoin = true;
    }
}
