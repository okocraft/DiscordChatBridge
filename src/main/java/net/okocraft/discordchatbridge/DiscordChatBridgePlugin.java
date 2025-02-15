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

import com.github.siroshun09.configapi.api.util.ResourceUtils;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.okocraft.discordchatbridge.chat.ChatSystem;
import net.okocraft.discordchatbridge.config.GeneralSettings;
import net.okocraft.discordchatbridge.logger.LoggerWrapper;
import net.okocraft.discordchatbridge.platform.PlatformInfo;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Path;

public interface DiscordChatBridgePlugin {

    @NotNull LoggerWrapper getWrappedLogger();

    @NotNull Path getDataDirectory();

    @NotNull YamlConfiguration getGeneralConfig();

    @NotNull YamlConfiguration getFormatConfig();

    @NotNull DiscordBot getBot();

    @NotNull ChatSystem getChatSystem();

    @NotNull PlatformInfo getPlatformInfo();

    default boolean load() {
        try {
            ResourceUtils.copyFromClassLoaderIfNotExists(
                    this.getClass().getClassLoader(),
                    "config.yml",
                    this.getGeneralConfig().getPath()
            );

            this.getGeneralConfig().load();
        } catch (IOException e) {
            this.getWrappedLogger().error("Could not load config.yml", e);
            return false;
        }

        try {
            ResourceUtils.copyFromClassLoaderIfNotExists(
                    this.getClass().getClassLoader(),
                    "format.yml",
                    this.getFormatConfig().getPath()
            );

            this.getFormatConfig().load();
        } catch (IOException e) {
            this.getWrappedLogger().error("Could not load format.yml", e);
            return false;
        }

        return true;
    }

    default boolean enable() {
        try {
            this.loginToDiscord();
            this.getBot().updateGame();
        } catch (Exception e) {
            this.getWrappedLogger().error("Could not login to Discord", e);
            return false;
        }

        this.registerCommands();
        this.registerListeners();

        if (this.getGeneralConfig().get(GeneralSettings.SEND_FIRST_JOIN_MESSAGE)) {
            this.registerLuckPermsFirstJoinListener();
        }

        return true;
    }

    default void disable() {
        if (this.enabled()) {
            this.getBot().shutdown();
            this.unregisterCommands();
            this.unregisterListeners();
            this.unregisterLuckPermsFirstJoinListener();
        }
    }

    void loginToDiscord();

    void registerCommands();

    void unregisterCommands();

    void registerListeners();

    void unregisterListeners();

    boolean enabled();

    void registerLuckPermsFirstJoinListener();

    void unregisterLuckPermsFirstJoinListener();

    @NotNull String serializeColor(@NotNull Color color);
}
