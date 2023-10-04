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

package net.okocraft.discordchatbridge.listener;

import com.github.siroshun09.configapi.api.Configuration;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.config.FormatSettings;
import net.okocraft.discordchatbridge.config.GeneralSettings;
import net.okocraft.discordchatbridge.constant.Constants;
import net.okocraft.discordchatbridge.constant.Placeholders;
import net.okocraft.discordchatbridge.util.ColorStripper;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class DiscordListener extends ListenerAdapter {

    private final DiscordChatBridgePlugin plugin;
    private final Map<Long, String> linkedChannels = new HashMap<>();

    private final Configuration rolePrefixes;
    private final String defaultRoleColorCode;

    private final AtomicLong lastPlayerListUsed = new AtomicLong(0);

    public DiscordListener(@NotNull DiscordChatBridgePlugin plugin) {
        this.plugin = plugin;

        var channelSection = plugin.getGeneralConfig().get(GeneralSettings.LINKED_CHANNELS);

        for (var key : channelSection.getKeyList()) {
            var id = channelSection.getLong(key);
            if (id != 0) {
                this.linkedChannels.put(id, key);
            }
        }

        this.rolePrefixes = plugin.getGeneralConfig().get(GeneralSettings.ROLE_PREFIX);
        this.defaultRoleColorCode = plugin.serializeColor(new Color(Role.DEFAULT_COLOR_RAW));
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        var member = event.getMember();

        if (event.getAuthor().isBot() || member == null) {
            return;
        }

        var message = event.getMessage().getContentDisplay();

        if (message.startsWith("!playerlist")) {
            this.onPlayerListCommand(event.getChannel());
            return;
        }

        var channelName = this.linkedChannels.get(event.getChannel().getIdLong());

        if (channelName == null) {
            return;
        }

        var config = this.plugin.getGeneralConfig();

        int maxLength = config.get(GeneralSettings.CHAT_MAX_LENGTH);

        if (0 < maxLength && maxLength < message.length()) {
            this.plugin.getBot().addReaction(event.getMessage(), "U+26A0");
            return;
        }

        var lines = message.lines().toList();
        int maxLines = config.get(GeneralSettings.CHAT_MAX_LINES);

        var attachments = event.getMessage().getAttachments();
        int maxAttachments = config.get(GeneralSettings.CHAT_MAX_ATTACHMENTS);

        if ((0 < maxLines && maxLines < lines.size()) ||
            (0 < maxAttachments && maxAttachments < attachments.size())) {
            this.plugin.getBot().addReaction(event.getMessage(), "U+26A0");
            return;
        }

        var senderName = this.createSenderName(member);
        var sourceName = config.get(GeneralSettings.DISCORD_SOURCE_NAME);

        for (var line : lines) {
            if (!line.isEmpty()) {
                this.plugin.getChatSystem().sendChat(channelName, senderName, sourceName, line);
            }
        }

        for (var attachment : attachments) {
            this.plugin.getChatSystem().sendChat(channelName, senderName, sourceName, attachment.getUrl());
        }
    }

    private void onPlayerListCommand(@NotNull MessageChannelUnion channel) {
        if (System.currentTimeMillis() - this.lastPlayerListUsed.get() < 5000) {
            return;
        }

        this.plugin.getBot().updateGame();

        var builder = new StringBuilder();

        var top =
                this.plugin.getFormatConfig()
                        .get(FormatSettings.PLAYER_LIST_TOP)
                        .replace(
                                Placeholders.PLAYER_COUNT,
                                String.valueOf(this.plugin.getPlatformInfo().getNumberOfPlayers())
                        );

        builder.append(top).append(Constants.LINE_SEPARATOR).append("```").append(Constants.LINE_SEPARATOR);

        this.plugin.getPlatformInfo()
                .getPlayerListsPerServer()
                .forEach(list -> {
                    builder.append(list);
                    builder.append(Constants.LINE_SEPARATOR);
                });

        builder.append(Constants.LINE_SEPARATOR).append("```");

        if (channel.canTalk()) {
            this.plugin.getBot().sendMessage(channel, builder.toString());
            this.lastPlayerListUsed.set(System.currentTimeMillis());
        }
    }

    private @NotNull String createSenderName(@NotNull Member member) {
        var rolePrefix = this.getRolePrefix(member);
        var originalName = member.getNickname() != null ? member.getNickname() : member.getEffectiveName();

        // true / false / role name
        var setting = this.plugin.getGeneralConfig().get(GeneralSettings.ALLOW_COLORS_IN_NAME);

        boolean allowColorsInName = Boolean.parseBoolean(setting) || (!setting.equalsIgnoreCase("false") && this.hasRole(member, setting));

        String senderName;

        if (allowColorsInName) {
            senderName = checkNameLength(originalName);
        } else {
            senderName = checkNameLength(ColorStripper.strip(originalName));
        }

        return rolePrefix + senderName;
    }

    private @NotNull String getRolePrefix(@NotNull Member member) {
        if (!this.plugin.getGeneralConfig().get(GeneralSettings.ENABLE_ROLE_PREFIX)) {
            return "";
        }

        var role = this.plugin.getBot().getFirstRole(member);
        String prefix;

        if (role != null) {
            var roleColorCode = Optional.ofNullable(role.getColor()).map(this.plugin::serializeColor).orElse(this.defaultRoleColorCode);
            var rolePrefix = this.rolePrefixes.getString(Long.toString(role.getIdLong()));

            if (rolePrefix.isEmpty()) {
                prefix = this.plugin.getGeneralConfig()
                        .get(GeneralSettings.DEFAULT_ROLE_PREFIX)
                        .replace(Placeholders.ROLE_COLOR, roleColorCode);
            } else {
                prefix = rolePrefix.replace(Placeholders.ROLE_COLOR, roleColorCode);
            }
        } else {
            prefix = "";
        }

        return prefix;
    }

    private boolean hasRole(@NotNull Member member, @NotNull String roleName) {
        return member.getRoles().stream().anyMatch(role -> role.getName().equals(roleName));
    }

    private @NotNull String checkNameLength(@NotNull String name) {
        int limit = plugin.getGeneralConfig().get(GeneralSettings.NICKNAME_LENGTH_LIMIT);
        int[] codePoints = name.codePoints().toArray();

        if (codePoints.length <= (limit >> 1)) {
            return name;
        }

        boolean color = false;
        int length = 0;
        var builder = new StringBuilder(name.length());

        for (int i = 0; i < codePoints.length && length < limit; i++) {
            int codePoint = codePoints[i];

            if (color) {
                color = false;

                if (ColorStripper.isLegacyColorCode(codePoint)) {
                    builder.appendCodePoint(codePoints[i - 1]);
                    builder.appendCodePoint(codePoint);
                } else {
                    builder.appendCodePoint(codePoints[i - 1]);

                    if (limit <= ++length) {
                        break;
                    }

                    if (is1ByteChar(codePoint)) {
                        builder.appendCodePoint(codePoint);
                        length++;
                    } else {
                        length += 2;

                        if (length <= limit) {
                            builder.appendCodePoint(codePoint);
                        }
                    }
                }
            } else {
                if (ColorStripper.isAmpersandOrSection(codePoint)) {
                    color = true;
                } else if (is1ByteChar(codePoint)) {
                    builder.appendCodePoint(codePoint);
                    length++;
                } else {
                    length += 2;

                    if (length <= limit) {
                        builder.appendCodePoint(codePoint);
                    }
                }
            }
        }

        return builder.toString();
    }

    private boolean is1ByteChar(int codePoint) {
        return codePoint <= 126;
    }
}
