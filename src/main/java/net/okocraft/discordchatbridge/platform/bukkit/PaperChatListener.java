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

package net.okocraft.discordchatbridge.platform.bukkit;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.constant.Constants;
import net.okocraft.discordchatbridge.listener.chat.AdventureChatListener;
import net.okocraft.discordchatbridge.util.AdventureVanillaChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class PaperChatListener extends AdventureChatListener implements Listener {

    PaperChatListener(@NotNull DiscordChatBridgePlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(@NotNull AsyncChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        var sender = event.getPlayer();
        processChat(Constants.GLOBAL_CHANNEL_NAME, sender.getName(), sender.displayName(), event.originalMessage());
        Bukkit.broadcast(AdventureVanillaChatFormatter.format(sender.getName(), event.originalMessage()));
        event.setCancelled(true);
    }
}
