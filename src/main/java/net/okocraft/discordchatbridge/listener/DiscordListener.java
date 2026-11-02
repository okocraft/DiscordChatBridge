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

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.constant.Placeholders;
import net.okocraft.discordchatbridge.util.ColorStripper;
import net.okocraft.discordchatbridge.util.PlayerListFormatter;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class DiscordListener extends ListenerAdapter {

    private final DiscordChatBridgePlugin plugin;
    private final Map<Long, String> linkedChannels = new HashMap<>();

    private final Map<String, String> rolePrefixes;
    private final String defaultRoleColorCode;

    private final Clock clock;
    private final AtomicLong nextPlayerListUse = new AtomicLong(Long.MIN_VALUE);

    public DiscordListener(@NotNull DiscordChatBridgePlugin plugin) {
        this(plugin, Clock.systemUTC());
    }

    DiscordListener(@NotNull DiscordChatBridgePlugin plugin, @NotNull Clock clock) {
        this.plugin = plugin;
        this.clock = clock;

        var channelSection = plugin.getGeneralConfig().channels;
        for (var entry : channelSection.entrySet()) {
            if (entry.getValue() != 0) {
                this.linkedChannels.put(entry.getValue(), entry.getKey());
            }
        }

        this.rolePrefixes = plugin.getGeneralConfig().rolePrefix;
        this.defaultRoleColorCode = plugin.serializeColor(new Color(Role.DEFAULT_COLOR_RAW));
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        var member = event.getMember();

        if (!event.isFromGuild() || event.getAuthor().isBot() || member == null) {
            return;
        }

        var channelId = event.getChannel().getIdLong();
        var channelName = this.linkedChannels.get(channelId);
        var systemChannel = this.plugin.getGeneralConfig().systemChannel;

        if (channelName == null && (systemChannel == 0 || channelId != systemChannel)) {
            return;
        }

        var message = event.getMessage().getContentDisplay();

        if (message.startsWith("!playerlist")) {
            this.onPlayerListCommand(event.getChannel());
            return;
        }

        if (channelName == null) {
            return;
        }

        var config = this.plugin.getGeneralConfig();

        int maxLength = config.chatMaxLength;

        if (0 < maxLength && maxLength < message.length()) {
            this.plugin.getBot().addReaction(event.getMessage(), "U+26A0");
            return;
        }

        var lines = message.lines().toList();
        int maxLines = config.chatMaxLines;

        var attachments = event.getMessage().getAttachments();
        int maxAttachments = config.chatMaxAttachments;

        if ((0 < maxLines && maxLines < lines.size()) ||
            (0 < maxAttachments && maxAttachments < attachments.size())) {
            this.plugin.getBot().addReaction(event.getMessage(), "U+26A0");
            return;
        }

        var senderName = this.createSenderName(member);
        var sourceName = config.discordSourceName;

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
        if (!channel.canTalk()) {
            return;
        }

        var nextUse = this.nextPlayerListUse.get();
        var now = this.clock.millis();
        if (now < nextUse || !this.nextPlayerListUse.compareAndSet(nextUse, now + 5000)) {
            return;
        }

        this.plugin.getBot().updateGame();

        var top =
                this.plugin.getFormatConfig()
                        .server.playerList.top
                        .replace(
                                Placeholders.PLAYER_COUNT,
                                String.valueOf(this.plugin.getPlatformInfo().getNumberOfPlayers())
                        );

        this.plugin.getBot().sendMessage(channel, PlayerListFormatter.formatMessage(
                top, this.plugin.getPlatformInfo().getPlayerListsPerServer()));
    }

    private @NotNull String createSenderName(@NotNull Member member) {
        var rolePrefix = this.getRolePrefix(member);
        var originalName = member.getNickname() != null ? member.getNickname() : member.getEffectiveName();

        // true / false / role name
        var setting = this.plugin.getGeneralConfig().allowColorsInName;

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
        if (!this.plugin.getGeneralConfig().enableRolePrefix()) {
            return "";
        }

        var role = this.plugin.getBot().getFirstRole(member);
        String prefix;

        if (role != null) {
            var roleColorCode = Optional.ofNullable(role.getColors().getPrimary()).map(this.plugin::serializeColor).orElse(this.defaultRoleColorCode);
            var rolePrefix = this.rolePrefixes.getOrDefault(Long.toString(role.getIdLong()), "");

            if (rolePrefix.isEmpty()) {
                prefix = this.plugin.getGeneralConfig()
                        .defaultRolePrefix()
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
        int limit = plugin.getGeneralConfig().nicknameLengthLimit;
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
