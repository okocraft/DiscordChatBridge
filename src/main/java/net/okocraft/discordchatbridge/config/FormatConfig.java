package net.okocraft.discordchatbridge.config;

import com.github.siroshun09.configapi.bungee.BungeeConfig;
import net.okocraft.discordchatbridge.DiscordChatBridge;
import org.jetbrains.annotations.NotNull;

public class FormatConfig extends BungeeConfig {

    public FormatConfig(@NotNull DiscordChatBridge plugin) {
        super(plugin, "format.yml", true);
    }

    @NotNull
    public String getDiscordChatFormat() {
        return getString("server.chat", "%player%: %message%");
    }

    @NotNull
    public String getServerJoinFormat() {
        return getString("server.join", ":heavy_plus_sign: **%player%** joined the server.");
    }

    @NotNull
    public String getServerLeftFormat() {
        return getString("server.leave", ":heavy_minus_sign: **%player%** left the server.");
    }

    @NotNull
    public String getServerSwitchFormat() {
        return getString("server.switch", ":heavy_plus_sign: **%player%** moved to **%server%**");
    }
    @NotNull
    public String getPlayerListTop() {
        return getString("server.player-list.top", "**===== Player List (%count%) =====**");
    }

    @NotNull
    public String getPlayerListFormat() {
        return getString("server.player-list.list", "%server%: %players%");
    }
    @NotNull
    public String getNoPermissionMessage() {
        return getString("command.no-permission", "&c* You have no permission: %perm%");
    }

    @NotNull
    public String getReloadingMessage() {
        return getString("command.reload.start", "&7* Reloading DiscordChatBridge...");
    }

    @NotNull
    public String getReloadSuccessMessage() {
        return getString("command.reload.success", "&7* The reload was successful.");
    }

    @NotNull
    public String getReloadFailureMessage() {
        return getString("command.reload.failure", "&c* Failed to reload. Please check the console.");
    }
}
