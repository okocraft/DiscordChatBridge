/*
 *     Copyright (c) 2026 Okocraft
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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.concurrent.Task;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.okocraft.discordchatbridge.config.FormatSettings;
import net.okocraft.discordchatbridge.logger.LoggerWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscordBotChatTest {

    private final DiscordChatBridgePlugin plugin = mock(DiscordChatBridgePlugin.class);
    private final JDA jda = mock(JDA.class);
    private final Guild guild = mock(Guild.class);
    private final MessageChannel channel = mock(MessageChannel.class);
    private final MessageCreateAction action = mock(MessageCreateAction.class);
    private final LoggerWrapper logger = mock(LoggerWrapper.class);
    private final FormatSettings format = new FormatSettings();
    private DiscordBot bot;

    @BeforeEach
    void setUp() {
        format.server.chat = "%message%";
        when(plugin.getFormatConfig()).thenReturn(format);
        when(jda.getGuilds()).thenReturn(List.of(guild));
        doReturn(mock(Task.class)).when(guild).loadMembers();
        when(jda.getChannelById(MessageChannel.class, 1L)).thenReturn(channel);
        when(channel.canTalk()).thenReturn(true);
        when(channel.sendMessage(any(MessageCreateData.class))).thenReturn(action);
    }

    @AfterEach
    void tearDown() {
        bot.shutdown();
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "@Alice hello|<@1> hello",
            "@Alice  @Bob|<@1>  <@2>",
            "hi @アリス|hi <@3>",
            "@Unknown hello|@Unknown hello",
            "@$1\\x hi|@$1\\x hi",
            "see #general now|see <#9> now",
    })
    void testMentionsAreResolvedWithoutChangingSurroundingText(String message, String expected) {
        doReturn(mentions(Member.class, "<@1>")).when(guild).getMembersByEffectiveName("Alice", true);
        doReturn(mentions(Member.class, "<@2>")).when(guild).getMembersByEffectiveName("Bob", true);
        doReturn(mentions(Member.class, "<@3>")).when(guild).getMembersByEffectiveName("アリス", true);
        doReturn(mentions(TextChannel.class, "<#9>")).when(guild).getTextChannelsByName("general", true);
        bot = new DiscordBot(plugin, () -> jda);

        bot.sendChat(1L, message, "Steve", "Steve");

        assertEquals(expected, sent().getContent());
    }

    @Test
    void testMessageAtMaximumLengthIsSent() {
        var message = "a".repeat(Message.MAX_CONTENT_LENGTH);
        bot = new DiscordBot(plugin, () -> jda);

        bot.sendChat(1L, message, "Steve", "Steve");

        assertEquals(message, sent().getContent());
    }

    @Test
    void testMessageOverMaximumLengthIsLoggedWithoutContent() {
        when(plugin.getWrappedLogger()).thenReturn(logger);
        bot = new DiscordBot(plugin, () -> jda);

        bot.sendChat(1L, "a".repeat(Message.MAX_CONTENT_LENGTH + 1), "Steve", "Steve");

        verify(logger, timeout(5_000)).warning("Could not send message to channel 1: too long (2001 characters)");
        verify(channel, never()).sendMessage(any(MessageCreateData.class));
    }

    @Test
    void testSendFailureIsLoggedWithChannelAndReason() {
        when(plugin.getWrappedLogger()).thenReturn(logger);
        when(channel.getId()).thenReturn("1");
        doAnswer(invocation -> {
            invocation.<Consumer<Throwable>>getArgument(1).accept(new IllegalStateException("50013: Missing Permissions"));
            return null;
        }).when(action).queue(isNull(), any());
        bot = new DiscordBot(plugin, () -> jda);

        bot.sendMessage(1L, "secret");

        verify(logger, timeout(5_000)).warning("Could not send message to channel 1: 50013: Missing Permissions");
    }

    @Test
    void testChannelWithoutPermissionIsLoggedWithoutContent() {
        when(plugin.getWrappedLogger()).thenReturn(logger);
        when(channel.getId()).thenReturn("1");
        when(channel.canTalk()).thenReturn(false);
        bot = new DiscordBot(plugin, () -> jda);

        bot.sendMessage(1L, "secret");

        verify(logger, timeout(5_000)).warning("Could not send message to channel 1: no permission to send messages");
        verify(channel, never()).sendMessage(any(MessageCreateData.class));
    }

    @Test
    void testUnknownChannelIsLogged() {
        when(plugin.getWrappedLogger()).thenReturn(logger);
        bot = new DiscordBot(plugin, () -> jda);

        bot.sendMessage(2L, "secret");

        verify(logger, timeout(5_000)).warning("Could not find the channel with id 2");
    }

    @Test
    void testReactionFailureIsLoggedWithMessageAndReason() {
        var message = mock(Message.class);
        RestAction<Void> reaction = mock();
        when(plugin.getWrappedLogger()).thenReturn(logger);
        when(message.getId()).thenReturn("5");
        when(message.addReaction(any())).thenReturn(reaction);
        doAnswer(invocation -> {
            invocation.<Consumer<Throwable>>getArgument(1).accept(new IllegalStateException("10008: Unknown Message"));
            return null;
        }).when(reaction).queue(isNull(), any());
        bot = new DiscordBot(plugin, () -> jda);

        bot.addReaction(message, "U+26A0");

        verify(logger, timeout(5_000)).warning("Could not add reaction to message 5: 10008: Unknown Message");
    }

    @Test
    void testChatNotifiesOnlyUsers() {
        bot = new DiscordBot(plugin, () -> jda);

        bot.sendChat(1L, "hello", "Steve", "Steve");

        assertEquals(EnumSet.of(Message.MentionType.EMOJI, Message.MentionType.USER, Message.MentionType.CHANNEL),
                sent().getAllowedMentions());
    }

    @Test
    void testSystemMessageNotifiesNobody() {
        bot = new DiscordBot(plugin, () -> jda);

        bot.sendMessage(1L, "@everyone joined");

        assertEquals(EnumSet.noneOf(Message.MentionType.class), sent().getAllowedMentions());
    }

    @Test
    void testReplyToChannelNotifiesNobody() {
        bot = new DiscordBot(plugin, () -> jda);

        bot.sendMessage(channel, "@everyone is online");

        assertEquals(EnumSet.noneOf(Message.MentionType.class), sent().getAllowedMentions());
    }

    private static <M extends IMentionable> List<M> mentions(Class<M> type, String mention) {
        var mentionable = mock(type);
        when(mentionable.getAsMention()).thenReturn(mention);
        return List.of(mentionable);
    }

    private MessageCreateData sent() {
        var captor = ArgumentCaptor.forClass(MessageCreateData.class);
        verify(channel, timeout(5_000)).sendMessage(captor.capture());
        return captor.getValue();
    }
}
