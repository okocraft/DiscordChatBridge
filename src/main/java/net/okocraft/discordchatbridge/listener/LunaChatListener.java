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

package net.okocraft.discordchatbridge.listener;

import com.github.siroshun09.mcmessage.util.Colorizer;
import com.github.ucchyocean.lc3.bungee.event.LunaChatBungeeChannelMessageEvent;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.okocraft.discordchatbridge.DiscordChatBridge;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static net.dv8tion.jda.api.entities.Message.MentionType.CHANNEL;
import static net.dv8tion.jda.api.entities.Message.MentionType.EMOTE;
import static net.dv8tion.jda.api.entities.Message.MentionType.USER;

public class LunaChatListener implements Listener {

    private static final Collection<Message.MentionType> ALLOWED_MENTIONS = Set.of(EMOTE, CHANNEL, USER);

    private final DiscordChatBridge plugin;

    public LunaChatListener(@NotNull DiscordChatBridge plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(@NotNull LunaChatBungeeChannelMessageEvent e) {
        String message = Colorizer.stripColorCode(e.getOriginalMessage());

        if (message.isEmpty()) {
            return;
        }

        Optional<Long> id = plugin.getGeneralConfig().getDiscordChannel(e.getChannel().getName());

        if (id.isEmpty()) {
            return;
        }

        if (plugin.getReceivedMessages().contains(message)) {
            plugin.getReceivedMessages().remove(message);
            return;
        }

        message = plugin.getFormatConfig().getDiscordChatFormat()
                .replace("%player%", e.getMember().getName())
                .replace("%display_name%", e.getMember().getDisplayName())
                .replace("%message%", message);

        if (message.isEmpty()) {
            return;
        }

        var toSend =
                new MessageBuilder(message)
                        .setAllowedMentions(ALLOWED_MENTIONS)
                        .build();
        var raw = toSend.getContentRaw();

        if (raw.length() < 2000) {
            plugin.getBot().sendMessage(id.get(), toSend);
        } else {
            plugin.getLogger().warning("The message is too long! :" + raw);
        }
    }
}
