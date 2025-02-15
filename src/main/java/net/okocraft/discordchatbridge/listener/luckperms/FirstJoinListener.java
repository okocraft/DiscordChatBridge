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

package net.okocraft.discordchatbridge.listener.luckperms;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.user.UserFirstLoginEvent;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.util.FirstJoinPlayerHolder;
import org.jetbrains.annotations.NotNull;

public class FirstJoinListener {

    private final EventSubscription<UserFirstLoginEvent> listener;

    public FirstJoinListener(@NotNull DiscordChatBridgePlugin plugin) {
        this.listener =
                LuckPermsProvider.get()
                        .getEventBus()
                        .subscribe(
                                plugin,
                                UserFirstLoginEvent.class,
                                event -> FirstJoinPlayerHolder.add(event.getUniqueId())
                        );
    }

    public void unsubscribe() {
        listener.close();
    }
}
