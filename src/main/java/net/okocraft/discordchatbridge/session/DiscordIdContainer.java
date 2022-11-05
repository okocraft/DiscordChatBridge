package net.okocraft.discordchatbridge.session;

import java.util.HashMap;
import java.util.Map;

public final class DiscordIdContainer {

    private static final long EXPIRE_DIFF = 1000 * 60 * 10;

    private static final Map<String, Long> PASSCODE_TO_DISCORD_ID = new HashMap<>();
    private static final Map<String, Long> PASSCODE_CREATED_TIMES = new HashMap<>();

    public static long pop(String passcode) {
        boolean validPass = isValidPasscode(passcode);
        PASSCODE_CREATED_TIMES.remove(passcode);
        if (validPass) {
            return PASSCODE_TO_DISCORD_ID.remove(passcode);
        } else {
            PASSCODE_TO_DISCORD_ID.remove(passcode);
            return -1;
        }
    }

    public static void add(String passcode, long discordUserId) {
        PASSCODE_TO_DISCORD_ID.put(passcode, discordUserId);
        PASSCODE_CREATED_TIMES.put(passcode, System.currentTimeMillis());
    }

    public static boolean isValidPasscode(String passcode) {
        return PASSCODE_CREATED_TIMES.getOrDefault(passcode, 0L) + EXPIRE_DIFF > System.currentTimeMillis()
                && PASSCODE_TO_DISCORD_ID.containsKey(passcode);
    }
}
