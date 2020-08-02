package net.okocraft.discordchatbridge.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.okocraft.discordchatbridge.DiscordChatBridge;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand extends Command {

    private final static String PERM = "discordchatbridge.reload";

    private final DiscordChatBridge plugin;

    public ReloadCommand(@NotNull DiscordChatBridge plugin) {
        super("dcbreload");

        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PERM)) {
            sendMessage(sender, plugin.getFormatConfig().getNoPermissionMessage().replace("%perm%", PERM));
            return;
        }

        sendMessage(sender, plugin.getFormatConfig().getReloadingMessage());

        if (plugin.reload()) {
            sendMessage(sender, plugin.getFormatConfig().getReloadSuccessMessage());
        } else {
            sendMessage(sender, plugin.getFormatConfig().getReloadFailureMessage());
        }
    }

    private void sendMessage(@NotNull CommandSender sender, @NotNull String message) {
        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
    }
}
