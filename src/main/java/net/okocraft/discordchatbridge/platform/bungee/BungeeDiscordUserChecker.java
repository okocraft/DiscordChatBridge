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

package net.okocraft.discordchatbridge.platform.bungee;

import net.okocraft.discordchatbridge.config.FormatSettings;
import net.okocraft.discordchatbridge.database.LinkedUser;
import net.okocraft.discordchatbridge.external.AdvancedBanIntegration;
import net.okocraft.discordchatbridge.platform.DiscordUserChecker;
import org.jetbrains.annotations.NotNull;

public class BungeeDiscordUserChecker implements DiscordUserChecker {

    private final DiscordChatBridgeBungee plugin;

    public BungeeDiscordUserChecker(@NotNull DiscordChatBridgeBungee plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull Result check(@NotNull LinkedUser user) {
        if (isAdvancedBanEnabled()) {
            if (AdvancedBanIntegration.isBanned(user.getUniqueId())) {
                return Result.deny(FormatSettings.YOU_ARE_BANNED);
            }

            if (AdvancedBanIntegration.isMuted(user.getUniqueId())) {
                return Result.deny(FormatSettings.YOU_ARE_MUTED);
            }
        }

        return Result.allow();
    }

    private boolean isAdvancedBanEnabled() {
        return plugin.getProxy().getPluginManager().getPlugin("AdvancedBan") != null;
    }
}
