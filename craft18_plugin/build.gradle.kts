group = "xyz.gary600.craft18_plugin"
version = "0.1.0"

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "7.1.2" // needed to build a fat jar w/ dependencies included
    idea // IntelliJ integration
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17)) // Modern Minecraft requires Java 17
    }
}

kotlin {
    sourceSets.all {
        languageSettings.optIn("kotlin.RequiresOptIn") // Allow experimental api
    }
}

// Places to fetch packages from
repositories {
    mavenCentral() // Central package directory, for misc packages
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // Spigot dependency
    maven("https://oss.sonatype.org/content/repositories/snapshots") // Spigot sub-dependency
    maven("https://repo.aikar.co/content/groups/aikar/") // ACF command framework
}

// Packages to fetch + compile
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.0") // Kotlin stdlib
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.0") // Kotlin reflection
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2") // Kotlin serialization
    compileOnly("org.spigotmc:spigot-api:1.18.1-R0.1-SNAPSHOT") // Spigot API (don't include in output)
    implementation("co.aikar:acf-bukkit:0.5.0-SNAPSHOT") // ACF command framework
    implementation("com.fazecast:jSerialComm:[2.0.0,3.0.0)") // Serial port access
}

tasks {
    shadowJar { // "fat jar" build
        // Relocate libraries inside plugin package to prevent conflict with other plugins that use them
        relocate("co.aikar.commands", "xyz.gary600.craft18_plugin.lib.aikar.commands") // ACF
        relocate("co.aikar.locales", "xyz.gary600.craft18_plugin.lib.aikar.locales") // ACF dependency
    }

    build {
        dependsOn(shadowJar) // for convenience, also build shadowJar when doing normal build
    }

    // Custom task: copy fat jar into Spigot plugin folder
    val copyJar by register<Copy>("copyJar") {
        dependsOn(shadowJar) // make sure jar is built
        from(layout.buildDirectory.dir("libs")) // take from the libs output dir
        include("*-all.jar") // copy the fat jar
        mkdir(layout.buildDirectory.dir("server/plugins"))
        into(layout.buildDirectory.dir("server/plugins")) // to server plugins folder
    }

    // Custom task: run Spigot
    register<JavaExec>("runSpigot") {
        // make sure jar is built and copied into correct folder
        dependsOn(shadowJar)
        dependsOn(copyJar)

        classpath = files(layout.buildDirectory.file("server/spigot.jar")) // include Spigot jar - automatically finds main class
        javaLauncher.set(rootProject.javaToolchains.launcherFor(java.toolchain)) // use the same java toolchain
        workingDir(layout.buildDirectory.dir("server")) // run in server directory, to keep project tree clean
        standardInput = System.`in` // pipe stdin so console is accessible
        args("-nogui") // run without the default ugly server gui
    }
}
