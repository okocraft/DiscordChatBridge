# Game Test Guidelines

## Scope

These guidelines cover what DiscordChatBridge promises to players, Discord members and server administrators on a running Paper server or Velocity proxy connected to a real Discord guild.

Pure logic is covered by the JUnit suite under `src/test/java` and is not repeated here. That includes mention parsing, length and line limits, player list formatting, configuration loading, cooldowns, listener filtering, lifecycle and reload decisions, and presence coalescing. The operations below check only what needs a real server, a real Discord connection, or a real LunaChat or LuckPerms installation.

Supported targets, from the plugin descriptors:
- Paper (`src/main/resources/plugin.yml`), with LunaChat and LuckPerms as optional soft dependencies. Folia is not declared and is out of scope.
- Velocity (`src/main/resources/velocity-plugin.json`), with LunaChat as a required dependency.

## Core Behaviors

### CB-001: Minecraft chat reaches Discord

**Description:** On Paper without LunaChat, global chat is posted to `system-channel`. With LunaChat, each LunaChat channel listed in `channels` is posted to its Discord channel. Posts use `format.yml` `server.chat`, resolve `@user` and `#channel` names, and notify only mentioned users. Roles, `@everyone` and `@here` never notify anyone.

**Importance:** This is the main purpose of the plugin, and unintended pings disturb the whole guild.

**Related code:**
- `src/main/java/net/okocraft/discordchatbridge/platform/bukkit/PaperChatListener.java`
- `src/main/java/net/okocraft/discordchatbridge/listener/chat/LunaChatListener.java`
- `src/main/java/net/okocraft/discordchatbridge/DiscordBot.java:processChat`

### CB-002: Discord chat reaches Minecraft

**Description:** A message in a Discord channel listed in `channels` is shown in Minecraft. Without LunaChat, only the `global` entry is broadcast. With LunaChat, it goes to the LunaChat channel of the same name. The sender is shown with the role prefix. Without LunaChat, the message body is shown literally: legacy color codes and MiniMessage-like text are not interpreted. With LunaChat, rendering is left to LunaChat. Bot messages, direct messages and unlinked channels are ignored. Messages over the configured limits get a ⚠ reaction instead of being relayed.

**Importance:** This is the other direction of the bridge, and the literal body prevents chat formatting injection.

**Related code:**
- `src/main/java/net/okocraft/discordchatbridge/listener/DiscordListener.java:onMessageReceived`
- `src/main/java/net/okocraft/discordchatbridge/platform/bukkit/PaperChatSystem.java`
- `src/main/java/net/okocraft/discordchatbridge/chat/LunaChatSystem.java`

### CB-003: Relayed Discord messages are not sent back

**Description:** A Discord message relayed into LunaChat is not forwarded to Discord again. A player whose display name happens to end with `discord-source-name` is still forwarded.

**Importance:** A loop would duplicate every Discord message, and the false match silences real players.

**Related code:**
- `src/main/java/net/okocraft/discordchatbridge/listener/chat/LunaChatListener.java:processChat`

### CB-004: Server notifications reach `system-channel` without pings

**Description:** Join, leave, first join (requires LuckPerms) and server switch (Velocity only) are posted to `system-channel` when enabled in `message-sending-setting`. They never notify anyone, even when a display name or format contains `@everyone`.

**Importance:** Operators rely on these notifications, and a ping would reach the whole guild.

**Related code:**
- `src/main/java/net/okocraft/discordchatbridge/listener/ServerListener.java`
- `src/main/java/net/okocraft/discordchatbridge/listener/luckperms/FirstJoinListener.java`

### CB-005: Bot presence and `!playerlist` show the current players

**Description:** The bot activity shows the player count about one second after joins and leaves. `!playerlist` in `system-channel` or a linked channel replies with the online players per server, at most once every five seconds.

**Importance:** These are the visible status features for Discord members.

**Related code:**
- `src/main/java/net/okocraft/discordchatbridge/DiscordBot.java:updateGame`
- `src/main/java/net/okocraft/discordchatbridge/listener/DiscordListener.java:onPlayerListCommand`

