/*
 *     Copyright (c) 2026 Okocraft
 *
 *     This file is part of DiscordChatBridge.
 *
 *     DiscordChatBridge is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     DiscordChatBridge is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with DiscordChatBridge. If not, see <https://www.gnu.org/licenses/>.
 */

package net.okocraft.discordchatbridge;

import net.okocraft.discordchatbridge.config.ConfigLoader;

public final class BridgeLifecycle {

    public enum State { STOPPED, STARTING, RUNNING, RECONNECTING, STOPPING, FAILED }

    private final DiscordChatBridgePlugin plugin;
    private volatile State state = State.STOPPED;
    private volatile DiscordBot bot;
    private boolean commandsRegistered;

    public BridgeLifecycle(DiscordChatBridgePlugin plugin) {
        this.plugin = plugin;
    }

    public State state() {
        var current = this.state;
        return current == State.RUNNING && !this.bot.connected() ? State.RECONNECTING : current;
    }

    DiscordBot bot() {
        return this.bot;
    }

    synchronized void login() {
        if (this.state != State.STARTING) {
            throw new IllegalStateException("Login requires an active startup");
        }
        this.bot = DiscordBot.login(this.plugin);
    }

    synchronized boolean start() {
        if (this.state == State.RUNNING) {
            return true;
        }
        if (this.state == State.STARTING || this.state == State.STOPPING) {
            return false;
        }
        if (this.state == State.FAILED && !this.stopRuntime()) {
            return false;
        }
        this.state = State.STARTING;
        try {
            if (!this.commandsRegistered) {
                try {
                    this.plugin.registerCommands();
                    this.commandsRegistered = true;
                } catch (Exception e) {
                    try {
                        this.plugin.unregisterCommands();
                    } catch (Exception cleanup) {
                        e.addSuppressed(cleanup);
                    }
                    throw e;
                }
            }
            this.plugin.loginToDiscord();
            this.plugin.registerListeners();
            if (this.plugin.getGeneralConfig().messageSendingSetting.firstJoin) {
                this.plugin.registerLuckPermsFirstJoinListener();
            }
            this.state = State.RUNNING;
            if (this.bot.terminated()) {
                throw new IllegalStateException("Discord connection terminated during startup");
            }
            this.bot.updateGame();
            return true;
        } catch (Exception e) {
            this.stopRuntime();
            this.state = State.FAILED;
            this.plugin.getWrappedLogger().error("Could not start DiscordChatBridge", e);
            return false;
        }
    }

    synchronized boolean restart(ConfigLoader.ConfigPair settings) {
        if (!this.stopRuntime()) {
            return false;
        }
        this.plugin.setSettings(settings);
        return this.start();
    }

    synchronized void stop() {
        this.stopRuntime();
        if (this.commandsRegistered) {
            try {
                this.plugin.unregisterCommands();
                this.commandsRegistered = false;
            } catch (Exception e) {
                this.state = State.FAILED;
                this.plugin.getWrappedLogger().error("Could not unregister commands", e);
            }
        }
    }

    void botStopped(DiscordBot stopped) {
        if (this.bot != stopped || this.state != State.RUNNING) {
            return;
        }
        synchronized (this) {
            if (this.bot == stopped && this.state == State.RUNNING) {
                this.stopRuntime();
                this.state = State.FAILED;
            }
        }
    }

    private boolean stopRuntime() {
        if (this.state == State.STOPPED) {
            return true;
        }
        this.state = State.STOPPING;
        boolean success = true;
        try {
            this.plugin.unregisterListeners();
        } catch (Exception e) {
            success = false;
            this.plugin.getWrappedLogger().error("Could not unregister listeners", e);
        }
        try {
            this.plugin.unregisterLuckPermsFirstJoinListener();
        } catch (Exception e) {
            success = false;
            this.plugin.getWrappedLogger().error("Could not unsubscribe from LuckPerms", e);
        }
        if (this.bot != null) {
            try {
                this.bot.shutdown();
            } catch (Exception e) {
                success = false;
                this.plugin.getWrappedLogger().error("Could not shut down Discord", e);
            }
        }
        this.state = success ? State.STOPPED : State.FAILED;
        return success;
    }
}
