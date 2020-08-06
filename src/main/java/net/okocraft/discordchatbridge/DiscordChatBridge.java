package net.okocraft.discordchatbridge;

import net.md_5.bungee.api.plugin.Plugin;
import net.okocraft.discordchatbridge.command.ReloadCommand;
import net.okocraft.discordchatbridge.config.FormatConfig;
import net.okocraft.discordchatbridge.config.GeneralConfig;
import net.okocraft.discordchatbridge.listener.LunaChatListener;
import net.okocraft.discordchatbridge.listener.ServerListener;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class DiscordChatBridge extends Plugin {

    private GeneralConfig config;
    private FormatConfig formatConfig;
    private DiscordBot bot;

    @Override
    public void onEnable() {
        if (loadConfigurations() && loginToDiscord()) {
            registerCommandAndListeners();
            getLogger().info("Successfully enabled.");
        }
    }

    @Override
    public void onDisable() {
        if (bot != null) {
            bot.shutdown();
            unregisterCommandAndListeners();
        }

        getLogger().info("Successfully disabled.");
    }

    public boolean reload() {
        if (bot != null) {
            bot.shutdown();
            unregisterCommandAndListeners();
        }

        if (loadConfigurations() && loginToDiscord()) {
            registerCommandAndListeners();
            return true;
        } else {
            return false;
        }
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

    private boolean loadConfigurations() {
        if (config == null) {
            config = new GeneralConfig(this);
        } else {
            config.reload();
        }

        if (!config.isLoaded()) {
            getLogger().severe("Could not load config.yml.");
            return false;
        }

        if (formatConfig == null) {
            formatConfig = new FormatConfig(this);
        } else {
            formatConfig.reload();
        }

        if (!formatConfig.isLoaded()) {
            getLogger().severe("Could not load format.yml.");
            return false;
        }

        return true;
    }

    private boolean loginToDiscord() {
        try {
            bot = DiscordBot.login(this);
            return true;
        } catch (Throwable e) {
            getLogger().log(Level.SEVERE, "Could not log in to discord.", e);
            return false;
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
