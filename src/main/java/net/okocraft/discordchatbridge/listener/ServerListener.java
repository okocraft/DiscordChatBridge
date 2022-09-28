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

package net.okocraft.discordchatbridge.listener;

import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.config.FormatSettings;
import net.okocraft.discordchatbridge.config.GeneralSettings;
import net.okocraft.discordchatbridge.constant.Placeholders;
import net.okocraft.discordchatbridge.util.ColorStripper;
import net.okocraft.discordchatbridge.util.FirstJoinPlayerHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class ServerListener {

    private final DiscordChatBridgePlugin plugin;
    private final Set<UUID> joinedPlayers = new HashSet<>();

    public ServerListener(@NotNull DiscordChatBridgePlugin plugin) {
        this.plugin = plugin;
    }

    protected void processJoin(@NotNull UUID uuid, @NotNull String name, @NotNull String displayName) {
        if (FirstJoinPlayerHolder.remove(uuid)) {
            var format = plugin.getFormatConfig().get(FormatSettings.SERVER_FIRST_JOIN);

            if (!format.isEmpty()) {
                sendMessage(replacePlayer(format, name, displayName));
                return;
            }
        }

        var format = plugin.getFormatConfig().get(FormatSettings.SERVER_JOIN);

        if (!format.isEmpty()) {
            sendMessage(replacePlayer(format, name, displayName));
        }

        joinedPlayers.add(uuid);
        plugin.getBot().updateGame();
    }

    protected void processDisconnection(@NotNull UUID uuid, @NotNull String name, @NotNull String displayName) {
        if (!joinedPlayers.contains(uuid)) {
            return;
        }

        var format = plugin.getFormatConfig().get(FormatSettings.SERVER_LEAVE);

        if (!format.isEmpty()) {
            sendMessage(replacePlayer(format, name, displayName));
        }

        joinedPlayers.remove(uuid);
        plugin.getBot().updateGame();
    }

    protected void processServerSwitch(@NotNull String name, @NotNull String displayName, @NotNull String serverName) {
        var format = plugin.getFormatConfig().get(FormatSettings.SERVER_SWITCH);

        if (!format.isEmpty()) {
            var playerReplaced = replacePlayer(format, name, displayName);
            var serverReplaced = replaceServer(playerReplaced, serverName);
            sendMessage(serverReplaced);
        }
    }

    protected void addJoinedPlayers(@NotNull Collection<UUID> players) {
        joinedPlayers.addAll(players);
    }

    private void sendMessage(@NotNull String message) {
        long systemChannelID = plugin.getGeneralConfig().get(GeneralSettings.SYSTEM_CHANNEL);

        if (systemChannelID != 0) {
            plugin.getBot().sendMessage(systemChannelID, message);
        }
    }

    private static @NotNull String replacePlayer(@NotNull String original, @NotNull String name, @NotNull String displayName) {
        return original
                .replace(Placeholders.PLAYER_NAME, escapeUnderscore(name))
                .replace(Placeholders.DISPLAY_NAME, escapeUnderscore(ColorStripper.strip(displayName)));
    }

    private static @NotNull String replaceServer(@NotNull String original, @NotNull String serverName) {
        return original.replace(Placeholders.SERVER_NAME, escapeUnderscore(serverName));
    }

    private static @NotNull String escapeUnderscore(@NotNull String text) {
        return text.replace("_", "\\_");
    }
}
