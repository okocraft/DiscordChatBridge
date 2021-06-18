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

package net.okocraft.discordchatbridge.constant;

public final class Placeholders {

    public static final String PLAYER_COUNT = "%player_count%";

    public static final String SERVER_NAME = "%server_name%";

    public static final String PLAYER_LIST = "%player_list%";

    public static final String PLAYER_NAME = "%player_name%";

    public static final String DISPLAY_NAME = "%display_name%";

    public static final String ROLE_COLOR = "%role_color%";

    public static final String MESSAGE = "%message%";

    public static final String PERMISSION = "%permission%";

    private Placeholders() {
        throw new UnsupportedOperationException();
    }
}
