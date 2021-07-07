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

package net.okocraft.discordchatbridge;

import com.github.siroshun09.configapi.api.Configuration;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.md_5.bungee.api.ChatColor;
import net.okocraft.discordchatbridge.config.FormatSettings;
import net.okocraft.discordchatbridge.config.GeneralSettings;
import net.okocraft.discordchatbridge.constant.Placeholders;
import net.okocraft.discordchatbridge.listener.DiscordListener;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;

public class DiscordBot {

    private static final Collection<Message.MentionType> ALLOWED_MENTION_TYPE =
            Set.of(Message.MentionType.EMOTE, Message.MentionType.USER, Message.MentionType.CHANNEL);

    private static final String MENTION_MARK = "@";

    private static final String CHANNEL_MARK = "#";

    private static final Pattern EVERYONE_PATTERN = Pattern.compile("@everyone");

    private static final String EVERYONE_REPLACEMENT = "@.everyone";

    private static final Pattern HERE_PATTERN = Pattern.compile("@here");

    private static final String HERE_REPLACEMENT = "@.here";

    private static final String DEFAULT_ROLE_COLOR_CODE = ChatColor.of(new Color(Role.DEFAULT_COLOR_RAW)).toString();

    private static final Comparator<Role> ROLE_COMPARATOR = Comparator.comparingInt(Role::getPosition);

    private final DiscordChatBridgePlugin plugin;
    private final JDA jda;
    private final Configuration rolePrefixes;
    private final ScheduledExecutorService scheduler;

    private final LoadingCache<String, Pattern> mentionPatternCache =
            createCache(name -> Pattern.compile(
                    Pattern.quote("@" + name),
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
            ));

    private final LoadingCache<String, Pattern> channelPatternCache =
            createCache(channel -> Pattern.compile(
                    Pattern.quote("#" + channel),
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
            ));

    private DiscordBot(@NotNull DiscordChatBridgePlugin plugin, @NotNull JDA jda) {
        this.plugin = plugin;
        this.jda = jda;
        this.rolePrefixes = plugin.getGeneralConfig().get(GeneralSettings.ROLE_PREFIX);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "DiscordChatBridge-Thread"));
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
                            .build()
                            .awaitReady()
            );
        } catch (InterruptedException | LoginException e) {
            throw new IllegalStateException("Could not log in to Discord", e);
        }
    }

    public void shutdown() {
        jda.shutdown();
        scheduler.shutdown();
    }

    public void sendMessage(long id, @NotNull Message message) {
        scheduler.submit(() -> sendMessageToChannel(id, message));
    }

    public void sendMessage(@NotNull TextChannel channel, @NotNull Message message) {
        scheduler.submit(() -> {
            if (channel.canTalk()) {
                channel.sendMessage(message).queue();
            }
        });
    }

    public void sendChat(long id, @NotNull String original,
                         @NotNull String name, @NotNull String displayName) {
        scheduler.submit(() -> processChat(id, original, name, displayName));
    }

    public void addReaction(@NotNull Message message, @NotNull String unicode) {
        scheduler.submit(() -> message.addReaction(unicode).queue());
    }

    public @NotNull String getRolePrefix(@NotNull Member member) {
        var roles = member.getRoles();

        if (roles.isEmpty()) {
            return plugin.getGeneralConfig()
                    .get(GeneralSettings.DEFAULT_ROLE_PREFIX)
                    .replace(Placeholders.ROLE_COLOR, DEFAULT_ROLE_COLOR_CODE);
        }

        roles = new ArrayList<>(roles);
        roles.sort(ROLE_COMPARATOR);
        var role = roles.get(0);
        var roleColor = role.getColor();

        String color;

        if (roleColor != null) {
            color = ChatColor.of(roleColor).toString();
        } else {
            color = DEFAULT_ROLE_COLOR_CODE;
        }

        var prefix = rolePrefixes.getString(Long.toString(role.getIdLong()));

        if (prefix.isEmpty()) {
            return plugin.getGeneralConfig()
                    .get(GeneralSettings.DEFAULT_ROLE_PREFIX)
                    .replace(Placeholders.ROLE_COLOR, color);
        } else {
            return prefix.replace(Placeholders.ROLE_COLOR, color);
        }
    }

    private void processChat(long id, @NotNull String original,
                             @NotNull String name, @NotNull String displayName) {
        var plain =
                plugin.getFormatConfig().get(FormatSettings.DISCORD_CHAT)
                        .replace(Placeholders.PLAYER_NAME, name)
                        .replace(Placeholders.DISPLAY_NAME, displayName)
                        .replace(Placeholders.MESSAGE, original);

        plain = EVERYONE_PATTERN.matcher(plain).replaceAll(EVERYONE_REPLACEMENT);
        plain = HERE_PATTERN.matcher(plain).replaceAll(HERE_REPLACEMENT);

        if (plain.contains(MENTION_MARK)) {
            try {
                plain = replaceMention(plain);
            } catch (ExecutionException ignored) {
            }
        }

        if (plain.contains(CHANNEL_MARK)) {
            try {
                plain = replaceChannel(plain);
            } catch (ExecutionException ignored) {
            }
        }

        if (plain.length() < Message.MAX_CONTENT_LENGTH) {
            sendMessageToChannel(id, new MessageBuilder(plain).build());
        } else {
            plugin.getJavaLogger().warning("The message is too long! :" + plain);
        }
    }

    public void updateGame() {
        scheduler.schedule(() -> {
                    var type = plugin.getGeneralConfig().get(GeneralSettings.DISCORD_ACTIVITY_TYPE);
                    var game =
                            plugin.getGeneralConfig()
                                    .get(GeneralSettings.DISCORD_ACTIVITY_GAME)
                                    .replace(
                                            Placeholders.PLAYER_COUNT, String.valueOf(plugin.getPlatformInfo().getNumberOfPlayers())
                                    );
                    var url = plugin.getGeneralConfig().get(GeneralSettings.DISCORD_ACTIVITY_URL);

                    jda.getPresence().setActivity(Activity.of(type, game, url));
                }, 1, TimeUnit.SECONDS
        );
    }

    private static LoadingCache<String, Pattern> createCache(@NotNull Function<String, Pattern> function) {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(15, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<>() {
                            @Override
                            public Pattern load(@NotNull String key) {
                                return function.apply(key);
                            }
                        }
                );
    }

    private void sendMessageToChannel(long id, @NotNull Message message) {
        var channel = jda.getTextChannelById(id);

        if (channel != null && channel.canTalk()) {
            channel.sendMessage(message)
                    .allowedMentions(ALLOWED_MENTION_TYPE)
                    .queue();
        } else {
            plugin.getJavaLogger().warning(
                    "Could not send message to channel. " +
                            "id: " + id + " content: " + message.getContentRaw()
            );
        }
    }

    private @NotNull String replaceMention(@NotNull String original) throws ExecutionException {
        var temp = original;

        for (var guild : jda.getGuilds()) {
            for (Member member : guild.getMembers()) {
                var pattern = mentionPatternCache.get(member.getEffectiveName());
                temp = pattern.matcher(temp).replaceAll(member.getAsMention());
            }
        }

        return temp;
    }

    private @NotNull String replaceChannel(@NotNull String original) throws ExecutionException {
        var temp = original;

        for (var guild : jda.getGuilds()) {
            for (var channel : guild.getTextChannels()) {
                var pattern = channelPatternCache.get(channel.getName());
                temp = pattern.matcher(temp).replaceAll(channel.getAsMention());
            }
        }

        return temp;
    }
}
