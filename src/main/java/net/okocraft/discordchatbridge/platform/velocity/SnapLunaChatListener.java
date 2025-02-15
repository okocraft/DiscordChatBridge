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

package net.okocraft.discordchatbridge.platform.velocity;

import com.github.ucchyocean.lc3.bungee.event.LunaChatBungeeChannelMessageEvent;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.listener.chat.LunaChatListener;
import org.jetbrains.annotations.NotNull;

public class SnapLunaChatListener extends LunaChatListener {

    private static SnapLunaChatListener instance;

    public static SnapLunaChatListener getInstance() {
        return instance;
    }

    public static void init(final DiscordChatBridgePlugin plugin) {
        instance = new SnapLunaChatListener(plugin);
    }

    private SnapLunaChatListener(@NotNull DiscordChatBridgePlugin plugin) {
        super(plugin);
    }

    public void onChat(@NotNull LunaChatBungeeChannelMessageEvent e) {
        processChat(e.getChannelName(), e.getDisplayName(), e.getMember(), e.getOriginalMessage());
    }
}
