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

import com.github.ucchyocean.lc3.bungee.event.LunaChatBungeeChannelMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.okocraft.discordchatbridge.DiscordChatBridge;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class LunaChatListener implements Listener {

    private static final Pattern COLOR_SECTION_PATTERN = Pattern.compile("(?i)§[0-9A-FK-ORX]");
    private static final String EMPTY = "";

    private final DiscordChatBridge plugin;

    public LunaChatListener(@NotNull DiscordChatBridge plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(@NotNull LunaChatBungeeChannelMessageEvent e) {
        var message = Colorizer.stripColorCode(e.getOriginalMessage());

        if (message.isEmpty()) {
            return;
        }

        var id = plugin.getGeneralConfig().getDiscordChannel(e.getChannel().getName());

        if (id.isEmpty()) {
            return;
        }

        if (plugin.getReceivedMessages().contains(message)) {
            plugin.getReceivedMessages().remove(message);
            return;
        }

        plugin.getBot().sendChat(
                id.get(),
                COLOR_SECTION_PATTERN.matcher(e.getOriginalMessage()).replaceAll(EMPTY),
                e.getMember().getName(),
                e.getMember().getDisplayName()
        );
    }
}
