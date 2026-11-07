plugins {
    alias(libs.plugins.jcommon)
    alias(libs.plugins.bundler)
}

description = "A Paper/Velocity plugin that connects Vanilla chat or LunaChat and Discord"

jcommon {
    javaVersion = JavaVersion.VERSION_25
    setupPaperRepository()
    setupJUnit(libs.junit.bom)
    setupMockito(libs.mockito)

    jarTask {
        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Specification-Title" to project.name,
                "Specification-Version" to project.version,
                "Automatic-Module-Name" to project.group
            )
        }
    }
}

repositories {
    maven("https://raw.githubusercontent.com/ucchyocean/mvn-repo/master") {
        content { includeModule("com.github.ucchyocean", "LunaChat") }
    }
    maven(rootDir.resolve("libs")) {
        content { includeModule("net.okocraft.okochat", "okochat-legacy") }
    }
}

dependencies {
    compileOnly(libs.platform.paper)
    compileOnly(libs.platform.velocity)
    compileOnly(libs.luckperms)
    compileOnly(libs.lunachat) {
        // LunaChat already shades these metrics implementations into its own JAR.
        exclude(group = "org.bstats", module = "bstats-bukkit")
        exclude(group = "org.bstats", module = "bstats-bungeecord")
    }
    compileOnly(libs.okochat.legacy)
    compileOnly(libs.configurate.yaml)
    implementation(libs.jda) {
        exclude(group = "club.minnced", module = "opus-java")
        exclude(group = "com.google.code.gson", module = "gson")
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.configurate.yaml)
    testImplementation(libs.platform.paper)
    testImplementation(libs.platform.velocity)
    testImplementation(libs.luckperms)
    testImplementation(libs.lunachat) {
        exclude(group = "org.bstats", module = "bstats-bukkit")
        exclude(group = "org.bstats", module = "bstats-bungeecord")
    }
}

val pluginProperties = mapOf(
    "version" to project.version.toString(),
    "description" to project.description,
    "url" to "https://github.com/okocraft/DiscordChatBridge"
)

bundler {
    processResources {
        inputs.properties(pluginProperties)
        filesMatching(listOf("plugin.yml", "velocity-plugin.json")) {
            expand("project" to pluginProperties)
        }
    }
}

tasks {
    jar {
        archiveClassifier = "plain"
    }
    shadowJar {
        archiveClassifier = ""
        minimize()
        exclude("META-INF/*", "module-info.class")
        relocate("com.google.crypto", "net.okocraft.discordchatbridge.lib.crypto")
        relocate("com.google.errorprone", "net.okocraft.discordchatbridge.lib.errorprone")
        relocate("google.protobuf", "net.okocraft.discordchatbridge.lib.protobuf")
        relocate("net.dv8tion.jda", "net.okocraft.discordchatbridge.lib.jda")
        relocate("com.fasterxml", "net.okocraft.discordchatbridge.lib.fasterxml")
        relocate("com.iwebpp.crypto", "net.okocraft.discordchatbridge.lib.crypto")
        relocate("com.neovisionaries.ws", "net.okocraft.discordchatbridge.lib.ws")
        relocate("gnu.trove", "net.okocraft.discordchatbridge.lib.trove")
        relocate("javax.annotation", "net.okocraft.discordchatbridge.lib.javax.annotation")
        relocate("okhttp3", "net.okocraft.discordchatbridge.lib.okhttp3")
        relocate("okio", "net.okocraft.discordchatbridge.lib.okio")
        relocate("org.apache.commons", "net.okocraft.discordchatbridge.lib.commons")
        relocate("org.jetbrains", "net.okocraft.discordchatbridge.lib.jetbrains")
        relocate("org.intellij", "net.okocraft.discordchatbridge.lib.intellij")
        relocate("kotlin", "net.okocraft.discordchatbridge.lib.kotlin")
    }
}