### CB-006: Startup, reload and shutdown keep the server usable

**Description:**
- Startup does not wait for Discord.
- If the Discord login fails, the plugin stays enabled with only `/dcbreload`, and `/dcbreload` recovers after the configuration is fixed.
- `/dcbreload` with an invalid configuration keeps the current settings and connection.
- `/dcbreload` with an unchanged token keeps the connection and applies the new settings.
- Shutdown stops the bot without delaying server stop.
- `/dcbreload` requires `discordchatbridge.reload`.

**Importance:** A broken Discord connection or configuration must not take the game server down or block recovery.

**Related code:**
- `src/main/java/net/okocraft/discordchatbridge/DiscordChatBridgePlugin.java:enable`, `reload`, `disable`
- `src/main/java/net/okocraft/discordchatbridge/platform/bukkit/DiscordChatBridgeBukkit.java`
- `src/main/java/net/okocraft/discordchatbridge/platform/velocity/DiscordChatBridgeVelocity.java`
- `src/main/java/net/okocraft/discordchatbridge/command/ReloadCommand.java`

## Platform Dependency Points

### DP-001: Paper chat event and broadcast

**Platform:** Paper

**Usage:** The plugin listens to `AsyncChatEvent` at `MONITOR` without modifying it, and reads `Player#displayName()` and `event.message()`. Discord messages are shown with `Bukkit.broadcast(Component)`.

**Affected behaviors:**
- CB-001
- CB-002

**Regression risks:**
- Chat is not forwarded, is forwarded while cancelled, or loses its rendering or signature.
- Broadcast text is interpreted as formatting.

**Relevant APIs / concepts:**
- `io.papermc.paper.event.player.AsyncChatEvent`
- `Bukkit#broadcast(Component)`

**Related code:**
- `src/main/java/net/okocraft/discordchatbridge/platform/bukkit/PaperChatListener.java`
- `src/main/java/net/okocraft/discordchatbridge/platform/bukkit/PaperChatSystem.java`

### DP-002: Join and leave events and online player count

**Platform:** Paper, Velocity

**Usage:** Paper uses `PlayerJoinEvent` and `PlayerQuitEvent`. Velocity uses `ServerConnectedEvent` (join or switch) and `DisconnectEvent`. The player count is read one second later from `Bukkit#getOnlinePlayers` or `ProxyServer#getPlayerCount`.

**Affected behaviors:**
- CB-004
- CB-005

**Regression risks:**
- Notifications are missing or duplicated.
- Switch is reported as join.
- The count is off by one after leaving.

**Relevant APIs / concepts:**
- `PlayerJoinEvent`, `PlayerQuitEvent`, `ServerConnectedEvent#getPreviousServer`, `DisconnectEvent`

**Related code:**
- `src/main/java/net/okocraft/discordchatbridge/platform/bukkit/BukkitServerListener.java`
- `src/main/java/net/okocraft/discordchatbridge/platform/velocity/VelocityServerListener.java`

### DP-003: Plugin lifecycle and command registration

**Platform:** Paper, Velocity

**Usage:**
- Paper: `onEnable`/`onDisable` and the `dcbreload` command from `plugin.yml`.
- Velocity: `ProxyInitializeEvent`/`ProxyShutdownEvent`, `/dcbreload` registered once through `CommandManager`, and listeners unregistered individually so the shutdown subscription survives reloads.

**Affected behaviors:**
- CB-006

**Regression risks:**
- `/dcbreload` is missing after a failed login.
- The bot keeps running after server stop.
- Server stop hangs.

**Relevant APIs / concepts:**
- `JavaPlugin` lifecycle, `CommandManager#register`, `EventManager#unregisterListener`

**Related code:**
- `src/main/java/net/okocraft/discordchatbridge/platform/bukkit/DiscordChatBridgeBukkit.java`
- `src/main/java/net/okocraft/discordchatbridge/platform/velocity/DiscordChatBridgeVelocity.java`

