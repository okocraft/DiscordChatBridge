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

package net.okocraft.discordchatbridge;

import net.okocraft.discordchatbridge.chat.ChatSystem;
import net.okocraft.discordchatbridge.config.ConfigLoader;
import net.okocraft.discordchatbridge.config.FormatSettings;
import net.okocraft.discordchatbridge.config.GeneralSettings;
import net.okocraft.discordchatbridge.logger.LoggerWrapper;
import net.okocraft.discordchatbridge.platform.PlatformInfo;
import net.okocraft.discordchatbridge.util.FirstJoinPlayerHolder;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Path;

public interface DiscordChatBridgePlugin {

    @NotNull LoggerWrapper getWrappedLogger();

    @NotNull Path getDataDirectory();

    @NotNull ConfigLoader.ConfigPair getSettings();

    void setSettings(@NotNull ConfigLoader.ConfigPair config);

    default @NotNull GeneralSettings getGeneralConfig() {
        return this.getSettings().general();
    }

    default @NotNull FormatSettings getFormatConfig() {
        return this.getSettings().format();
    }

    @NotNull BridgeLifecycle getLifecycle();

    default @NotNull DiscordBot getBot() {
        return this.getLifecycle().bot();
    }

    @NotNull ChatSystem getChatSystem();

    @NotNull FirstJoinPlayerHolder getFirstJoinPlayers();

    @NotNull PlatformInfo getPlatformInfo();

    default boolean load() {
        try {
            this.setSettings(ConfigLoader.load(this.getDataDirectory()));
            return true;
        } catch (IOException e) {
            this.getWrappedLogger().error("Could not load plugin configuration", e);
            return false;
        }
    }

    default boolean reload() {
        final ConfigLoader.ConfigPair next;
        try {
            next = ConfigLoader.load(this.getDataDirectory());
        } catch (IOException e) {
            this.getWrappedLogger().error("Could not reload plugin configuration", e);
            return false;
        }

        return this.getLifecycle().restart(next);
    }

    default boolean enable() {
        return this.getLifecycle().start();
    }

    default void disable() {
        this.getLifecycle().stop();
    }

    default void loginToDiscord() {
        this.getLifecycle().login();
    }

    void registerCommands();

    void unregisterCommands();

    void registerListeners();

    void unregisterListeners();

    default boolean enabled() {
        return this.getLifecycle().state() == BridgeLifecycle.State.RUNNING;
    }

    void registerLuckPermsFirstJoinListener();

    void unregisterLuckPermsFirstJoinListener();

    @NotNull String serializeColor(@NotNull Color color);
}
