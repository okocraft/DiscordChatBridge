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

    private DiscordSettings discord = new DiscordSettings();

    @Setting("server-name")
    private String serverName = "server";

    @Setting("chat-max-length")
    private int chatMaxLength = 150;

    @Setting("chat-max-lines")
    private int chatMaxLines;

    @Setting("chat-max-attachments")
    private int chatMaxAttachments;

    @Setting("discord-source-name")
    private String discordSourceName = "Dis";

    @Setting("system-channel")
    private long systemChannel;

    private Map<String, Long> channels = Map.of();

    @Setting("role-prefix")
    private Map<String, String> rolePrefix = Map.of();

    @Setting("allow-colors-in-name")
    private String allowColorsInName = "true";

    @Setting("nickname-length-limit")
    private int nicknameLengthLimit = 16;

    @Setting("message-sending-setting")
    private MessageSendingSettings messageSendingSetting = new MessageSendingSettings();

    public String discordToken() {
        return this.discord.token;
    }

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

    public String discordActivityGame() {
        return this.discord.activity.game;
    }

    public String discordActivityUrl() {
        return this.discord.activity.url;
    }

    public String serverName() {
        return this.serverName;
    }

    public int chatMaxLength() {
        return this.chatMaxLength;
    }

    public int chatMaxLines() {
        return this.chatMaxLines;
    }

    public int chatMaxAttachments() {
        return this.chatMaxAttachments;
    }

    public String discordSourceName() {
        return this.discordSourceName;
    }

    public long systemChannel() {
        return this.systemChannel;
    }

    public Map<String, Long> linkedChannels() {
        return this.channels;
    }

    public Map<String, String> rolePrefixes() {
        return this.rolePrefix;
    }

    public boolean enableRolePrefix() {
        return Boolean.parseBoolean(this.rolePrefix.getOrDefault("enable", "true"));
    }

    public String defaultRolePrefix() {
        return this.rolePrefix.getOrDefault("default", "&f*");
    }

    public String allowColorsInName() {
        return this.allowColorsInName;
    }

    public int nicknameLengthLimit() {
        return this.nicknameLengthLimit;
    }

    public boolean sendJoinMessage() {
        return this.messageSendingSetting.join;
    }

    public boolean sendLeaveMessage() {
        return this.messageSendingSetting.leave;
    }

    public boolean sendSwitchMessage() {
        return this.messageSendingSetting.switch;
    }

    public boolean sendFirstJoinMessage() {
        return this.messageSendingSetting.firstJoin;
    }

    void validate() {
        if (this.discord == null || this.discord.token == null || this.discord.token.isBlank()) {
            throw new IllegalArgumentException("discord.token must not be blank");
        }
        if (this.discord.status == null || this.discord.activity == null ||
                this.discord.activity.type == null || this.discord.activity.game == null ||
                this.discord.activity.url == null) {
            throw new IllegalArgumentException("discord.status and discord.activity fields are required");
        }
        if (this.serverName == null || this.serverName.isBlank() || this.discordSourceName == null ||
                this.allowColorsInName == null || this.messageSendingSetting == null) {
            throw new IllegalArgumentException("Missing required general settings");
        }
        if (this.chatMaxLength < 0 || this.chatMaxLines < 0 || this.chatMaxAttachments < 0) {
            throw new IllegalArgumentException("chat-max-* values must be zero or greater");
        }
        if (this.nicknameLengthLimit <= 0) {
            throw new IllegalArgumentException("nickname-length-limit must be greater than zero");
        }
        if (this.systemChannel < 0) {
            throw new IllegalArgumentException("system-channel must be zero or greater");
        }
        if (this.channels == null || this.channels.entrySet().stream().anyMatch(
                entry -> entry.getKey() == null || entry.getKey().isBlank() ||
                        entry.getValue() == null || entry.getValue() < 0)) {
            throw new IllegalArgumentException("channels must map names to non-negative channel IDs");
        }
        if (this.rolePrefix == null || this.rolePrefix.values().stream().anyMatch(value -> value == null)) {
            throw new IllegalArgumentException("role-prefix must contain string values");
        }
        var enabled = this.rolePrefix.getOrDefault("enable", "true");
        if (!enabled.equalsIgnoreCase("true") && !enabled.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException("role-prefix.enable must be true or false");
        }
        this.channels = Map.copyOf(this.channels);
        this.rolePrefix = Map.copyOf(this.rolePrefix);
    }

    @ConfigSerializable
    public static final class DiscordSettings {
        private String token = "";
        private String status = "ONLINE";
        private ActivitySettings activity = new ActivitySettings();
    }

    @ConfigSerializable
    public static final class ActivitySettings {
        private String type = "PLAYING";
        private String game = "%player_count% players in server";
        private String url = "";
    }

    @ConfigSerializable
    public static final class MessageSendingSettings {
        private boolean join = true;
        private boolean leave = true;
        private boolean switch = true;

        @Setting("first-join")
        private boolean firstJoin = true;
    }
}