### DP-004: LunaChat channel API

**Platform:** LunaChat 3 (Bukkit and Velocity)

**Usage:**
- `LunaChatBukkitChannelMessageEvent` and `LunaChatVelocityChannelMessageEvent` deliver channel chat.
- `Channel#chatFromOtherSource(sender, source, message)` injects Discord messages as a `ChannelMemberOther` named `<sender>@<source>`.

**Affected behaviors:**
- CB-001
- CB-002
- CB-003

**Regression risks:**
- Channel chat is not forwarded.
- Discord messages do not appear in LunaChat.
- A changed member type or name causes a loop or drops players.

**Relevant APIs / concepts:**
- `Channel#chatFromOtherSource`, `ChannelMemberOther`

**Related code:**
- `src/main/java/net/okocraft/discordchatbridge/listener/chat/LunaChatListener.java`
- `src/main/java/net/okocraft/discordchatbridge/chat/LunaChatSystem.java`

### DP-005: LuckPerms first login event

**Platform:** LuckPerms

**Usage:** `UserFirstLoginEvent` marks a player whose next join is announced with `server.first-join`.

**Affected behaviors:**
- CB-004

**Regression risks:**
- A first join is announced as a normal join, or not at all.

**Relevant APIs / concepts:**
- `net.luckperms.api.event.user.UserFirstLoginEvent`

**Related code:**
- `src/main/java/net/okocraft/discordchatbridge/listener/luckperms/FirstJoinListener.java`

### DP-006: Discord gateway through JDA

**Platform:** Discord (JDA)

**Usage:**
- The plugin logs in asynchronously with the `GUILD_MEMBERS` and `MESSAGE_CONTENT` intents and caches all members.
- It sets allowed mentions per message, sets the presence, and adds reactions.

**Affected behaviors:**
- CB-001
- CB-002
- CB-004
- CB-005
- CB-006

**Regression risks:**
- The bot connects but receives empty message content.
- Mentions ping more or fewer targets.
- The presence never updates.
- Login failures block startup.

**Relevant APIs / concepts:**
- Privileged gateway intents, allowed mentions, presence

**Related code:**
- `src/main/java/net/okocraft/discordchatbridge/DiscordBot.java`

## Game Test Viewpoints

### VP-001: Chat round trip in each mode

**Verify:**
- A player's chat appears in the configured Discord channel with the `server.chat` format.
- A Discord member's message appears in Minecraft with the role prefix and the body unchanged.

**Related behaviors:**
- CB-001
- CB-002

**Related dependency points:**
- DP-001
- DP-004
- DP-006

**Failure examples:**
- Nothing arrives on one side.
- `&c` or `<red>` in a Discord message colors the Minecraft text.

### VP-002: Mentions notify only the mentioned user

**Verify:**
- `@<member name>` from Minecraft notifies that member.
- `@everyone`, `@here` and a role mention from Minecraft do not notify anyone.
- Notification messages never notify anyone.

**Related behaviors:**
- CB-001
- CB-004

**Related dependency points:**
- DP-006

**Failure examples:**
- The whole guild is pinged by a player's chat or by a join message.

### VP-003: No relay loop and no false exclusion with LunaChat

**Verify:**
- A Discord message relayed into LunaChat appears once in Discord.
- A player named `<something><discord-source-name>` (for example `HeroDis`) is forwarded.

**Related behaviors:**
- CB-003

**Related dependency points:**
- DP-004

**Failure examples:**
- The Discord message is echoed back by the bot.
- The player's chat never reaches Discord.

### VP-004: Notifications, presence and player list follow players

**Verify:**
- Join, leave, first join and (Velocity) switch messages appear once each in `system-channel`.
- The bot activity shows the new player count shortly after each change.
- `!playerlist` lists the online players and is ignored when repeated within five seconds.

**Related behaviors:**
- CB-004
- CB-005

**Related dependency points:**
- DP-002
- DP-005
- DP-006

