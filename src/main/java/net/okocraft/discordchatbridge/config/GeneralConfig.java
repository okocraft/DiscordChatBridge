package net.okocraft.discordchatbridge.config;

import com.github.siroshun09.configapi.bungee.BungeeConfig;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;
import net.okocraft.discordchatbridge.DiscordChatBridge;
import net.okocraft.discordchatbridge.LinkedChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GeneralConfig extends BungeeConfig {

    private final DiscordChatBridge plugin;
    private final List<LinkedChannel> linkedChannels = new LinkedList<>();

    public GeneralConfig(@NotNull DiscordChatBridge plugin) {
        super(plugin, "config.yml", true);

        this.plugin = plugin;
    }

    @Override
    public boolean load() {
        if (super.load()) {
            Configuration section = getConfig().getSection("channels");

            if (section != null) {
                linkedChannels.clear();

                for (String key : section.getKeys()) {
                    linkedChannels.add(new LinkedChannel(key, section.getLong(key, 0)));
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean reload() {
        return load();
    }

    @NotNull
    public String getToken() {
        return getString("discord.token");
    }

    @NotNull
    public OnlineStatus getStatus() {
        String value = getString("discord.status", "ONLINE");
        try {
            return OnlineStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid online status: " + value);
            return OnlineStatus.ONLINE;
        }
    }

    @Nullable
    public Activity getActivity() {
        Activity.ActivityType type;

        String typeValue = getString("discord.activity.type", "DEFAULT");
        try {
            type = Activity.ActivityType.valueOf(typeValue);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid online status: " + typeValue);
            return null;
        }

        String game = getString("discord.activity.game", "%count% players in server");
        String url = getString("discord.activity.url");

        return Activity.of(type, game.replace("%count%", String.valueOf(plugin.getProxy().getOnlineCount())), url);
    }

    public Optional<Long> getDiscordChannel(@NotNull String channelName) {
        return linkedChannels.stream().
                filter(c -> c.getChannelName().equals(channelName))
                .map(LinkedChannel::getId)
                .findFirst();
    }

    public Optional<String> getLunaChatChannel(long id) {
        return linkedChannels.stream().
                filter(c -> c.getId() == id)
                .map(LinkedChannel::getChannelName)
                .findFirst();
    }

    @Nullable
    public LinkedChannel getSystemChannel() {
        if (linkedChannels.isEmpty()) {
            return null;
        } else {
            return linkedChannels.get(0);
        }
    }

    public int getChatMaxLength() {
        return getInt("chat-max-length", 150);
    }

    @NotNull
    public String getSourceName() {
        return getString("discord-source-name", "Dis");
    }

    @NotNull
    public String getPrefix(@NotNull Member member) {
        String prefix = null;

        for (Role role : member.getRoles().stream().sorted().collect(Collectors.toList())) {
            String temp = getString("role-prefix." + role.getIdLong());

            if (!temp.isEmpty()) {
                prefix = temp.replace("%color%", ChatColor.of(getColor(role)).toString());
            }
        }

        if (prefix == null) {
            prefix = getString("role-prefix.default", "&f* ");
        }

        return prefix;
    }

    @NotNull
    private Color getColor(@NotNull Role role) {
        Color color = role.getColor();

        if (color == null) {
            color = new Color(Role.DEFAULT_COLOR_RAW);
        }

        return color;
    }
}
