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

import com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitChannelMessageEvent;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.listener.chat.LunaChatListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class BukkitLunaChatListener extends LunaChatListener implements Listener {

    public BukkitLunaChatListener(@NotNull DiscordChatBridgePlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onChat(@NotNull LunaChatBukkitChannelMessageEvent e) {
        processChat(e.getChannelName(), e.getDisplayName(), e.getMember(), e.getOriginalMessage());
    }
}
