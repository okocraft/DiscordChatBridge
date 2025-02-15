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

package net.okocraft.discordchatbridge;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.okocraft.discordchatbridge.config.FormatSettings;
import net.okocraft.discordchatbridge.config.GeneralSettings;
import net.okocraft.discordchatbridge.constant.Placeholders;
import net.okocraft.discordchatbridge.listener.DiscordListener;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class DiscordBot {

    private static final Collection<Message.MentionType> ALLOWED_MENTION_TYPE =
            Set.of(Message.MentionType.EMOJI, Message.MentionType.USER, Message.MentionType.CHANNEL);

    private static final String MENTION_MARK = "@";

    private static final String CHANNEL_MARK = "#";

    private static final Pattern MENTION_PATTERN = createMentionPattern(MENTION_MARK);

    private static final Pattern CHANNEL_PATTERN = createMentionPattern(CHANNEL_MARK);

    private static final Comparator<Role> ROLE_COMPARATOR = Comparator.comparingInt(Role::getPosition);

    private static Pattern createMentionPattern(@NotNull String mark) {
        return Pattern.compile(mark + "([^" + mark + "]*?)(?:\\s|$)");
    }

    private final DiscordChatBridgePlugin plugin;
    private final JDA jda;
    private final ScheduledExecutorService scheduler;

    private DiscordBot(@NotNull DiscordChatBridgePlugin plugin, @NotNull JDA jda) {
        this.plugin = plugin;
        this.jda = jda;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "DiscordChatBridge-Thread"));

        this.loadMembers();
    }

    @Contract("_ -> new")
    public static @NotNull DiscordBot login(@NotNull DiscordChatBridgePlugin plugin) throws IllegalStateException {
        try {
            return new DiscordBot(
                    plugin,
                    JDABuilder.createDefault(plugin.getGeneralConfig().get(GeneralSettings.DISCORD_TOKEN))
                            .addEventListeners(new DiscordListener(plugin))
                            .setAutoReconnect(true)
                            .setStatus(plugin.getGeneralConfig().get(GeneralSettings.DISCORD_STATUS))
                            .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                            .setMemberCachePolicy(MemberCachePolicy.ALL)
                            .disableCache(Arrays.asList(CacheFlag.values()))
                            .build()
                            .awaitReady()
            );
        } catch (InterruptedException e) {
            throw new IllegalStateException("Could not log in to Discord", e);
        }
    }

    public void shutdown() {
        this.jda.shutdown();
        this.scheduler.shutdown();
    }

    public void sendMessage(long id, @NotNull String message) {
        this.sendMessage(id, new MessageCreateBuilder().setContent(message).build());
    }

    public void sendMessage(long id, @NotNull MessageCreateData message) {
        var channel = this.jda.getChannelById(MessageChannel.class, id);

        if (channel != null) {
            this.sendMessage(channel, message);
        } else {
            this.plugin.getWrappedLogger().warning("Could not find the channel with id " + id);
        }
    }

    public void sendMessage(@NotNull MessageChannel channel, @NotNull String message) {
        this.sendMessage(channel, new MessageCreateBuilder().setContent(message).build());
    }

    public void sendMessage(@NotNull MessageChannel channel, @NotNull MessageCreateData message) {
        this.scheduler.submit(() -> {
            if (channel.canTalk()) {
                channel.sendMessage(message).queue();
            } else {
                this.plugin.getWrappedLogger().warning(
                        "Could not send message to channel. " +
                                "id: " + channel.getId() + " content: " + message.getContent()
                );
            }
        });
    }

    public void sendChat(long id, @NotNull String original,
                         @NotNull String name, @NotNull String displayName) {
        this.scheduler.submit(() -> this.processChat(id, original, name, displayName));
    }

    public void addReaction(@NotNull Message message, @NotNull String unicode) {
        this.scheduler.submit(() -> message.addReaction(Emoji.fromUnicode(unicode)).queue());
    }

    public @Nullable Role getFirstRole(@NotNull Member member) {
        var roles = member.getRoles();

        if (roles.isEmpty()) {
            return null;
        } else {
            return roles.stream().min(ROLE_COMPARATOR).orElse(null);
        }
    }

    private void processChat(long id, @NotNull String original,
                             @NotNull String name, @NotNull String displayName) {
        var plain =
                this.plugin.getFormatConfig().get(FormatSettings.DISCORD_CHAT)
                        .replace(Placeholders.PLAYER_NAME, this.escapeUnderscore(name))
                        .replace(Placeholders.DISPLAY_NAME, this.escapeUnderscore(displayName))
                        .replace(Placeholders.MESSAGE, original);

        plain = plain.replace("@everyone", "@.everyone");
        plain = plain.replace("@here", "@.here");

        if (plain.contains(MENTION_MARK)) {
            plain = this.replaceMention(plain);
        }

        if (plain.contains(CHANNEL_MARK)) {
            plain = this.replaceChannel(plain);
        }

        if (plain.length() < Message.MAX_CONTENT_LENGTH) {
            this.sendMessage(id, new MessageCreateBuilder().addContent(plain).setAllowedMentions(ALLOWED_MENTION_TYPE).build());
        } else {
            this.plugin.getWrappedLogger().warning("The message is too long! :" + plain);
        }
    }

    public void updateGame() {
        this.scheduler.schedule(() -> {
                    var type = this.plugin.getGeneralConfig().get(GeneralSettings.DISCORD_ACTIVITY_TYPE);
                    var game =
                            this.plugin.getGeneralConfig()
                                    .get(GeneralSettings.DISCORD_ACTIVITY_GAME)
                                    .replace(
                                            Placeholders.PLAYER_COUNT, String.valueOf(this.plugin.getPlatformInfo().getNumberOfPlayers())
                                    );
                    var url = this.plugin.getGeneralConfig().get(GeneralSettings.DISCORD_ACTIVITY_URL);

            this.jda.getPresence().setActivity(Activity.of(type, game, url));
                }, 1, TimeUnit.SECONDS
        );
    }

    public @NotNull String escapeUnderscore(@NotNull String text) {
        return text.replace("_", "\\_");
    }

    private @NotNull String replaceMention(@NotNull String original) {
        return MENTION_PATTERN.matcher(original).replaceAll(result ->
                Optional.of(result)
                        .map(this::searchForUserOrRoleMention)
                        .orElseGet(result::group)
        );
    }

    private @Nullable String searchForUserOrRoleMention(@NotNull MatchResult result) {
        var name = result.group(1);
        var userMention = this.searchForUserMention(name);

        if (userMention != null) {
            return userMention;
        }

        return this.searchForRoleMention(name);
    }

    private @Nullable String searchForUserMention(@NotNull String username) {
        return this.getMentionableFromAllGuilds(guild -> this.searchForUser(guild, username));
    }

    private @NotNull Collection<Member> searchForUser(@NotNull Guild guild, @NotNull String username) {
        return guild.getMembersByEffectiveName(username, true);
    }

    private @Nullable String searchForRoleMention(@NotNull String roleName) {
        return this.getMentionableFromAllGuilds(guild -> guild.getRolesByName(roleName, true));
    }

    private @NotNull String replaceChannel(@NotNull String original) {
        return CHANNEL_PATTERN.matcher(original).replaceAll(result ->
                Optional.of(result)
                        .map(this::searchForChannelMention)
                        .orElseGet(result::group)
        );
    }

    private @Nullable String searchForChannelMention(@NotNull MatchResult result) {
        var channelName = result.group();
        return this.getMentionableFromAllGuilds(guild -> this.searchForGuildMessageChannels(guild, channelName));
    }

    private @NotNull Collection<? extends GuildMessageChannel> searchForGuildMessageChannels(@NotNull Guild guild,
                                                                                             @NotNull String channelName) {
        var textChannels = guild.getTextChannelsByName(channelName, true);

        if (!textChannels.isEmpty()) {
            return textChannels;
        }

        var threadChannels = guild.getThreadChannelsByName(channelName, true);

        if (!threadChannels.isEmpty()) {
            return threadChannels;
        }

        return guild.getNewsChannelsByName(channelName, true);
    }

    private <M extends IMentionable> @Nullable String getMentionableFromAllGuilds(@NotNull Function<Guild, Collection<M>> mentionableFunction) {
        return this.jda.getGuilds().stream()
                .map(mentionableFunction)
                .flatMap(Collection::stream)
                .map(IMentionable::getAsMention)
                .findFirst()
                .orElse(null);
    }

    private void loadMembers() {
        this.jda.getGuilds().stream()
                .map(Guild::loadMembers)
                .forEach(task -> task.onError(this::failedToLoadMembers));
    }

    private void failedToLoadMembers(@NotNull Throwable ex) {
        this.plugin.getWrappedLogger().error("Failed to load members.", ex);
    }
}
