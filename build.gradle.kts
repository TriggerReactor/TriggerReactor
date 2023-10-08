import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.indra.git)
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.taskTree)
    `java-library`
}

val javaVersionFromCli = providers.systemProperty("java.version")
    .orElse(providers.gradleProperty("java.version"))
    .map { JavaVersion.toVersion(it) }
    .getOrElse(JavaVersion.VERSION_17)

val theManifest = the<JavaPluginExtension>().manifest {
    attributes(
        "Specification-Title" to project.name,
        "Specification-Vendor" to "TriggerReactor Team",
        "Specification-Version" to project.version,
        "Implementation-Title" to project.name,
        "Implementation-Vendor" to "TriggerReactor core team",
        "Implementation-Version" to project.version,
        "Implementation-Timestamp" to DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
    )

    if (indraGit.isPresent) {
        indraGit.applyVcsInformationToManifest(this)
    }

    /// These two are included by most CI's
    System.getenv()["GIT_COMMIT"]?.apply { attributes("Git-Commit" to this) }
    System.getenv()["GIT_BRANCH"]?.apply { attributes("Git-Branch" to this) }
}

subprojects {
    apply(plugin = "java-library")

    configure<JavaPluginExtension> {
        // Using a toolchain is preferred. Never remove this line and uncomment the following lines:
        // ########################################
     // sourceCompatibility = javaVersionFromCli
     // targetCompatibility = javaVersionFromCli

        toolchain {
            languageVersion = JavaLanguageVersion.of(javaVersionFromCli.majorVersion)
        }
    }

    dependencies {
        implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
        implementation(rootProject.libs.mysql)
        if (javaVersionFromCli.isJava11Compatible) {
            // Nashorn is deprecated in Java 11, but we still need it. This is the only way to get it.
            implementation(rootProject.libs.nashorn)
        }

        // For future use
     // testImplementation(platform(rootProject.libs.junit.bom))
     // testImplementation(rootProject.libs.junit.api)
        testImplementation(rootProject.libs.junit)
        testImplementation(rootProject.libs.mockito.core)
        testImplementation(rootProject.libs.javassist)
    }

    tasks {
        java {
            withJavadocJar()

            manifest().from(theManifest)
        }

        compileJava {
            options.encoding = StandardCharsets.UTF_8.displayName()
            options.release.set(javaVersionFromCli.majorVersion.toInt())
        }

        processResources {
            include("**/*.js", "**/*.yml")
        }

        test {
            useJUnitPlatform()

            reports.html.required.set(false)
            testLogging {
                showStandardStreams = true
                showExceptions = true
                showCauses = true
                showStackTraces = true

                events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
                exceptionFormat = TestExceptionFormat.FULL
            }

            // @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
            // jvmArgs!!.addAll(setOf("--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED"))
        }

        javadoc {
            isFailOnError = false

            options.encoding(StandardCharsets.UTF_8.displayName())
            (options as StandardJavadocDocletOptions).apply {
                links("https://docs.oracle.com/en/java/javase/17/docs/api/")
                links("https://hub.spigotmc.org/javadocs/bukkit/")

                addStringOption("Xdoclint:none", "-quiet")
                addBooleanOption("html5", javaVersionFromCli.isJava9Compatible)
            }
        }
    }
}