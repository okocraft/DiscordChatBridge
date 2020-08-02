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
        getLogger().info("Loading configurations...");
        config = new GeneralConfig(this);
        formatConfig = new FormatConfig(this);

        if (!config.isLoaded()) {
            getLogger().severe("Could not reload config.yml.");
            return;
        }

        if (!formatConfig.isLoaded()) {
            getLogger().severe("Could not reload format.yml.");
            return;
        }

        try {
            bot = DiscordBot.login(this);
            getLogger().info("Logged in to Discord.");
        } catch (Throwable e) {
            getLogger().log(Level.SEVERE, "Could not log in to discord.", e);
            return;
        }

        getProxy().getPluginManager().registerListener(this, new ServerListener(this));
        getProxy().getPluginManager().registerListener(this, new LunaChatListener(this));
        getProxy().getPluginManager().registerCommand(this, new ReloadCommand(this));

        getLogger().info("Successfully enabled.");
    }

    @Override
    public void onDisable() {
        getProxy().getPluginManager().unregisterCommands(this);
        getProxy().getPluginManager().unregisterListeners(this);
        bot.shutdown();

        getLogger().info("Successfully disabled.");
    }

    public boolean reload() {
        getLogger().info("Reloading DiscordChatBridge...");

        getProxy().getPluginManager().unregisterCommands(this);
        getProxy().getPluginManager().unregisterListeners(this);

        bot.shutdown();

        getLogger().info("Reloading configurations...");
        if (!config.reload()) {
            getLogger().severe("Could not reload config.yml.");
            return false;
        }

        if (!formatConfig.reload()) {
            getLogger().severe("Could not reload format.yml.");
            return false;
        }

        try {
            bot = DiscordBot.login(this);
            getLogger().info("Logged in to Discord.");
        } catch (Throwable e) {
            getLogger().log(Level.SEVERE, "Could not log in to discord.", e);
            return false;
        }

        getProxy().getPluginManager().registerListener(this, new ServerListener(this));
        getProxy().getPluginManager().registerListener(this, new LunaChatListener(this));
        getProxy().getPluginManager().registerCommand(this, new ReloadCommand(this));

        getLogger().info("Successfully reloaded.");
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
}
