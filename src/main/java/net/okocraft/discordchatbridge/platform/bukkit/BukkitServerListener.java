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

package net.okocraft.discordchatbridge.platform.bukkit;

import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.listener.ServerListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class BukkitServerListener extends ServerListener implements Listener {

    public BukkitServerListener(@NotNull DiscordChatBridgePlugin plugin) {
        super(plugin);

        var players = Bukkit.getOnlinePlayers();

        if (!players.isEmpty()) {
            addJoinedPlayers(
                    players.stream()
                            .map(Entity::getUniqueId)
                            .toList()
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(@NotNull PlayerJoinEvent event) {
        var player = event.getPlayer();
        processJoin(player.getUniqueId(), player.getName(), player.getDisplayName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(@NotNull PlayerQuitEvent event) {
        var player = event.getPlayer();
        processDisconnection(player.getUniqueId(), player.getName(), player.getDisplayName());
    }
}
