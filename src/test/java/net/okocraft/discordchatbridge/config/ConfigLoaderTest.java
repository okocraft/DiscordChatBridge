/*
 *     Copyright (c) 2025 Okocraft
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

package net.okocraft.discordchatbridge.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigLoaderTest {

    @TempDir
    Path directory;

    @Test
    void loadsTypedSettingsAndKeepsExistingFiles() throws IOException {
        var customConfig = """
                discord:
                  token: "test-token"
                  status: ONLINE
                  activity:
                    type: DEFAULT
                    game: Playing
                    url: ""
                channels:
                  global: 123
                  staff: 456
                role-prefix:
                  enable: false
                  default: "&f*"
                  "789": "&a%role_color%"
                allow-colors-in-name: false
                """;
        Files.writeString(this.directory.resolve("config.yml"), customConfig);

        var config = ConfigLoader.load(this.directory);

        assertEquals("test-token", config.general().discordToken());
        assertEquals(net.dv8tion.jda.api.entities.Activity.ActivityType.PLAYING, config.general().discordActivityType());
        assertEquals(456L, config.general().linkedChannels().get("staff"));
        assertEquals("&a%role_color%", config.general().rolePrefixes().get("789"));
        assertFalse(config.general().enableRolePrefix());
        assertEquals("false", config.general().allowColorsInName());
        assertEquals(150, config.general().chatMaxLength());
        assertTrue(config.general().sendFirstJoinMessage());
        assertEquals("%player_name%: %message%", config.format().discordChat());
        assertEquals(customConfig, Files.readString(this.directory.resolve("config.yml")));
        assertTrue(Files.exists(this.directory.resolve("format.yml")));
        assertThrows(UnsupportedOperationException.class, () -> config.general().linkedChannels().put("other", 1L));
    }

    @Test
    void rejectsInvalidValuesWithoutRewritingTheFile() throws IOException {
        var invalid = """
                discord:
                  token: test-token
                nickname-length-limit: 0
                """;
        Files.writeString(this.directory.resolve("config.yml"), invalid);

        var error = assertThrows(IOException.class, () -> ConfigLoader.load(this.directory));

        assertTrue(error.getMessage().contains("config.yml"));
        assertTrue(error.getMessage().contains("nickname-length-limit"));
        assertEquals(invalid, Files.readString(this.directory.resolve("config.yml")));
    }

    @Test
    void rejectsMalformedYaml() throws IOException {
        Files.writeString(this.directory.resolve("config.yml"), "discord: [not a mapping");

        assertThrows(IOException.class, () -> ConfigLoader.load(this.directory));
    }
}
