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
import net.okocraft.discordchatbridge.external.LuckPermsIntegration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class LunaChatSystem implements ChatSystem {

    @Override
    public @NotNull Result sendChat(@NotNull String channelName, @NotNull String sender,
                                    @NotNull String source, @NotNull String message, @Nullable LinkedUser linkedUser) {
        var channel = getChannel(channelName);

        if (channel == null) {
            return Result.failure(FormatSettings.CHANNEL_NOT_FOUND);
        }

        if (linkedUser != null) {
            var checkResult = canSpeak(channel, linkedUser);

            if (checkResult.succeed()) {
                ChannelMemberOther discordSender = new ChannelMemberOther(!source.isEmpty() ? sender + "@" + source : sender);
                ChannelMemberOther hiddenCheck = new ChannelMemberOther(
                        linkedUser.getName(),
                        linkedUser.getName(),
                        "",
                        "",
                        null,
                        linkedUser.getUniqueId().toString()
                );

                var lcApi = LunaChat.getAPI();

                lcApi.getHidelist(hiddenCheck).stream()
                        .filter(channel.getMembers()::contains)
                        .forEach(hiding -> lcApi.addHidelist(hiding, discordSender));

                channel.chatFromOtherSource(sender, source, message);

                lcApi.getHidelist(discordSender).stream()
                        .filter(channel.getMembers()::contains)
                        .forEach(hiding -> lcApi.removeHidelist(hiding, discordSender));
            }

            return checkResult;
        } else {
            // when null, config allows non verified discord user chat.
            channel.chatFromOtherSource(sender, source, message);
            return Result.success();
        }
    }

    public @NotNull Result canSpeak(@NotNull Channel channel, @NotNull LinkedUser user) {
        ChannelMember player = ChannelMember.getChannelMember("$" + user.getUniqueId().toString());

        if (!channel.isBroadcastChannel() && !channel.getMembers().contains(player)) {
            return Result.failureAndDeleteMessage(FormatSettings.NOT_LUNACHAT_CHANNEL_MEMBER);
        }

        if (channel.getBanned().contains(player)) {
            return Result.failureAndDeleteMessage(FormatSettings.LUNACHAT_YOU_ARE_BANNED);
        }

        if (channel.getMuted().contains(player)) {
            return Result.failureAndDeleteMessage(FormatSettings.YOU_ARE_MUTED);
        }

        String permissionNode = "lunachat.speak." + channel.getName();
        if (player.isOnline()) {
            if (player.isPermissionSet(permissionNode)
                    && !player.hasPermission(permissionNode)) {
                return Result.failureAndDeleteMessage(FormatSettings.NO_LUNACHAT_SPEAK_PERMISSION);
            }
        } else {
            if (LuckPermsIntegration.hasPermission(user.getUniqueId(), permissionNode)) {
                return Result.failureAndDeleteMessage(FormatSettings.NO_LUNACHAT_SPEAK_PERMISSION);
            }
        }

        return Result.success();
    }

    protected abstract @Nullable Channel getChannel(@NotNull String channelName);
}
