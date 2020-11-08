package net.okocraft.discordchatbridge.config;

import com.github.siroshun09.configapi.bungee.BungeeYaml;
import com.github.siroshun09.configapi.bungee.BungeeYamlFactory;
import com.github.siroshun09.configapi.common.yaml.Yaml;
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
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GeneralConfig {

    private final DiscordChatBridge plugin;
    private final Yaml yaml;
    private List<LinkedChannel> linkedChannels = Collections.emptyList();

    public GeneralConfig(@NotNull DiscordChatBridge plugin) throws IOException {
        this.plugin = plugin;
        this.yaml = BungeeYamlFactory.load(plugin, "config.yml");

        loadChannels();
    }

    public void reload() throws IOException {
        yaml.reload();
        loadChannels();
    }

    @NotNull
    public String getToken() {
        return yaml.getString("discord.token");
    }

    @NotNull
    public OnlineStatus getStatus() {
        String value = yaml.getString("discord.status", "ONLINE");
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

        String typeValue = yaml.getString("discord.activity.type", "DEFAULT");
        try {
            type = Activity.ActivityType.valueOf(typeValue);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid online status: " + typeValue);
            return null;
        }

        String game = yaml.getString("discord.activity.game", "%count% players in server");
        String url = yaml.getString("discord.activity.url");

        return Activity.of(type, game.replace("%count%", String.valueOf(plugin.getProxy().getOnlineCount())), url);
    }

    public Optional<Long> getDiscordChannel(@NotNull String channelName) {
        return linkedChannels.stream()
                .filter(c -> c.getChannelName().equals(channelName))
                .map(LinkedChannel::getId)
                .findFirst();
    }

    public Optional<String> getLunaChatChannel(long id) {
        return linkedChannels.stream()
                .filter(c -> c.getId() == id)
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
        return yaml.getInteger("chat-max-length", 150);
    }

    @NotNull
    public String getSourceName() {
        return yaml.getString("discord-source-name", "Dis");
    }

    @NotNull
    public String getPrefix(@NotNull Member member) {
        String prefix = null;

        for (Role role : member.getRoles().stream().sorted().collect(Collectors.toList())) {
            String temp = yaml.getString("role-prefix." + role.getIdLong());

            if (!temp.isEmpty()) {
                prefix = temp.replace("%color%", ChatColor.of(getColor(role)).toString());
            }
        }

        if (prefix == null) {
            prefix = yaml.getString("role-prefix.default", "&f* ");
        }

        return prefix;
    }

    private void loadChannels() {
        BungeeYaml bungeeYaml = (BungeeYaml) yaml;
        Configuration section = bungeeYaml.getConfig().getSection("channels");

        if (section != null) {
            linkedChannels = new LinkedList<>();

            for (String key : section.getKeys()) {
                linkedChannels.add(new LinkedChannel(key, section.getLong(key, 0)));
            }
        }
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
