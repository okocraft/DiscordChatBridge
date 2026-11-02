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

package net.okocraft.discordchatbridge.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigLoaderTest {

    @TempDir
    Path directory;

    @BeforeEach
    void writeValidSettings() throws IOException {
        Files.writeString(directory.resolve("config.yml"), "discord:\n  token: test-token\n");
        Files.writeString(directory.resolve("format.yml"), "server:\n  chat: '[%player_name%] %message%'\n");
    }

    @Test
    void testLoadReadsSettingsWithoutChangingExistingFiles() throws IOException {
        var config = """
                # Keep administrator comments.
                discord:
                  token: test-token
                server-name: survival
                system-channel: 123456789012345678
                channels:
                  global: 234567890123456789
                message-sending-setting:
                  switch: false
                """;
        Files.writeString(directory.resolve("config.yml"), config);
        var format = Files.readString(directory.resolve("format.yml"));

        var settings = ConfigLoader.load(directory);

        assertEquals("survival", settings.general().serverName);
        assertEquals(123456789012345678L, settings.general().systemChannel);
        assertEquals(234567890123456789L, settings.general().channels.get("global"));
        assertFalse(settings.general().messageSendingSetting.switchMessage);
        assertEquals("[%player_name%] %message%", settings.format().server.chat);
        assertEquals(config, Files.readString(directory.resolve("config.yml")));
        assertEquals(format, Files.readString(directory.resolve("format.yml")));
    }

    @Test
    void testLoadCreatesMissingFormatFromBundledResource() throws IOException {
        Files.delete(directory.resolve("format.yml"));

        var settings = ConfigLoader.load(directory);

        assertNotNull(settings.format().command.reload.success);
        try (var resource = ConfigLoader.class.getClassLoader().getResourceAsStream("format.yml")) {
            assertNotNull(resource);
            assertArrayEquals(resource.readAllBytes(), Files.readAllBytes(directory.resolve("format.yml")));
        }
    }

    @Test
    void testLoadReadsEditedSettingsOnSubsequentCall() throws IOException {
        var first = ConfigLoader.load(directory);
        Files.writeString(directory.resolve("config.yml"), "discord:\n  token: test-token\nserver-name: creative\n");

        var second = ConfigLoader.load(directory);

        assertEquals("creative", second.general().serverName);
        assertEquals("server", first.general().serverName);
    }

    @ParameterizedTest
    @ValueSource(strings = {"chat-max-length: -1", "chat-max-lines: -1", "chat-max-attachments: -1",
            "nickname-length-limit: 0", "nickname-length-limit: -1"})
    void testLoadRejectsInvalidLimitsWithoutOverwritingFile(String invalidSetting) throws IOException {
        var config = "discord:\n  token: test-token\n" + invalidSetting + "\n";
        Files.writeString(directory.resolve("config.yml"), config);

        var error = assertThrows(IOException.class, () -> ConfigLoader.load(directory));

        assertTrue(error.getMessage().contains("config.yml"));
        assertInstanceOf(IllegalArgumentException.class, error.getCause());
        assertEquals(config, Files.readString(directory.resolve("config.yml")));
    }

    @Test
    void testLoadAcceptsUnlimitedChatLimitsAndMinimumNicknameLimit() throws IOException {
        Files.writeString(directory.resolve("config.yml"), """
                discord:
                  token: test-token
                chat-max-length: 0
                chat-max-lines: 0
                chat-max-attachments: 0
                nickname-length-limit: 1
                """);

        var settings = ConfigLoader.load(directory).general();

        assertEquals(0, settings.chatMaxLength);
        assertEquals(0, settings.chatMaxLines);
        assertEquals(0, settings.chatMaxAttachments);
        assertEquals(1, settings.nicknameLengthLimit);
    }

    @ParameterizedTest
    @ValueSource(strings = {"config.yml", "format.yml"})
    void testLoadReportsMalformedYamlWithoutOverwritingFile(String filename) throws IOException {
        var malformed = "broken: [\n";
        var path = directory.resolve(filename);
        Files.writeString(path, malformed);

        var error = assertThrows(IOException.class, () -> ConfigLoader.load(directory));

        assertTrue(error.getMessage().contains(filename));
        assertNotNull(error.getCause());
        assertEquals(malformed, Files.readString(path));
    }

    @ParameterizedTest
    @ValueSource(strings = {"''", "'   '"})
    void testLoadRejectsBlankToken(String token) throws IOException {
        Files.writeString(directory.resolve("config.yml"), "discord:\n  token: " + token + "\n");

        var error = assertThrows(IOException.class, () -> ConfigLoader.load(directory));

        assertTrue(error.getMessage().contains("config.yml"));
        assertInstanceOf(IllegalArgumentException.class, error.getCause());
    }
}
