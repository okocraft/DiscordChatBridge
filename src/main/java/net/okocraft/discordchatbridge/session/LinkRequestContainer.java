package net.okocraft.discordchatbridge.session;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class LinkRequestContainer {

    private static final long EXPIRE_DIFF = 1000 * 60 * 10;

    private static final Map<String, LinkRequestEntry> PASSCODE_TO_MINECRAFT_ACCOUNT = new HashMap<>();
    private static final Map<String, Long> PASSCODE_CREATED_TIMES = new HashMap<>();

    public static LinkRequestEntry pop(String passcode) {
        boolean validPass = isValidPasscode(passcode);
        PASSCODE_CREATED_TIMES.remove(passcode);
        if (validPass) {
            return PASSCODE_TO_MINECRAFT_ACCOUNT.remove(passcode);
        } else {
            PASSCODE_TO_MINECRAFT_ACCOUNT.remove(passcode);
            return null;
        }
    }

    public static void add(String passcode, UUID minecraftUuid, String minecraftName) {
        PASSCODE_TO_MINECRAFT_ACCOUNT.put(passcode, new LinkRequestEntry(minecraftUuid, minecraftName));
        PASSCODE_CREATED_TIMES.put(passcode, System.currentTimeMillis());
    }

    public static boolean isValidPasscode(String passcode) {
        return PASSCODE_CREATED_TIMES.getOrDefault(passcode, 0L) + EXPIRE_DIFF > System.currentTimeMillis()
                && PASSCODE_TO_MINECRAFT_ACCOUNT.containsKey(passcode);
    }
}
