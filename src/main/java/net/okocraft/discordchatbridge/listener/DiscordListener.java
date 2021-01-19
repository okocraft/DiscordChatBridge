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

import com.github.ucchyocean.lc3.LunaChatBungee;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.okocraft.discordchatbridge.DiscordChatBridge;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;

public class DiscordListener extends ListenerAdapter {

    private final DiscordChatBridge plugin;
    private final String lineSeparator = System.getProperty("line.separator");

    private long lastPlayerListUsed;

    public DiscordListener(@NotNull DiscordChatBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        var member = event.getMember();

        if (event.getAuthor().isBot() || member == null) {
            return;
        }

        var channelName = plugin.getGeneralConfig().getLunaChatChannel(event.getChannel().getIdLong());

        if (channelName.isEmpty()) {
            return;
        }

        var message = event.getMessage().getContentStripped();

        if (message.startsWith("!playerlist")) {
            onPlayerListCommand(event.getTextChannel());
            return;
        }

        if (plugin.getGeneralConfig().getChatMaxLength() < message.length()) {
            plugin.getBot().addReaction(event.getMessage(), "U+FE0F");
            return;
        }

        var channel = LunaChatBungee.getInstance().getLunaChatAPI().getChannel(channelName.get());

        if (channel == null) {
            return;
        }

        var name = member.getNickname() != null ? member.getNickname() : member.getEffectiveName();

        plugin.getReceivedMessages().add(message);

        channel.chatFromOtherSource(
                plugin.getBot().getRolePrefix(member) + name,
                plugin.getGeneralConfig().getSourceName(),
                message
        );
    }

    private void onPlayerListCommand(@NotNull TextChannel channel) {
        if (System.currentTimeMillis() - lastPlayerListUsed < 5000) {
            return;
        }

        plugin.getBot().updateGame();

        MessageBuilder builder = new MessageBuilder(
                plugin.getFormatConfig().getPlayerListTop()
                        .replace("%count%", String.valueOf(plugin.getProxy().getOnlineCount()))
        );

        builder.append(lineSeparator).append("```").append(lineSeparator);

        for (ServerInfo server : plugin.getProxy().getServers().values()) {
            builder.append(plugin.getFormatConfig().getPlayerListFormat()
                    .replace("%server%", server.getName())
                    .replace("%players%", server.getPlayers().stream()
                            .map(ProxiedPlayer::getName)
                            .sorted().collect(Collectors.joining(", ")))

            );

            builder.append(lineSeparator);
        }

        builder.append(lineSeparator).append("```");

        if (channel.canTalk()) {
            plugin.getBot().sendMessage(channel, builder.build());
            lastPlayerListUsed = System.currentTimeMillis();
        }
    }
}
