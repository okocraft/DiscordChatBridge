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

package net.okocraft.discordchatbridge.chat.lunachat;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.channel.Channel;
import com.github.ucchyocean.lc3.member.ChannelMember;
import com.github.ucchyocean.lc3.member.ChannelMemberOther;
import com.github.ucchyocean.lc3.util.ClickableFormat;
import com.github.ucchyocean.lc3.util.Utility;
import net.okocraft.discordchatbridge.chat.ChatSystem;
import net.okocraft.discordchatbridge.config.FormatSettings;
import net.okocraft.discordchatbridge.database.LinkedUser;
import net.okocraft.discordchatbridge.external.LuckPermsIntegration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class LunaChatSystem implements ChatSystem {

    private static final Method SEND_MESSAGE_METHOD;

    static {
        try {
            SEND_MESSAGE_METHOD = Channel.class.getDeclaredMethod("sendMessage", ChannelMember.class, String.class, ClickableFormat.class, boolean.class);
            SEND_MESSAGE_METHOD.setAccessible(true);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

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
                var discord = new ChannelMemberDiscord(linkedUser, sender + "@" + source);

                // LunaChat Channel#chatFromOtherSource start
                // NGワード発言のマスク
                var maskedMessage = message;

                for (Pattern pattern : LunaChat.getConfig().getNgwordCompiled()) {
                    Matcher matcher = pattern.matcher(maskedMessage);
                    if (matcher.find()) {
                        maskedMessage = matcher.replaceAll(Utility.getAstariskString(matcher.group(0).length()));
                    }
                }

                // キーワード置き換え
                var msgFormat = ClickableFormat.makeFormat(channel.getFormat(), new ChannelMemberOther(discord.getName()), channel, false);

                // カラーコード置き換え チャンネルで許可されている場合に置き換える。
                if (channel.isAllowCC()) {
                    maskedMessage = Utility.replaceColorCode(maskedMessage);
                }
                // Channel#chatFromOtherSource end

                try {
                    SEND_MESSAGE_METHOD.invoke(channel, discord, maskedMessage, msgFormat, false);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Result.failure(config -> "error occurred.");
                }
            }

            return checkResult;
        } else {
            // when null, config allows non verified discord user chat.
            channel.chatFromOtherSource(sender, source, message);
            return Result.success();
        }
    }

    private @NotNull Result canSpeak(@NotNull Channel channel, @NotNull LinkedUser user) {
        ChannelMember player = ChannelMember.getChannelMember("$" + user.getUniqueId().toString());

        if (channel.getBanned().contains(player)) {
            return Result.failureAndDeleteMessage(FormatSettings.LUNACHAT_YOU_ARE_BANNED);
        }

        if (!channel.isBroadcastChannel() && !channel.getMembers().contains(player)) {
            return Result.failureAndDeleteMessage(FormatSettings.NOT_LUNACHAT_CHANNEL_MEMBER);
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
