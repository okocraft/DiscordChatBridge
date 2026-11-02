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

import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigLoader {

    public record ConfigPair(GeneralSettings general, FormatSettings format) {
    }

    private ConfigLoader() {
    }

    public static ConfigPair load(Path directory) throws IOException {
        var general = loadFile(directory.resolve("config.yml"), GeneralSettings.class);
        var format = loadFile(directory.resolve("format.yml"), FormatSettings.class);
        return new ConfigPair(general, format);
    }

    private static <T> T loadFile(Path path, Class<T> type) throws IOException {
        copyDefaultIfMissing(path);

        try {
            var loaded = YamlConfigurationLoader.builder().path(path).build().load().get(type);
            if (loaded == null) {
                throw new IllegalArgumentException("Configuration root must be a mapping");
            }
            if (loaded instanceof GeneralSettings general) {
                general.validate();
            } else if (loaded instanceof FormatSettings format) {
                format.validate();
            }
            return loaded;
        } catch (ConfigurateException | IllegalArgumentException e) {
            throw new IOException("Invalid configuration in " + path + ": " + e.getMessage(), e);
        }
    }

    private static void copyDefaultIfMissing(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        if (Files.exists(path)) {
            return;
        }

        try (InputStream resource = ConfigLoader.class.getClassLoader().getResourceAsStream(path.getFileName().toString())) {
            if (resource == null) {
                throw new IOException("Missing default configuration resource: " + path.getFileName());
            }
            Files.copy(resource, path);
        }
    }
}
