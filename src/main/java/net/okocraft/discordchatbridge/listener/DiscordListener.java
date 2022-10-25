/*
 *     Copyright (c) 2022 Okocraft
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
import java.util.stream.Collectors;

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
                linkedChannels.put(id, key);
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
            onPlayerListCommand(event.getChannel());
            return;
        }

        var channelName = linkedChannels.get(event.getChannel().getIdLong());

        if (channelName == null) {
            return;
        }

        var config = plugin.getGeneralConfig();

        int maxLength = config.get(GeneralSettings.CHAT_MAX_LENGTH);

        if (0 < maxLength && maxLength < message.length()) {
            plugin.getBot().addReaction(event.getMessage(), "U+26A0");
            return;
        }

        var lines = message.lines().collect(Collectors.toList());
        int maxLines = config.get(GeneralSettings.CHAT_MAX_LINES);

        if (0 < maxLines && maxLines < lines.size()) {
            plugin.getBot().addReaction(event.getMessage(), "U+26A0");
            return;
        }

        var senderName = createSenderName(member);
        var sourceName = config.get(GeneralSettings.DISCORD_SOURCE_NAME);

        for (var line : lines) {
            if (!line.isEmpty()) {
                plugin.getChatSystem().sendChat(channelName, senderName, sourceName, line);
            }
        }

        var attachments = event.getMessage().getAttachments();

        if (!attachments.isEmpty()) {
            for (var attachment : attachments) {
                plugin.getChatSystem().sendChat(channelName, senderName, sourceName, attachment.getUrl());
            }
        }
    }

    private void onPlayerListCommand(@NotNull MessageChannelUnion channel) {
        if (System.currentTimeMillis() - lastPlayerListUsed.get() < 5000) {
            return;
        }

        plugin.getBot().updateGame();

        var builder = new StringBuilder();

        var top =
                plugin.getFormatConfig()
                        .get(FormatSettings.PLAYER_LIST_TOP)
                        .replace(
                                Placeholders.PLAYER_COUNT,
                                String.valueOf(plugin.getPlatformInfo().getNumberOfPlayers())
                        );

        builder.append(top).append(Constants.LINE_SEPARATOR).append("```").append(Constants.LINE_SEPARATOR);

        plugin.getPlatformInfo()
                .getPlayerListsPerServer()
                .forEach(list -> {
                    builder.append(list);
                    builder.append(Constants.LINE_SEPARATOR);
                });

        builder.append(Constants.LINE_SEPARATOR).append("```");

        if (channel.canTalk()) {
            plugin.getBot().sendMessage(channel, builder.toString());
            lastPlayerListUsed.set(System.currentTimeMillis());
        }
    }

    private @NotNull String createSenderName(@NotNull Member member) {
        var rolePrefix = getRolePrefix(member);
        var originalName = member.getNickname() != null ? member.getNickname() : member.getEffectiveName();

        // true / false / role name
        var setting = plugin.getGeneralConfig().get(GeneralSettings.ALLOW_COLORS_IN_NAME);

        boolean allowColorsInName = Boolean.parseBoolean(setting) || (!setting.equalsIgnoreCase("false") && hasRole(member, setting));

        String senderName;

        if (allowColorsInName) {
            senderName = originalName;
        } else {
            senderName = ColorStripper.strip(originalName);
        }

        return rolePrefix + senderName;
    }

    private @NotNull String getRolePrefix(@NotNull Member member) {
        if (!plugin.getGeneralConfig().get(GeneralSettings.ENABLE_ROLE_PREFIX)) {
            return "";
        }

        var role = plugin.getBot().getFirstRole(member);
        String prefix;

        if (role != null) {
            var roleColorCode = Optional.ofNullable(role.getColor()).map(plugin::serializeColor).orElse(defaultRoleColorCode);
            var rolePrefix = rolePrefixes.getString(Long.toString(role.getIdLong()));

            if (rolePrefix.isEmpty()) {
                prefix = plugin.getGeneralConfig()
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
}
