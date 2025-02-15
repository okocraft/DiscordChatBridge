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

package net.okocraft.discordchatbridge.platform.bukkit;

import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.okocraft.discordchatbridge.DiscordBot;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.chat.ChatSystem;
import net.okocraft.discordchatbridge.command.ReloadCommand;
import net.okocraft.discordchatbridge.listener.luckperms.FirstJoinListener;
import net.okocraft.discordchatbridge.logger.JavaLogger;
import net.okocraft.discordchatbridge.logger.LoggerWrapper;
import net.okocraft.discordchatbridge.platform.PlatformInfo;
import net.okocraft.discordchatbridge.util.ColorSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.nio.file.Path;

public class DiscordChatBridgeBukkit extends JavaPlugin implements DiscordChatBridgePlugin {

    private final LoggerWrapper wrappedLogger = new JavaLogger(this.getLogger());
    private final BukkitPlatform bukkitPlatform = new BukkitPlatform(this);
    private final ReloadCommand reloadCommand = new ReloadCommand(this);
    private final YamlConfiguration generalConfig = YamlConfiguration.create(this.getDataDirectory().resolve("config.yml"));
    private final YamlConfiguration formatConfig = YamlConfiguration.create(this.getDataDirectory().resolve("format.yml"));

    private DiscordBot bot;
    private ChatSystem chatSystem;
    private FirstJoinListener firstJoinListener;
    private boolean isEnabled;

    @Override
    public void onEnable() {
        this.load();

        if (this.isLunaChatEnabled()) {
            this.chatSystem = new BukkitLunaChatSystem();
        } else {
            this.chatSystem = new PaperChatSystem();
        }

        this.isEnabled = this.enable();
    }

    @Override
    public void onDisable() {
        this.disable();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        this.reloadCommand.processCommand(sender::hasPermission, sender::sendMessage);
        return true;
    }

    @Override
    public @NotNull LoggerWrapper getWrappedLogger() {
        return this.wrappedLogger;
    }

    @Override
    public @NotNull Path getDataDirectory() {
        return this.getDataFolder().toPath();
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
        return this.bukkitPlatform;
    }

    @Override
    public void loginToDiscord() {
        this.bot = DiscordBot.login(this);
    }

    @Override
    public void registerCommands() {
    }

    @Override
    public void unregisterCommands() {
    }

    @Override
    public void registerListeners() {
        this.getServer().getPluginManager().registerEvents(new BukkitServerListener(this), this);

        if (this.isLunaChatEnabled()) {
            this.getServer().getPluginManager().registerEvents(new BukkitLunaChatListener(this), this);
        } else {
            this.getServer().getPluginManager().registerEvents(new PaperChatListener(this),this);
        }
    }

    @Override
    public void unregisterListeners() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public boolean enabled() {
        return this.isEnabled;
    }

    @Override
    public void registerLuckPermsFirstJoinListener() {
        if (this.getServer().getPluginManager().getPlugin("LuckPerms") != null) {
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

    private boolean isLunaChatEnabled() {
        return this.getServer().getPluginManager().getPlugin("LunaChat") != null;
    }
}
