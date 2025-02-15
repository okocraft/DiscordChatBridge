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

import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
//import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.okocraft.discordchatbridge.DiscordBot;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.chat.ChatSystem;
import net.okocraft.discordchatbridge.listener.luckperms.FirstJoinListener;
import net.okocraft.discordchatbridge.logger.LoggerWrapper;
import net.okocraft.discordchatbridge.logger.Slf4jLogger;
import net.okocraft.discordchatbridge.platform.PlatformInfo;
import net.okocraft.discordchatbridge.util.ColorSerializer;
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

    private final YamlConfiguration generalConfig;
    private final YamlConfiguration formatConfig;
    private final VelocityPlatform velocityPlatform;

    private DiscordBot bot;
    private ChatSystem chatSystem;
    private FirstJoinListener firstJoinListener;
    private boolean isEnabled;

    @Inject
    public DiscordChatBridgeVelocity(@NotNull ProxyServer server, @NotNull Logger logger,
                                     @DataDirectory Path dataDirectory) {
        this.server = server;
        this.wrappedLogger = new Slf4jLogger(logger);
        this.dataDirectory = dataDirectory;

        this.generalConfig = YamlConfiguration.create(this.getDataDirectory().resolve("config.yml"));
        this.formatConfig = YamlConfiguration.create(this.getDataDirectory().resolve("format.yml"));
        this.velocityPlatform = new VelocityPlatform(server, this.formatConfig);
    }

    @Subscribe
    public void onEnable(ProxyInitializeEvent e) {
        this.load();

        this.chatSystem = new SnapLunaChatSystemIntegrator();
        SnapLunaChatListener.init(this);

        this.isEnabled = this.enable();
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

    public @NotNull YamlConfiguration getGeneralConfig() {
        return this.generalConfig;
    }

    public @NotNull YamlConfiguration getFormatConfig() {
        return this.formatConfig;
    }

    public @NotNull DiscordBot getBot() {
        return this.bot;
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
    public void loginToDiscord() {
        this.bot = DiscordBot.login(this);
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
        this.server.getEventManager().register(this, new VelocityServerListener(this));
    }

    @Override
    public void unregisterListeners() {
        this.server.getEventManager().unregisterListeners(this);
    }

    @Override
    public boolean enabled() {
        return this.isEnabled;
    }

    @Override
    public void registerLuckPermsFirstJoinListener() {
        if (this.server.getPluginManager().getPlugin("LuckPerms").isPresent()) {
            this.firstJoinListener = new FirstJoinListener(this);
        }
    }

    @Override
    public void unregisterLuckPermsFirstJoinListener() {
        if (this.firstJoinListener != null) {
            this.firstJoinListener.unsubscribe();
        }
    }

    @Override
    public @NotNull String serializeColor(@NotNull Color color) {
        return ColorSerializer.adventure(color);
    }
}