pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.5"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

stonecutter {
    create(rootProject) {
        versions(mapOf(
            "fabric-1.21.4" to "1.21.4",
            "fabric-26.2" to "26.2",
            "neoforge-1.21.4" to "1.21.4",
            "neoforge-26.2" to "26.2",
        ))
        vcsVersion = "fabric-26.2"
    }
}

rootProject.name = "ravensmod"
