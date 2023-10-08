pluginManagement {
    repositories {
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
            mavenContent {
                includeGroup("org.bukkit")
                includeGroup("org.spigotmc")
            }
        }
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://libraries.minecraft.net/") {
            content {
                includeGroup("com.mojang")
                includeGroup("net.minecraft")
            }
        }
     // maven("https://maven.fabricmc.net/")                                // TODO: Support Fabric
     // maven("https://repo.spongepowered.org/repository/maven-public/")    // TODO: Support Sponge
     // maven("https://maven.neoforged.net/releases/")                      // TODO: Support NeoForge
     // maven("https://repo.lucko.me/")                                     // TODO: Bridge Brigadier by Lucko's Commodore
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("unstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    pluginManagement.repositories.onEach(repositories::add)
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.7.0")
}

rootProject.name = "TriggerReactor-families"

sequenceOf(
    "core",
 // "sponge",
    "bukkit",
    "bukkit:legacy",
    "bukkit:latest",
 // "fabric"
).forEach {
    include(it)
}

val requiredRuntimeVersion: JavaVersion = JavaVersion.toVersion(11)
if (JavaVersion.current() < requiredRuntimeVersion) {
    throw GradleException("""
        You are running a Java version that is too old to build TriggerReactor.

        Required: $requiredRuntimeVersion
        Current: ${JavaVersion.current()}
        """.trimIndent())
}