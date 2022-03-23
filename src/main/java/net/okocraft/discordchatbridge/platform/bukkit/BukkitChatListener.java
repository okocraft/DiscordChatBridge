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

package net.okocraft.discordchatbridge.platform.bukkit;

import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.constant.Constants;
import net.okocraft.discordchatbridge.listener.chat.VanillaChatListener;
import net.okocraft.discordchatbridge.util.VanillaChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

public class BukkitChatListener extends VanillaChatListener implements Listener {

    public BukkitChatListener(@NotNull DiscordChatBridgePlugin plugin) {
        super(plugin);
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(@NotNull AsyncPlayerChatEvent e) {
        if (e.isCancelled()) {
            return;
        }

        var sender = e.getPlayer();

        processChat(Constants.GLOBAL_CHANNEL_NAME, sender.getName(), sender.getDisplayName(), e.getMessage());
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', VanillaChatFormatter.format(sender.getName(), e.getMessage())));
        e.setCancelled(true);
    }
}
