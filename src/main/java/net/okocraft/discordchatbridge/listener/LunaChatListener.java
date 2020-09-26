package net.okocraft.discordchatbridge.listener;

import com.github.ucchyocean.lc3.bungee.event.LunaChatBungeeChannelMessageEvent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.okocraft.discordchatbridge.DiscordChatBridge;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class LunaChatListener implements Listener {

    private final DiscordChatBridge plugin;

    public LunaChatListener(@NotNull DiscordChatBridge plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(@NotNull LunaChatBungeeChannelMessageEvent e) {
        String message = e.getOriginalMessage();

        if (message.isEmpty()) {
            return;
        }

        Optional<Long> id = plugin.getGeneralConfig().getDiscordChannel(e.getChannel().getName());

        if (id.isEmpty()) {
            return;
        }

        if (plugin.getIgnoringMessages().contains(message)) {
            plugin.getIgnoringMessages().remove(message);
            return;
        }

        message = plugin.getFormatConfig().getDiscordChatFormat()
                .replace("%player%", e.getMember().getName())
                .replace("%display_name%", e.getMember().getDisplayName())
                .replace("%message%", message);

        if (message.isEmpty()) {
            return;
        }

        if (message.length() < 2000) {
            plugin.getBot().sendMessage(id.get(), ChatColor.stripColor(message));
        } else {
            plugin.getLogger().warning("The message is too long! " + message);
        }
    }
}
