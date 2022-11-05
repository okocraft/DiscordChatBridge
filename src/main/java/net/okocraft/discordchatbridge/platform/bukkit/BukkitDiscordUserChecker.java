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

import net.okocraft.discordchatbridge.database.LinkedUser;
import net.okocraft.discordchatbridge.external.AdvancedBanIntegration;
import net.okocraft.discordchatbridge.platform.DiscordUserChecker;
import org.jetbrains.annotations.NotNull;

public class BukkitDiscordUserChecker implements DiscordUserChecker {

    private final DiscordChatBridgeBukkit plugin;

    public BukkitDiscordUserChecker(@NotNull DiscordChatBridgeBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull Result check(@NotNull LinkedUser user) {
        if (isAdvancedBanEnabled()) {
            if (AdvancedBanIntegration.isBanned(user.getUniqueId())) {
                return Result.deny("banned");
            }

            if (AdvancedBanIntegration.isMuted(user.getUniqueId())) {
                return Result.deny("muted");
            }
        }

        var offlinePlayer = plugin.getServer().getOfflinePlayer(user.getUniqueId());

        if (offlinePlayer.isBanned()) {
            return Result.deny("banned");
        }

        return Result.allow();
    }

    private boolean isAdvancedBanEnabled() {
        return plugin.getServer().getPluginManager().getPlugin("AdvancedBan") != null;
    }
}
