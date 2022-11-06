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

package net.okocraft.discordchatbridge.chat;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.channel.Channel;
import com.github.ucchyocean.lc3.member.ChannelMember;
import com.github.ucchyocean.lc3.member.ChannelMemberOther;
import net.okocraft.discordchatbridge.config.FormatSettings;
import net.okocraft.discordchatbridge.database.LinkedUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class LunaChatSystem implements ChatSystem {

    @Override
    public @NotNull Result sendChat(@NotNull String channelName, @NotNull String sender,
                                    @NotNull String source, @NotNull String message, @Nullable LinkedUser linkedUser) {
        var channel = getChannel(channelName);

        if (channel == null) {
            return Result.failure(FormatSettings.DISCORD_CHAT); // todo
        }

        if (linkedUser != null) {
            var checkResult = canSpeak(channel, ChannelMemberDiscord.getChannelMember(linkedUser));

            if (checkResult.succeed()) {
                ChannelMemberOther discordSender = new ChannelMemberOther(!source.isEmpty() ? sender + "@" + source : sender);
                ChannelMemberOther hiddenCheck = new ChannelMemberOther(
                        linkedUser.getName(),
                        linkedUser.getName(),
                        "",
                        "",
                        null,
                        "$" + linkedUser.getUniqueId().toString()
                );

                var lcApi = LunaChat.getAPI();

                channel.getMembers().stream()
                        .filter(mem -> lcApi.getHidelist(mem).contains(hiddenCheck))
                        .forEach(mem -> lcApi.addHidelist(mem, discordSender));

                channel.chatFromOtherSource(sender, source, message);

                channel.getMembers().forEach(mem -> lcApi.removeHidelist(mem, discordSender));
            }

            return checkResult;
        } else {
            // when null, config allows non verified discord user chat.
            channel.chatFromOtherSource(sender, source, message);
            return Result.success();
        }
    }

    public @NotNull Result canSpeak(@NotNull Channel channel, @NotNull ChannelMember player) {
        if (!channel.isBroadcastChannel() && !channel.getMembers().contains(player))
            return Result.failure(FormatSettings.DISCORD_CHAT); // todo
        if (channel.getBanned().contains(player)) return Result.failure(FormatSettings.DISCORD_CHAT); // todo
        if (channel.getMuted().contains(player)) return Result.failure(FormatSettings.DISCORD_CHAT); // todo
        if (!player.hasPermission("lunachat.speak." + channel.getName()))
            return Result.failure(FormatSettings.DISCORD_CHAT); // todo

        return Result.success();
    }

    protected abstract @Nullable Channel getChannel(@NotNull String channelName);
}
