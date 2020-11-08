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

    private GeneralConfig config;
    private FormatConfig formatConfig;
    private DiscordBot bot;
    private ReceivedMessages receivedMessages;

    @Override
    public void onEnable() {
        try {
            loadConfigurations();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load files.", e);
            return;
        }

        try {
            bot = DiscordBot.login(this);
        } catch (Throwable e) {
            getLogger().log(Level.SEVERE, "Could not log in to discord.", e);
            return;
        }

        receivedMessages = new ReceivedMessages();
        registerCommandAndListeners();
        getLogger().info("Successfully enabled.");
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
            getLogger().log(Level.SEVERE, "Could not log in to discord.", e);
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
