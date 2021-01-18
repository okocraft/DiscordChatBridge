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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.okocraft.discordchatbridge.listener.DiscordListener;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;

public class DiscordBot {

    private static final Collection<Message.MentionType> ALLOWED_MENTION_TYPE =
            Set.of(Message.MentionType.EMOTE, Message.MentionType.USER, Message.MentionType.CHANNEL);
    private static final String PLAYER_NAME = "%player%";
    private static final String DISPLAY_NAME = "%display_name%";
    private static final String MESSAGE = "%message%";
    private static final String MENTION_MARK = "@";
    private static final String CHANNEL_MARK = "#";
    private static final Pattern EVERYONE_PATTERN = Pattern.compile("@everyone");
    private static final String EVERYONE_REPLACEMENT = "@.everyone";
    private static final Pattern HERE_PATTERN = Pattern.compile("@here");
    private static final String HERE_REPLACEMENT = "@.here";

    private final DiscordChatBridge plugin;
    private final JDA jda;
    private final ExecutorService executor;

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

    private DiscordBot(@NotNull DiscordChatBridge plugin, @NotNull JDA jda) {
        this.plugin = plugin;
        this.jda = jda;
        this.executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "DiscordChatBridge-Thread"));
    }

    @Contract("_ -> new")
    static @NotNull DiscordBot login(@NotNull DiscordChatBridge plugin) throws IllegalStateException {
        try {
            return new DiscordBot(
                    plugin,
                    JDABuilder.createDefault(plugin.getGeneralConfig().getToken())
                            .addEventListeners(new DiscordListener(plugin))
                            .setAutoReconnect(true)
                            .setStatus(plugin.getGeneralConfig().getStatus())
                            .setActivity(plugin.getGeneralConfig().createActivity())
                            .build().awaitReady()
            );
        } catch (InterruptedException | LoginException e) {
            throw new IllegalStateException("Could not log in to Discord", e);
        }
    }

    public void shutdown() {
        jda.shutdown();
        executor.shutdown();
    }

    public void sendMessage(long id, @NotNull Message message) {
        executor.submit(() -> sendMessageToChannel(id, message));
    }

    public void sendMessage(@NotNull TextChannel channel, @NotNull Message message) {
        executor.submit(() -> {
            if (channel.canTalk()) {
                channel.sendMessage(message).queue();
            }
        });
    }

    public void sendChat(long id, @NotNull String original,
                         @NotNull String name, @NotNull String displayName) {
        executor.submit(() -> processChat(id, original, name, displayName));
    }

    public void addReaction(@NotNull Message message, @NotNull String unicode) {
        executor.submit(() -> message.addReaction(unicode).queue());
    }

    private void processChat(long id, @NotNull String original,
                             @NotNull String name, @NotNull String displayName) {
        var plain =
                plugin.getFormatConfig().getDiscordChatFormat()
                        .replace(PLAYER_NAME, name)
                        .replace(DISPLAY_NAME, displayName)
                        .replace(MESSAGE, original);

        plain = EVERYONE_PATTERN.matcher(plain).replaceAll(EVERYONE_REPLACEMENT);
        plain = HERE_PATTERN.matcher(plain).replaceAll(HERE_REPLACEMENT);

        if (plain.contains(MENTION_MARK)) {
            try {
                plain = plugin.getBot().replaceMention(plain);
            } catch (ExecutionException ignored) {
            }
        }

        if (plain.contains(CHANNEL_MARK)) {
            try {
                plain = plugin.getBot().replaceChannel(plain);
            } catch (ExecutionException ignored) {
            }
        }

        if (plain.length() < Message.MAX_CONTENT_LENGTH) {
            sendMessageToChannel(id, new MessageBuilder(plain).build());
        } else {
            plugin.getLogger().warning("The message is too long! :" + plain);
        }
    }

    public void updateGame() {
        executor.submit(() -> {
            var newActivity = plugin.getGeneralConfig().createActivity();
            jda.getPresence().setActivity(newActivity);
        });
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
            plugin.getLogger().warning(
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
