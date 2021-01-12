/*
 *     Copyright (c) 2020 Okocraft
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

import net.md_5.bungee.api.plugin.Plugin;
import net.okocraft.discordchatbridge.command.ReloadCommand;
import net.okocraft.discordchatbridge.config.FormatConfig;
import net.okocraft.discordchatbridge.config.GeneralConfig;
import net.okocraft.discordchatbridge.data.ReceivedMessages;
import net.okocraft.discordchatbridge.listener.LunaChatListener;
import net.okocraft.discordchatbridge.listener.ServerListener;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Level;

public class DiscordChatBridge extends Plugin {

    private final ReceivedMessages receivedMessages = new ReceivedMessages();
    private GeneralConfig config;
    private FormatConfig formatConfig;
    private DiscordBot bot;

    @Override
    public void onEnable() {
       if (reload()) {
           getLogger().info("Successfully enabled.");
       }
    }

    @Override
    public void onDisable() {
        if (bot != null) {
            bot.shutdown();
            unregisterCommandAndListeners();
            receivedMessages.clear();
        }

        getLogger().info("Successfully disabled.");
    }

    public boolean reload() {
        if (bot != null) {
            bot.shutdown();
            unregisterCommandAndListeners();
        }

        try {
            loadConfigurations();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load files.", e);
            return false;
        }

        try {
            bot = DiscordBot.login(this);
        } catch (Throwable e) {
            getLogger().warning("Could not login to discord.");
            getLogger().warning("Please check the token of discord bot.");
            return false;
        }

        receivedMessages.clear();
        registerCommandAndListeners();
        return true;
    }

    @NotNull
    public GeneralConfig getGeneralConfig() {
        return config;
    }

    @NotNull
    public FormatConfig getFormatConfig() {
        return formatConfig;
    }

    @NotNull
    public DiscordBot getBot() {
        return bot;
    }

    @NotNull
    public ReceivedMessages getReceivedMessages() {
        return receivedMessages;
    }

    private void loadConfigurations() throws IOException {
        if (config == null) {
            config = new GeneralConfig(this);
        } else {
            config.reload();
        }

        if (formatConfig == null) {
            formatConfig = new FormatConfig(this);
        } else {
            formatConfig.reload();
        }
    }

    private void registerCommandAndListeners() {
        getProxy().getPluginManager().registerListener(this, new ServerListener(this));
        getProxy().getPluginManager().registerListener(this, new LunaChatListener(this));
        getProxy().getPluginManager().registerCommand(this, new ReloadCommand(this));
    }

    private void unregisterCommandAndListeners() {
        getProxy().getPluginManager().unregisterCommands(this);
        getProxy().getPluginManager().unregisterListeners(this);
    }
}
