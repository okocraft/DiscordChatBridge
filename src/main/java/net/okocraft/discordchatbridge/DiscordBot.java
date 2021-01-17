/*
 *     Copyright (c) 2020 Okocraft
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
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.okocraft.discordchatbridge.listener.DiscordListener;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiscordBot {

    private final DiscordChatBridge plugin;
    private final JDA jda;
    private final ExecutorService executor;

    private DiscordBot(@NotNull DiscordChatBridge plugin, @NotNull JDA jda) {
        this.plugin = plugin;
        this.jda = jda;
        this.executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "DiscordChatBridge-Thread"));
    }

    @Contract("_ -> new")
    static @NotNull DiscordBot login(@NotNull DiscordChatBridge plugin) throws IllegalStateException {
        try {
            return new DiscordBot(plugin,
                    JDABuilder.createDefault(plugin.getGeneralConfig().getToken())
                            .addEventListeners(new DiscordListener(plugin))
                            .setAutoReconnect(true)
                            .setStatus(plugin.getGeneralConfig().getStatus())
                            .setActivity(plugin.getGeneralConfig().getActivity())
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
        executor.submit(() -> {
            TextChannel channel = jda.getTextChannelById(id);

            if (channel != null && channel.canTalk()) {
                channel.sendMessage(message).queue();
            } else {
                plugin.getLogger().warning(
                        "Could not send message to channel. " +
                                "id: " + id + " content: " + message.getContentRaw()
                );
            }
        });
    }

    public void sendMessage(@NotNull TextChannel channel, @NotNull Message message) {
        executor.submit(() -> {
            if (channel.canTalk()) {
                channel.sendMessage(message).queue();
            }
        });
    }

    public void addReaction(@NotNull Message message, @NotNull String unicode) {
        executor.submit(() -> message.addReaction(unicode).queue());
    }

    public void updateGame() {
        executor.submit(() -> jda.getPresence().setActivity(plugin.getGeneralConfig().getActivity()));
    }
}