**Failure examples:**
- A switch is reported as a join.
- The count stays one too high after a leave.
- `!playerlist` floods the channel.

### VP-005: Failures and reloads keep the server usable

**Verify:**
- With an invalid token, the server starts without waiting for Discord, logs the failure and still accepts `/dcbreload`.
- After fixing the token, `/dcbreload` connects.
- An invalid configuration reload keeps the bridge working.
- A reload with the same token keeps relaying without a reconnect.
- Server stop finishes promptly with no plugin errors.

**Related behaviors:**
- CB-006

**Related dependency points:**
- DP-003
- DP-006

**Failure examples:**
- Startup hangs.
- `/dcbreload` is unknown on Velocity after a failed login.
- The bot stays online after the server stops.

## Operation Guidelines

### OP-001: Paper without LunaChat

**Purpose:** Chat round trip, mentions, notifications, presence and player list on vanilla Paper chat.

**Setup:**
1. Paper server with DiscordChatBridge and LuckPerms; LunaChat is not installed.
2. `config.yml`: test bot token, `system-channel` and `channels.global` set to the same test channel id, and all `message-sending-setting` entries `true`.
3. Two Discord accounts in the test guild: member A (no role) and member B (with a colored role).
4. Two Minecraft accounts: P1 (has joined before) and P2 (never joined; delete its LuckPerms user data if needed).

**Operation:**
1. Start the server and join with P1.
2. P1 chats `hello @<member A name> @everyone`.
3. Member B posts `&chi <red>there` in the channel.
4. Member A posts `!playerlist` twice within five seconds.
5. Join with P2, then disconnect P2.

**Verify / PASS criteria:**
- The P1 join message appears once and the bot activity shows `1 players` within a few seconds.
- The P1 chat appears with the `server.chat` format and member A is notified. Nobody is notified by `@everyone`.
- In Minecraft, member B's message is shown with the role prefix and the literal text `&chi <red>there`.
- Exactly one player list reply appears.
- P2 produces the first-join message (not the normal join message), then a leave message. The activity returns to `1 players`.
- No notification is triggered by any join or leave message.

**Covers:**
- VP-001, VP-002, VP-004
- CB-001, CB-002, CB-004, CB-005
- DP-001, DP-002, DP-005, DP-006

### OP-002: Paper with LunaChat

**Purpose:** LunaChat channel round trip and loop prevention.

**Setup:**
1. OP-001 server with LunaChat installed and a LunaChat channel `global` mapped in `channels`.
2. A Minecraft account whose display name ends with `discord-source-name` (default `Dis`), for example by setting a nickname `HeroDis`.

**Operation:**
1. Member A posts `from discord` in the linked channel.
2. Player P1 chats `from minecraft` in the LunaChat channel.
3. The `HeroDis` player chats `hero here` in the LunaChat channel.

**Verify / PASS criteria:**
- `from discord` appears in the LunaChat channel with the sender suffixed by `@Dis`, and appears only once in Discord, with no bot echo.
- `from minecraft` and `hero here` both appear in Discord.

**Covers:**
- VP-001, VP-003
- CB-001, CB-002, CB-003
- DP-004

### OP-003: Velocity with LunaChat

**Purpose:** Proxy notifications, switch detection, cross-server player list and chat.

**Setup:**
1. Velocity proxy with DiscordChatBridge and LunaChat, and two backend servers `lobby` and `survival`.
2. The same Discord configuration as OP-002.

**Operation:**
1. P1 joins through the proxy (lands on `lobby`).
2. P1 runs `/server survival`.
3. P1 chats in the LunaChat channel, and member A replies in Discord.
4. Member A posts `!playerlist`.
5. P1 disconnects.

**Verify / PASS criteria:**
- The Discord channel shows one join, then one switch message naming `survival` (not a second join), then one leave.
- Chat appears in both directions as in OP-002.
- The player list shows P1 under `survival`.
- The bot activity reflects the proxy player count after each step.

**Covers:**
- VP-001, VP-004
- CB-001, CB-002, CB-004, CB-005
- DP-002, DP-004, DP-006

