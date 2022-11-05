package net.okocraft.discordchatbridge.chat;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.LunaChatMode;
import com.github.ucchyocean.lc3.member.ChannelMember;
import com.github.ucchyocean.lc3.member.ChannelMemberPlayer;
import com.github.ucchyocean.lc3.member.ChannelMemberProxiedPlayer;
import net.luckperms.api.LuckPermsProvider;
import net.md_5.bungee.api.chat.BaseComponent;
import net.okocraft.discordchatbridge.database.LinkedUser;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class ChannelMemberDiscord extends ChannelMember {

    private final UUID uniqueId;
    private final String name;

    ChannelMemberDiscord(LinkedUser user) {
        this.uniqueId = user.getUniqueId();
        this.name = user.getName();
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String getPrefix() {
        return "";
    }

    @Override
    public String getSuffix() {
        return "";
    }

    @Override
    public void sendMessage(String s) {
    }

    @Override
    public void sendMessage(BaseComponent[] baseComponents) {
    }

    @Override
    public String getWorldName() {
        return "";
    }

    @Override
    public String getServerName() {
        return "";
    }

    @Override
    public boolean hasPermission(String s) {
        try {
            return LuckPermsProvider.get().getUserManager().loadUser(uniqueId).join()
                    .getCachedData()
                    .getPermissionData()
                    .checkPermission(s)
                    .asBoolean();
        } catch (NoClassDefFoundError e) {
            return true;
        }
    }

    @Override
    public String toString() {
        return "$" + uniqueId;
    }

    @Override
    public boolean isPermissionSet(String permission) {
        return true;
    }

    @Override
    public void chat(String s) {
    }

    public static @NotNull ChannelMember getChannelMember(LinkedUser user) {
        ChannelMember result;
        String nameOrUuid = "$" + user.getUniqueId();
        if (LunaChat.getMode() == LunaChatMode.BUKKIT) {
            result = ChannelMemberPlayer.getChannelMember(nameOrUuid);
        } else {
            result = LunaChat.getMode() == LunaChatMode.BUNGEE ? ChannelMemberProxiedPlayer.getChannelMember(nameOrUuid) : null;
        }

        return Objects.requireNonNullElseGet(result, () -> new ChannelMemberDiscord(user));
    }
}
