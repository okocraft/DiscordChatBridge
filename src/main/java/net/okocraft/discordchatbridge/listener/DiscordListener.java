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

package net.okocraft.discordchatbridge.listener;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.config.FormatSettings;
import net.okocraft.discordchatbridge.config.GeneralSettings;
import net.okocraft.discordchatbridge.constant.Constants;
import net.okocraft.discordchatbridge.constant.Placeholders;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class DiscordListener extends ListenerAdapter {

    private final DiscordChatBridgePlugin plugin;
    private final Map<Long, String> linkedChannels = new HashMap<>();

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
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        var member = event.getMember();

        if (event.getAuthor().isBot() || member == null) {
            return;
        }

        var message = event.getMessage().getContentStripped();

        if (message.startsWith("!playerlist")) {
            onPlayerListCommand(event.getTextChannel());
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

        var name = member.getNickname() != null ? member.getNickname() : member.getEffectiveName();
        var senderName = plugin.getBot().getRolePrefix(member) + name;
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

    private void onPlayerListCommand(@NotNull TextChannel channel) {
        if (System.currentTimeMillis() - lastPlayerListUsed.get() < 5000) {
            return;
        }

        plugin.getBot().updateGame();

        var builder = new MessageBuilder();

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
            plugin.getBot().sendMessage(channel, builder.build());
            lastPlayerListUsed.set(System.currentTimeMillis());
        }
    }
}
