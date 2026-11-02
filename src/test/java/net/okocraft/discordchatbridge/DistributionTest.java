package net.okocraft.discordchatbridge;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.*;

class DistributionTest {

    @Test
    void testBothPlatformDescriptorsHaveExpandedMetadataAndEntrypoints() throws IOException {
        try (var jar = new JarFile(System.getProperty("distributionJar"))) {
            for (var descriptor : new String[]{"plugin.yml", "velocity-plugin.json"}) {
                var entry = jar.getJarEntry(descriptor);
                assertNotNull(entry, descriptor);
                var text = new String(jar.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8);
                assertFalse(text.contains("${"), descriptor);
                assertTrue(text.contains(System.getProperty("projectVersion")), descriptor);
                assertTrue(text.contains("A Paper/Velocity plugin that connects Vanilla chat or LunaChat and Discord"));
            }
            assertNotNull(jar.getJarEntry("net/okocraft/discordchatbridge/platform/bukkit/DiscordChatBridgeBukkit.class"));
            assertNotNull(jar.getJarEntry("net/okocraft/discordchatbridge/platform/velocity/DiscordChatBridgeVelocity.class"));
            assertEquals(System.getProperty("projectVersion"), jar.getManifest().getMainAttributes().getValue("Implementation-Version"));
        }
    }

    @Test
    void testJdaIsBundledAndRelocated() throws IOException {
        try (var jar = new JarFile(System.getProperty("distributionJar"))) {
            assertNotNull(jar.getJarEntry("net/okocraft/discordchatbridge/lib/jda/api/JDA.class"));
            assertNotNull(jar.getJarEntry("net/okocraft/discordchatbridge/lib/okhttp3/OkHttpClient.class"));
            assertFalse(jar.stream().anyMatch(entry -> entry.getName().startsWith("net/dv8tion/jda/")));
            assertFalse(jar.stream().anyMatch(entry -> entry.getName().startsWith("okhttp3/")));
        }
    }

    @Test
    void testProvidedAndExcludedLibrariesAreNotBundled() throws IOException {
        try (var jar = new JarFile(System.getProperty("distributionJar"))) {
            for (var prefix : new String[]{"org/bukkit/", "io/papermc/", "com/velocitypowered/",
                    "net/luckperms/", "com/github/ucchyocean/", "net/okocraft/okochat/",
                    "org/spongepowered/configurate/", "org/slf4j/", "com/google/gson/", "club/minnced/opus/"}) {
                assertFalse(jar.stream().anyMatch(entry -> entry.getName().startsWith(prefix)), prefix);
            }
        }
    }
}
