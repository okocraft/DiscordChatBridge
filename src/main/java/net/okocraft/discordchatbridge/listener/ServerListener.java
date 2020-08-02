package net.okocraft.discordchatbridge.listener;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.okocraft.discordchatbridge.DiscordChatBridge;
import net.okocraft.discordchatbridge.LinkedChannel;
import org.jetbrains.annotations.NotNull;

public class ServerListener implements Listener {

    private final DiscordChatBridge plugin;

    public ServerListener(@NotNull DiscordChatBridge plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDisconnect(@NotNull PlayerDisconnectEvent e) {
        String format = plugin.getFormatConfig().getServerLeftFormat();

        if (!format.isEmpty()) {
            sendMessage(replace(format, e.getPlayer()));
        }

        plugin.getBot().updateGame();
    }

    @EventHandler
    public void onJoinOrSwitch(@NotNull ServerSwitchEvent e) {
        if (e.getFrom() == null) {
            String format = plugin.getFormatConfig().getServerJoinFormat();

            if (!format.isEmpty()) {
                sendMessage(replace(format, e.getPlayer()));
            }
        } else {
            String format = plugin.getFormatConfig().getServerSwitchFormat();

            if (!format.isEmpty()) {
                sendMessage(replace(format, e.getPlayer(), e.getPlayer().getServer().getInfo()));
            }
        }

        plugin.getBot().updateGame();
    }

    private void sendMessage(@NotNull String message) {
        LinkedChannel system = plugin.getGeneralConfig().getSystemChannel();

        if (system != null) {
            plugin.getBot().sendMessage(system.getId(), message);
        }
    }

    @NotNull
    private String replace(@NotNull String format, @NotNull ProxiedPlayer player) {
        return format
                .replace("%player%", player.getName())
                .replace("%display_name%", player.getDisplayName());
    }

    @NotNull
    private String replace(@NotNull String format, @NotNull ProxiedPlayer player, @NotNull ServerInfo server) {
        return replace(format, player).replace("%server%", server.getName());
    }
}
