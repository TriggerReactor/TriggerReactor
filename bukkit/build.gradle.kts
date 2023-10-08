plugins {
    alias(libs.plugins.shadow)
    `java-library`
}

allprojects {
    apply(plugin = rootProject.libs.plugins.shadow.get().pluginId)
    apply(plugin = "java-library")

    dependencies {
        implementation(rootProject.libs.spigotmc.api)
        api(rootProject.projects.core)
        testImplementation(rootProject.projects.core.dependencyProject.sourceSets.test.get().output)
    }

    tasks.shadowJar {
        // TODO
    }
}

subprojects {
    dependencies {
        api(rootProject.projects.bukkit)
        testImplementation(rootProject.projects.bukkit.dependencyProject.sourceSets.test.get().output)
    }
}