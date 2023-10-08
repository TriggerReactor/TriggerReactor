configurations.all {
    resolutionStrategy {
        force(rootProject.libs.spigotmc.api.legacy)
    }
}

dependencies {
    implementation(rootProject.libs.spigotmc.api.legacy)
    testImplementation(rootProject.libs.spigotmc.api.legacy)
}