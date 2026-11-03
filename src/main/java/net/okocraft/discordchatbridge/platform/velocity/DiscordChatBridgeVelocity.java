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

import net.okocraft.discordchatbridge.config.ConfigLoader;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
//import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.okocraft.discordchatbridge.BridgeLifecycle;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.chat.ChatSystem;
import net.okocraft.discordchatbridge.listener.luckperms.FirstJoinListener;
import net.okocraft.discordchatbridge.logger.LoggerWrapper;
import net.okocraft.discordchatbridge.logger.Slf4jLogger;
import net.okocraft.discordchatbridge.platform.PlatformInfo;
import net.okocraft.discordchatbridge.util.ColorSerializer;
import net.okocraft.discordchatbridge.util.FirstJoinPlayerHolder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.awt.Color;
import java.nio.file.Path;

//@Plugin(id = "discordchatbridge", name = "DiscordChatBridge", version = "2.5",
//        description = "A Paper/Velocity plugin that connects Vanilla chat or LunaChat and Discord",
//        authors = {"Siroshun09"})
public class DiscordChatBridgeVelocity implements DiscordChatBridgePlugin {

    private final ProxyServer server;
    private final LoggerWrapper wrappedLogger;
    private final Path dataDirectory;

    private volatile ConfigLoader.ConfigPair config;
    private final VelocityPlatform velocityPlatform;

    private final BridgeLifecycle lifecycle = new BridgeLifecycle(this);
    private ChatSystem chatSystem;
    private FirstJoinListener firstJoinListener;
    private VelocityServerListener serverListener;
    private VelocityLunaChatListener lunaChatListener;
    private volatile FirstJoinPlayerHolder firstJoinPlayers = new FirstJoinPlayerHolder();

    @Inject
    public DiscordChatBridgeVelocity(@NotNull ProxyServer server, @NotNull Logger logger,
                                     @DataDirectory Path dataDirectory) {
        this.server = server;
        this.wrappedLogger = new Slf4jLogger(logger);
        this.dataDirectory = dataDirectory;

        this.velocityPlatform = new VelocityPlatform(server, this);
    }

    @Subscribe
    public void onEnable(ProxyInitializeEvent e) {
        if (!this.load()) {
            return;
        }

        if (this.server.getPluginManager().isLoaded("lunachat")) {
            this.chatSystem = new VelocityLunaChatSystem();
        }

        this.enable();
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent e) {
        this.disable();
    }

    @Override
    public @NotNull LoggerWrapper getWrappedLogger() {
        return this.wrappedLogger;
    }

    @Override
    public @NotNull Path getDataDirectory() {
        return this.dataDirectory;
    }

    @Override
    public @NotNull ConfigLoader.ConfigPair getSettings() {
        return this.config;
    }

    @Override
    public void setSettings(@NotNull ConfigLoader.ConfigPair config) {
        this.config = config;
    }

    @Override
    public @NotNull BridgeLifecycle getLifecycle() {
        return this.lifecycle;
    }

    @Override
    public @NotNull ChatSystem getChatSystem() {
        return this.chatSystem;
    }

    @Override
    public @NotNull PlatformInfo getPlatformInfo() {
        return this.velocityPlatform;
    }

    @Override
    public void registerCommands() {
        this.server.getCommandManager().register(
                this.server.getCommandManager().metaBuilder("dcbreload").build(),
                new VelocityReloadCommand(this)
        );
    }

    @Override
    public void unregisterCommands() {
        this.server.getCommandManager().unregister("dcbreload");
    }

    @Override
    public void registerListeners() {
        if (this.server.getPluginManager().isLoaded("lunachat")) {
            this.lunaChatListener = new VelocityLunaChatListener(this);
            this.server.getEventManager().register(this, this.lunaChatListener);
        }
        this.serverListener = new VelocityServerListener(this);
        this.server.getEventManager().register(this, this.serverListener);
    }

    @Override
    public void unregisterListeners() {
        RuntimeException failure = null;
        if (this.serverListener != null) {
            try {
                this.server.getEventManager().unregisterListener(this, this.serverListener);
                this.serverListener = null;
            } catch (RuntimeException e) {
                failure = e;
            }
        }
        if (this.lunaChatListener != null) {
            try {
                this.server.getEventManager().unregisterListener(this, this.lunaChatListener);
                this.lunaChatListener = null;
            } catch (RuntimeException e) {
                if (failure == null) {
                    failure = e;
                } else {
                    failure.addSuppressed(e);
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    @Override
    public @NotNull FirstJoinPlayerHolder getFirstJoinPlayers() {
        return this.firstJoinPlayers;
    }

    @Override
    public void registerLuckPermsFirstJoinListener() {
        if (this.server.getPluginManager().getPlugin("luckperms").isPresent()) {
            this.firstJoinPlayers = new FirstJoinPlayerHolder();
            this.firstJoinListener = new FirstJoinListener(this);
        }
    }

    @Override
    public void unregisterLuckPermsFirstJoinListener() {
        if (this.firstJoinListener != null) {
            this.firstJoinListener.unsubscribe();
            this.firstJoinListener = null;
        }
    }

    @Override
    public @NotNull String serializeColor(@NotNull Color color) {
        return ColorSerializer.adventure(color);
    }
}