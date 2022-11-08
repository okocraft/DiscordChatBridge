package net.okocraft.discordchatbridge.external;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.util.Tristate;

import java.util.UUID;

public final class LuckPermsIntegration {

    private static final boolean LUCKPERMS_EXISTS;

    static {
        LUCKPERMS_EXISTS = checkLuckPerms();
    }

    private static boolean checkLuckPerms() {
        try {
            Class.forName("net.luckperms.api.LuckPermsProvider");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean hasPermission(UUID uuid, String node, boolean def) {
        if (LUCKPERMS_EXISTS) {
            var result = LuckPermsProvider.get().getUserManager().loadUser(uuid).join().getCachedData().getPermissionData().checkPermission(node);
            return result != Tristate.UNDEFINED ? result.asBoolean() : def;
        } else {
            return def;
        }
    }
}
