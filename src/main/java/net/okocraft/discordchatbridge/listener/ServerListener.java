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

import net.dv8tion.jda.api.MessageBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.okocraft.discordchatbridge.DiscordChatBridge;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ServerListener implements Listener {

    private static final Pattern COLOR_SECTION_PATTERN = Pattern.compile("(?i)§[0-9A-FK-ORX]");
    private static final String EMPTY = "";

    private final DiscordChatBridge plugin;
    private final Set<UUID> joinedPlayers = new HashSet<>();

    public ServerListener(@NotNull DiscordChatBridge plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDisconnect(@NotNull PlayerDisconnectEvent e) {
        if (!joinedPlayers.contains(e.getPlayer().getUniqueId())) {
            return;
        }

        var format = plugin.getFormatConfig().getServerLeftFormat();

        if (!format.isEmpty()) {
            sendMessage(replace(format, e.getPlayer()));
        }

        joinedPlayers.remove(e.getPlayer().getUniqueId());
        plugin.getProxy().getScheduler().schedule(plugin, () -> plugin.getBot().updateGame(), 3, TimeUnit.SECONDS);
    }

    @EventHandler
    public void onJoinOrSwitch(@NotNull ServerSwitchEvent e) {
        if (e.getFrom() == null) {
            var format = plugin.getFormatConfig().getServerJoinFormat();

            if (!format.isEmpty()) {
                sendMessage(replace(format, e.getPlayer()));
            }

            joinedPlayers.add(e.getPlayer().getUniqueId());
        } else {
            var format = plugin.getFormatConfig().getServerSwitchFormat();

            if (!format.isEmpty()) {
                sendMessage(replace(format, e.getPlayer(), e.getPlayer().getServer().getInfo()));
            }
        }

        plugin.getProxy().getScheduler().schedule(plugin, () -> plugin.getBot().updateGame(), 3, TimeUnit.SECONDS);
    }

    private void sendMessage(@NotNull String message) {
        var system = plugin.getGeneralConfig().getSystemChannel();
        var toSend = new MessageBuilder(message).build();

        if (system != null) {
            plugin.getBot().sendMessage(system.getId(), toSend);
        }
    }

    private @NotNull String replace(@NotNull String format, @NotNull ProxiedPlayer player) {
        return format
                .replace("%player%", player.getName())
                .replace("%display_name%", COLOR_SECTION_PATTERN.matcher(player.getDisplayName()).replaceAll(EMPTY));
    }

    private @NotNull String replace(@NotNull String format, @NotNull ProxiedPlayer player, @NotNull ServerInfo server) {
        return replace(format, player).replace("%server%", server.getName());
    }
}
