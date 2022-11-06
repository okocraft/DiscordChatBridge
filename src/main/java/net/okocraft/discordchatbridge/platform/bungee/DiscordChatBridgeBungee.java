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

import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.md_5.bungee.api.plugin.Plugin;
import net.okocraft.discordchatbridge.DiscordBot;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.chat.ChatSystem;
import net.okocraft.discordchatbridge.database.LinkManager;
import net.okocraft.discordchatbridge.listener.luckperms.FirstJoinListener;
import net.okocraft.discordchatbridge.logger.JavaLogger;
import net.okocraft.discordchatbridge.logger.LoggerWrapper;
import net.okocraft.discordchatbridge.platform.DiscordUserChecker;
import net.okocraft.discordchatbridge.platform.PlatformInfo;
import net.okocraft.discordchatbridge.util.ColorSerializer;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.nio.file.Path;

public class DiscordChatBridgeBungee extends Plugin implements DiscordChatBridgePlugin {

    private final LoggerWrapper wrappedLogger = new JavaLogger(getLogger());
    private final BungeePlatform bungeePlatform = new BungeePlatform(this);
    private final YamlConfiguration generalConfig = YamlConfiguration.create(getDataDirectory().resolve("config.yml"));
    private final YamlConfiguration formatConfig = YamlConfiguration.create(getDataDirectory().resolve("format.yml"));
    private final BungeeDiscordUserChecker discordUserChecker = new BungeeDiscordUserChecker(this);

    private DiscordBot bot;
    private ChatSystem chatSystem;
    private LinkManager linkManager;
    private FirstJoinListener firstJoinListener;
    private boolean isEnabled;

    @Override
    public void onEnable() {
        load();

        if (isLunaChatEnabled()) {
            chatSystem = new BungeeLunaChatSystem();
        } else {
            chatSystem = new BungeeChatSystem();
        }

        isEnabled = enable();
    }

    @Override
    public void onDisable() {
        disable();
    }

    @Override
    public @NotNull LoggerWrapper getWrappedLogger() {
        return wrappedLogger;
    }

    @Override
    public @NotNull Path getDataDirectory() {
        return getDataFolder().toPath();
    }

    public @NotNull YamlConfiguration getGeneralConfig() {
        return generalConfig;
    }

    public @NotNull YamlConfiguration getFormatConfig() {
        return formatConfig;
    }

    public @NotNull DiscordBot getBot() {
        return bot;
    }

    @Override
    public @NotNull ChatSystem getChatSystem() {
        return chatSystem;
    }

    @Override
    public @NotNull PlatformInfo getPlatformInfo() {
        return bungeePlatform;
    }

    @Override
    public @NotNull LinkManager getLinkManager() {
        return linkManager;
    }

    @Override
    public void setLinkManager(LinkManager linkManager) {
        this.linkManager = linkManager;
    }

    @Override
    public void loginToDiscord() {
        bot = DiscordBot.login(this);
    }

    @Override
    public void registerCommands() {
        getProxy().getPluginManager().registerCommand(this, new BungeeCommand(this));
    }

    @Override
    public void unregisterCommands() {
        getProxy().getPluginManager().unregisterCommands(this);
    }

    @Override
    public void registerListeners() {
        getProxy().getPluginManager().registerListener(this, new BungeeServerListener(this));

        if (isLunaChatEnabled()) {
            getProxy().getPluginManager().registerListener(this, new BungeeLunaChatListener(this));
        } else {
            getProxy().getPluginManager().registerListener(this, new BungeeChatListener(this));
        }
    }

    @Override
    public void unregisterListeners() {
        getProxy().getPluginManager().unregisterListeners(this);
    }

    @Override
    public boolean enabled() {
        return isEnabled;
    }

    @Override
    public void registerLuckPermsFirstJoinListener() {
        if (getProxy().getPluginManager().getPlugin("LuckPerms") != null) {
            firstJoinListener = new FirstJoinListener(this);
        }
    }

    @Override
    public void unregisterLuckPermsFirstJoinListener() {
        if (firstJoinListener != null) {
            firstJoinListener.unsubscribe();
        }
    }

    @Override
    public @NotNull String serializeColor(@NotNull Color color) {
        return ColorSerializer.bungeecord(color);
    }

    private boolean isLunaChatEnabled() {
        return getProxy().getPluginManager().getPlugin("LunaChat") != null;
    }

    @Override
    public @NotNull DiscordUserChecker getDiscordUserChecker() {
        return discordUserChecker;
    }
}