### OP-004: Failure, reload and shutdown

**Purpose:** Startup without Discord, recovery with `/dcbreload`, reload safety and clean shutdown, on both Paper and Velocity.

**Setup:**
1. OP-001 (Paper) or OP-003 (Velocity) environment with `discord.token` set to an invalid value.
2. An operator account with `discordchatbridge.reload` and a player account without it.

**Operation:**
1. Start the server or proxy.
2. As the player without permission, run `/dcbreload`.
3. Put the valid token in `config.yml` and run `/dcbreload` as the operator.
4. Change `format.yml` `server.chat` and `discord.status` to `IDLE`, then run `/dcbreload`.
5. Break `config.yml` (for example set `discord.token: ""`) and run `/dcbreload`.
6. Restore `config.yml`, run `/dcbreload`, then stop the server.

**Verify / PASS criteria:**
- Step 1: startup completes without waiting for Discord, and the console shows `Could not log in to Discord`.
- Step 2: the permission message is shown and nothing reloads.
- Step 3: the bot comes online and chat relays.
- Step 4: the bot does not disconnect, its status becomes Idle, and new chat uses the new format.
- Step 5: the failure message is shown and chat keeps relaying with the previous settings.
- Step 6: the server stops within its normal time, the bot goes offline, and no DiscordChatBridge exception is logged.

**Covers:**
- VP-005
- CB-006
- DP-003, DP-006

## Conditional Test Areas

### CT-001: Paper chat API

**Trigger:** Changes to `AsyncChatEvent`, chat rendering or signed chat, or to `Bukkit#broadcast`.

**Related dependency points:**
- DP-001

**Possible regressions:**
- Chat is not forwarded, or Discord text is rendered with formatting.

**Recommended verification:** OP-001 steps 2–3.

### CT-002: Velocity events and commands

**Trigger:** Changes to `ServerConnectedEvent`, `DisconnectEvent`, `CommandManager` or `EventManager` listener ownership.

**Related dependency points:**
- DP-002
- DP-003

**Possible regressions:**
- Switch is reported as join.
- `/dcbreload` disappears.
- Shutdown cleanup no longer runs after a reload.

**Recommended verification:** OP-003, and OP-004 on Velocity.

### CT-003: LunaChat update

**Trigger:** A LunaChat release that changes channel message events, `Channel#chatFromOtherSource` or `ChannelMemberOther`.

**Related dependency points:**
- DP-004

**Possible regressions:**
- Relay loop, dropped players, or Discord messages missing in LunaChat.

**Recommended verification:** OP-002.

### CT-004: JDA or Discord API update

**Trigger:** A JDA upgrade, or Discord changes to intents, allowed mentions or presence.

**Related dependency points:**
- DP-006

**Possible regressions:**
- Empty message content, unexpected pings, a stale presence, or a blocking login.

**Recommended verification:** OP-001 and OP-004.

## Test Design Notes

- Use a dedicated test guild and bot. Enable the Server Members and Message Content privileged intents for the bot. Never commit the token or paste it into issues, logs or pull requests.
- Without LunaChat, Minecraft chat goes to `system-channel` and only `channels.global` is relayed to Minecraft. Set both to the same channel id for a two-way test.
- Notifications are observed most reliably from a second Discord client logged in as the mentioned member, with notifications enabled for the test channel.
- Presence updates are delayed by about one second and coalesced. Wait a few seconds before judging the activity text.

## Maintenance Rules

Update this file when:
- A Core Behavior is added, removed, or materially changed.
- The plugin changes how it depends on Paper, Velocity, LunaChat, LuckPerms or Discord.
- A regression reveals an important missing durable viewpoint or dependency.
- An existing operation fails to detect a meaningful problem.
- A setup or operation is found to be unstable or unnecessarily long.

Keep CB/DP/VP/OP/CT identifiers stable across revisions. Never reassign a deleted identifier. Do not store one-off update notes, temporary UV IDs, or version-specific NEW-OP actions in this file.
