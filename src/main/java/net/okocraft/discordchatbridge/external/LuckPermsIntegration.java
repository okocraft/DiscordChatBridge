package net.okocraft.discordchatbridge.external;

import java.util.UUID;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.UserManager;

public final class LuckPermsIntegration {

    public static boolean hasPermission(UUID uuid, String node) {
        try {
            final UserManager userManager = LuckPermsProvider.get().getUserManager();
            return userManager.loadUser(uuid).join().getCachedData().getPermissionData().checkPermission(node).asBoolean();
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }
}
