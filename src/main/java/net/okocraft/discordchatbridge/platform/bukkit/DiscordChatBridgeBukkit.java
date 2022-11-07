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

package net.okocraft.discordchatbridge.platform.bukkit;

import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.okocraft.discordchatbridge.DiscordBot;
import net.okocraft.discordchatbridge.DiscordChatBridgePlugin;
import net.okocraft.discordchatbridge.chat.ChatSystem;
import net.okocraft.discordchatbridge.command.DiscordChatBridgeCommand;
import net.okocraft.discordchatbridge.database.LinkManager;
import net.okocraft.discordchatbridge.listener.luckperms.FirstJoinListener;
import net.okocraft.discordchatbridge.logger.JavaLogger;
import net.okocraft.discordchatbridge.logger.LoggerWrapper;
import net.okocraft.discordchatbridge.platform.DiscordUserChecker;
import net.okocraft.discordchatbridge.platform.PlatformInfo;
import net.okocraft.discordchatbridge.util.ColorSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiscordChatBridgeBukkit extends JavaPlugin implements DiscordChatBridgePlugin {

    private final LoggerWrapper wrappedLogger = new JavaLogger(getLogger());
    private final BukkitPlatform bukkitPlatform = new BukkitPlatform(this);
    private final YamlConfiguration generalConfig = YamlConfiguration.create(getDataDirectory().resolve("config.yml"));
    private final YamlConfiguration formatConfig = YamlConfiguration.create(getDataDirectory().resolve("format.yml"));
    private final BukkitDiscordUserChecker discordUserChecker = new BukkitDiscordUserChecker(this);

    private DiscordBot bot;
    private ChatSystem chatSystem;
    private FirstJoinListener firstJoinListener;
    private LinkManager linkManager;
    private boolean isEnabled;

    @Override
    public void onEnable() {
        load();

        if (isLunaChatEnabled()) {
            chatSystem = new BukkitLunaChatSystem();
        } else {
            chatSystem = PaperChecker.IS_PAPER ? new PaperChatSystem() : new BukkitChatSystem();
        }

        isEnabled = enable();
    }

    @Override
    public void onDisable() {
        disable();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        DiscordChatBridgeCommand.onCommand(this, new BukkitCommandSender(sender), args);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("reload", "link")
                    .filter(sub -> sub.startsWith(args[0]))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
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
        return bukkitPlatform;
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
    }

    @Override
    public void unregisterCommands() {
    }

    @Override
    public void registerListeners() {
        getServer().getPluginManager().registerEvents(new BukkitServerListener(this), this);

        if (isLunaChatEnabled()) {
            getServer().getPluginManager().registerEvents(new BukkitLunaChatListener(this), this);
        } else {
            getServer().getPluginManager().registerEvents(
                    PaperChecker.IS_PAPER ? new PaperChatListener(this) : new BukkitChatListener(this),
                    this
            );
        }
    }

    @Override
    public void unregisterListeners() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public boolean enabled() {
        return isEnabled;
    }

    @Override
    public void registerLuckPermsFirstJoinListener() {
        if (getServer().getPluginManager().getPlugin("LuckPerms") != null) {
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
        return PaperChecker.IS_PAPER ? ColorSerializer.adventure(color) : ColorSerializer.bungeecord(color);
    }

    private boolean isLunaChatEnabled() {
        return getServer().getPluginManager().getPlugin("LunaChat") != null;
    }

    @Override
    public @NotNull DiscordUserChecker getDiscordUserChecker() {
        return discordUserChecker;
    }
}
