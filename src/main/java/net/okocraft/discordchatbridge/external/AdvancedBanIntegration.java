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

package net.okocraft.discordchatbridge.external;

import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.utils.PunishmentType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class AdvancedBanIntegration {

    public static boolean isMuted(@NotNull UUID uuid) {
        return hasPunishment(uuid, PunishmentType.MUTE);
    }

    public static boolean isBanned(@NotNull UUID uuid) {
        return hasPunishment(uuid, PunishmentType.BAN);
    }

    private static boolean hasPunishment(@NotNull UUID uuid, @NotNull PunishmentType type) {
        return !PunishmentManager.get().getPunishments(uuid.toString().replace("-", ""), type, true).isEmpty();
    }
}
