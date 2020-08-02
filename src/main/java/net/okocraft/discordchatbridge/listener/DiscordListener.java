package net.okocraft.discordchatbridge.listener;

import com.github.ucchyocean.lc3.LunaChatBungee;
import com.github.ucchyocean.lc3.channel.Channel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.okocraft.discordchatbridge.DiscordChatBridge;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.stream.Collectors;

public class DiscordListener extends ListenerAdapter {

    private final DiscordChatBridge plugin;

    public DiscordListener(@NotNull DiscordChatBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getMember() == null) {
            return;
        }

        String message = event.getMessage().getContentStripped();

        if (message.startsWith("!playerlist")) {
            onPlayerListCommand(event.getTextChannel());
            return;
        }

        if (plugin.getGeneralConfig().getChatMaxLength() < message.length()) {
            plugin.getBot().addReaction(event.getMessage(), "U+FE0F");
            return;
        }

        Optional<String> channelName = plugin.getGeneralConfig().getLunaChatChannel(event.getChannel().getIdLong());

        if (channelName.isEmpty()) {
            return;
        }

        Channel channel = LunaChatBungee.getInstance().getLunaChatAPI().getChannel(channelName.get());

        if (channel == null) {
            return;
        }

        channel.chatFromOtherSource(
                plugin.getGeneralConfig().getPrefix(event.getMember()) + event.getMember().getNickname(),
                plugin.getGeneralConfig().getSourceName(),
                event.getMessage().getContentStripped()
        );
    }

    private void onPlayerListCommand(@NotNull TextChannel channel) {
        StringBuilder builder = new StringBuilder(
                plugin.getFormatConfig().getPlayerListTop()
                        .replace("%count%", String.valueOf(plugin.getProxy().getOnlineCount()))
        );

        builder.append("\\r```\\r");

        for (ServerInfo server : plugin.getProxy().getServers().values()) {
            builder.append(plugin.getFormatConfig().getPlayerListFormat()
                    .replace("%server%", server.getName())
                    .replace("%players%", server.getPlayers().stream()
                            .map(ProxiedPlayer::getName)
                            .sorted().collect(Collectors.joining(", ")))

            );

            builder.append("\\r");
        }

        builder.append("```");

        if (channel.canTalk()) {
            plugin.getBot().sendMessage(channel, builder.toString());
        }
    }
}
